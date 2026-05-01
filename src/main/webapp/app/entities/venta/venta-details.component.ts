import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IVenta } from '@/shared/model/venta.model';
import { type IPago } from '@/shared/model/pago.model';
import { EstadoPago } from '@/shared/model/estado-pago.model';
import { EstadoComprobante } from '@/shared/model/estado-comprobante.model';
import { type IComprobante } from '@/shared/model/comprobante.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';
import type { IVentaHistorial } from '@/shared/model/venta-historial.model';

import VentaService from './venta.service';
import PagoService from '@/entities/pago/pago.service';
import ComprobanteService from '@/entities/comprobante/comprobante.service';
import OperationalTraceCard from '@/shared/components/OperationalTraceCard.vue';

export default defineComponent({
  name: 'VentaDetails',
  components: {
    OperationalTraceCard,
  },
  setup() {
    const dateFormat = useDateFormat();
    const formatDateLong = (value?: Date | string | null): string => {
      if (!value) return 'No disponible';
      if (typeof dateFormat.formatDateLong === 'function') {
        return dateFormat.formatDateLong(value);
      }
      return new Date(value).toLocaleString('es-AR');
    };
    const ventaService       = inject('ventaService',       () => new VentaService());
    const pagoService        = inject('pagoService',        () => new PagoService());
    const comprobanteService = inject('comprobanteService', () => new ComprobanteService());
    const alertService       = inject('alertService',       () => useAlertService(), true);

    const route  = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

    const venta: Ref<IVenta>              = ref({});
    const detalles: Ref<Array<{ id: number | string; vehiculo: any; precioUnitario: number; subtotal: number }>> = ref([]);
    const pagos: Ref<IPago[]>             = ref([]);
    const comprobantes: Ref<IComprobante[]> = ref([]);
    const historial: Ref<IVentaHistorial[]> = ref([]);

    const loadingDetalles = ref(false);
    const loadingPagos       = ref(false);
    const loadingComprobantes = ref(false);
    const loadingHistorial = ref(false);

    const totalPagado = computed(() =>
      pagos.value.filter(p => p.estado !== EstadoPago.ANULADO).reduce((sum, p) => sum + Number(p.monto ?? 0), 0),
    );

    const ventaStatusLabel = computed(() => labelEstado(venta.value.estado));
    const ventaMonedaDisplay = computed(() => {
      return (
        venta.value.moneda ??
        pagos.value.find(pago => pago.moneda)?.moneda ??
        comprobantes.value.find(comprobante => comprobante.moneda)?.moneda ??
        null
      );
    });
    const traceCreatedBy = computed(() => venta.value.createdBy ?? venta.value.user?.login ?? 'No disponible');
    const traceUpdatedBy = computed(() => venta.value.lastModifiedBy ?? venta.value.user?.login ?? 'No disponible');
    const traceLastAction = computed(() => {
      if (comprobantes.value.length > 0) return 'Comprobante emitido';
      if (pagos.value.length > 0) return 'Pago registrado';
      if (venta.value.estado === EstadoVenta.PAGADA) return 'Venta confirmada';
      if (venta.value.estado === EstadoVenta.FINALIZADA) return 'Venta finalizada';
      if (venta.value.estado === EstadoVenta.RESERVADA) return 'Venta reservada';
      if (venta.value.lastModifiedDate && venta.value.createdDate && venta.value.lastModifiedDate !== venta.value.createdDate) {
        return 'Venta actualizada';
      }
      return 'Venta creada';
    });
    const traceLastActionDate = computed(() => {
      const latestPago = pagos.value
        .map(pago => pago.fecha)
        .filter(Boolean)
        .sort((a, b) => new Date(b as Date).getTime() - new Date(a as Date).getTime())[0];

      const latestComprobante = comprobantes.value
        .map(comprobante => comprobante.fechaEmision)
        .filter(Boolean)
        .sort((a, b) => new Date(b as Date).getTime() - new Date(a as Date).getTime())[0];

      const actionDate = latestComprobante ?? latestPago ?? venta.value.lastModifiedDate ?? venta.value.createdDate;
      return actionDate ? formatDateLong(actionDate) : 'No disponible';
    });

    const retrieveVenta = async (ventaId: any) => {
      try {
        venta.value = await ventaService().find(ventaId);
        detalles.value = venta.value.vehiculo
          ? [{
              id: `vehiculo-${venta.value.vehiculo.id}`,
              vehiculo: venta.value.vehiculo,
              precioUnitario: Number(venta.value.precioBaseVehiculo ?? venta.value.vehiculo.precio ?? 0),
              subtotal: Number(venta.value.importeConvertido ?? venta.value.importeNeto ?? 0),
            }]
          : [];
        await Promise.all([
          cargarPagos(ventaId),
          cargarComprobantes(ventaId),
          cargarHistorial(ventaId),
        ]);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const cargarPagos = async (ventaId: any) => {
      loadingPagos.value = true;
      try {
        pagos.value = await pagoService().findByVentaId(Number(ventaId));
      } catch { pagos.value = []; }
      finally { loadingPagos.value = false; }
    };

    const cargarComprobantes = async (ventaId: any) => {
      loadingComprobantes.value = true;
      try {
        comprobantes.value = await comprobanteService().findByVentaId(Number(ventaId));
      } catch { comprobantes.value = []; }
      finally { loadingComprobantes.value = false; }
    };

    const cargarHistorial = async (ventaId: any) => {
      loadingHistorial.value = true;
      try {
        historial.value = await ventaService().historial(Number(ventaId));
      } catch {
        historial.value = [];
      } finally {
        loadingHistorial.value = false;
      }
    };

    const anularComprobante = async (comprobanteId: number) => {
      if (!venta.value.id) return;
      try {
        await comprobanteService().anular(comprobanteId);
        await cargarComprobantes(venta.value.id);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.ventaId) {
      retrieveVenta(route.params.ventaId);
    }

    function formatPrecio(valor?: number | null): string {
      return Number(valor ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function formatCotizacion(cotizacion?: number | null): string {
      return Number(cotizacion ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 8 });
    }

    function formatFecha(fecha?: Date | string | null): string {
      if (!fecha) return '—';
      return new Date(fecha).toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' });
    }

    function labelEstadoComprobante(estado?: string | null): string {
      const map: Record<string, string> = {
        [EstadoComprobante.EMITIDO]: 'Emitido',
        [EstadoComprobante.ANULADO]: 'Anulado',
      };
      return map[estado as string] ?? estado ?? '-';
    }

    function badgeEstado(estado?: string | null): string {
      const map: Record<string, string> = {
        [EstadoVenta.PENDIENTE]:  'bg-warning text-dark',
        [EstadoVenta.PAGADA]:     'bg-success',
        [EstadoVenta.CANCELADA]:  'bg-danger',
        [EstadoVenta.RESERVADA]:  'bg-info',
        [EstadoVenta.FINALIZADA]: 'bg-primary',
      };
      return map[estado as string] ?? 'bg-light text-dark border';
    }

    function labelEstado(estado?: string | null): string {
      const map: Record<string, string> = {
        [EstadoVenta.PENDIENTE]:  'Pendiente',
        [EstadoVenta.PAGADA]:     'Pagada',
        [EstadoVenta.CANCELADA]:  'Cancelada',
        [EstadoVenta.RESERVADA]:  'Reservada',
        [EstadoVenta.FINALIZADA]: 'Finalizada',
      };
      return map[estado as string] ?? estado ?? '—';
    }

    return {
      ...dateFormat,
      formatDateLong,
      alertService,
      venta,
      detalles,
      pagos,
      comprobantes,
      historial,
      totalPagado,
      ventaStatusLabel,
      ventaMonedaDisplay,
      traceCreatedBy,
      traceUpdatedBy,
      traceLastAction,
      traceLastActionDate,
      loadingDetalles,
      loadingPagos,
      loadingComprobantes,
      loadingHistorial,
      previousState,
      formatPrecio,
      formatCotizacion,
      formatFecha,
      badgeEstado,
      labelEstado,
      labelEstadoComprobante,
      anularComprobante,
    };
  },
});
