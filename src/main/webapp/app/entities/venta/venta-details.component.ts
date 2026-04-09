import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IVenta } from '@/shared/model/venta.model';
import { type IDetalleVenta } from '@/shared/model/detalle-venta.model';
import { type IPago } from '@/shared/model/pago.model';
import { type IComprobante } from '@/shared/model/comprobante.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';

import VentaService from './venta.service';
import DetalleVentaService from '@/entities/detalle-venta/detalle-venta.service';
import PagoService from '@/entities/pago/pago.service';
import ComprobanteService from '@/entities/comprobante/comprobante.service';

export default defineComponent({
  name: 'VentaDetails',
  setup() {
    const dateFormat = useDateFormat();
    const ventaService       = inject('ventaService',       () => new VentaService());
    const detalleVentaService = inject('detalleVentaService', () => new DetalleVentaService());
    const pagoService        = inject('pagoService',        () => new PagoService());
    const comprobanteService = inject('comprobanteService', () => new ComprobanteService());
    const alertService       = inject('alertService',       () => useAlertService(), true);

    const route  = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

    const venta: Ref<IVenta>              = ref({});
    const detalles: Ref<IDetalleVenta[]>  = ref([]);
    const pagos: Ref<IPago[]>             = ref([]);
    const comprobantes: Ref<IComprobante[]> = ref([]);

    const loadingDetalles    = ref(false);
    const loadingPagos       = ref(false);
    const loadingComprobantes = ref(false);

    const totalPagado = computed(() =>
      pagos.value.reduce((sum, p) => sum + Number(p.monto ?? 0), 0),
    );

    const retrieveVenta = async (ventaId: any) => {
      try {
        venta.value = await ventaService().find(ventaId);
        await Promise.all([
          cargarDetalles(ventaId),
          cargarPagos(ventaId),
          cargarComprobantes(ventaId),
        ]);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const cargarDetalles = async (ventaId: any) => {
      loadingDetalles.value = true;
      try {
        const res = await detalleVentaService().retrieve({ 'ventaId.equals': ventaId, page: 0, size: 50 });
        detalles.value = res.data;
      } catch { detalles.value = []; }
      finally { loadingDetalles.value = false; }
    };

    const cargarPagos = async (ventaId: any) => {
      loadingPagos.value = true;
      try {
        const res = await pagoService().retrieve({ 'ventaId.equals': ventaId, page: 0, size: 50 });
        pagos.value = res.data;
      } catch { pagos.value = []; }
      finally { loadingPagos.value = false; }
    };

    const cargarComprobantes = async (ventaId: any) => {
      loadingComprobantes.value = true;
      try {
        const res = await comprobanteService().retrieve({ 'ventaId.equals': ventaId, page: 0, size: 50 });
        comprobantes.value = res.data;
      } catch { comprobantes.value = []; }
      finally { loadingComprobantes.value = false; }
    };

    if (route.params?.ventaId) {
      retrieveVenta(route.params.ventaId);
    }

    function formatPrecio(valor?: number | null): string {
      return Number(valor ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function formatFecha(fecha?: Date | string | null): string {
      if (!fecha) return '—';
      return new Date(fecha).toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' });
    }

    function badgeEstado(estado?: string | null): string {
      const map: Record<string, string> = {
        [EstadoVenta.PENDIENTE]:  'bg-warning text-dark',
        [EstadoVenta.PAGADA]:     'bg-success',
        [EstadoVenta.CANCELADA]:  'bg-danger',
        [EstadoVenta.RESERVADA]:  'bg-info',
      };
      return map[estado as string] ?? 'bg-light text-dark border';
    }

    function labelEstado(estado?: string | null): string {
      const map: Record<string, string> = {
        [EstadoVenta.PENDIENTE]:  'Pendiente',
        [EstadoVenta.PAGADA]:     'Pagada',
        [EstadoVenta.CANCELADA]:  'Cancelada',
        [EstadoVenta.RESERVADA]:  'Reservada',
      };
      return map[estado as string] ?? estado ?? '—';
    }

    return {
      ...dateFormat,
      alertService,
      venta,
      detalles,
      pagos,
      comprobantes,
      totalPagado,
      loadingDetalles,
      loadingPagos,
      loadingComprobantes,
      previousState,
      formatPrecio,
      formatFecha,
      badgeEstado,
      labelEstado,
    };
  },
});
