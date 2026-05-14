import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IComprobante } from '@/shared/model/comprobante.model';
import { EstadoComprobante } from '@/shared/model/estado-comprobante.model';
import { EstadoPago } from '@/shared/model/estado-pago.model';
import { type IPago } from '@/shared/model/pago.model';
import { type ITasacionUsado } from '@/shared/model/tasacion-usado.model';
import { type IVenta } from '@/shared/model/venta.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';
import type { IVentaHistorial } from '@/shared/model/venta-historial.model';
import type { IEntregaChecklistItem } from '@/shared/model/entrega-checklist-item.model';
import type { IEntregaUnidad } from '@/shared/model/entrega-unidad.model';
import { EstadoEntregaUnidad } from '@/shared/model/enumerations/estado-entrega-unidad.model';

import ComprobanteService from '@/entities/comprobante/comprobante.service';
import EntregaUnidadService from '@/entities/entrega-unidad/entrega-unidad.service';
import PagoService from '@/entities/pago/pago.service';
import TasacionUsadoService from '@/entities/tasacion-usado/tasacion-usado.service';
import OperationalTraceCard from '@/shared/components/OperationalTraceCard.vue';
import VentaService from './venta.service';

type TimelineTone = 'primary' | 'success' | 'warning' | 'danger' | 'muted';
type TimelineEvent = { key: string; fecha: Date | string | null; titulo: string; detalle?: string; tono: TimelineTone };

export default defineComponent({
  name: 'VentaDetails',
  components: { OperationalTraceCard },
  setup() {
    const dateFormat = useDateFormat();
    const ventaService = inject('ventaService', () => new VentaService());
    const pagoService = inject('pagoService', () => new PagoService());
    const comprobanteService = inject('comprobanteService', () => new ComprobanteService());
    const tasacionUsadoService = inject('tasacionUsadoService', () => new TasacionUsadoService());
    const entregaUnidadService = inject('entregaUnidadService', () => new EntregaUnidadService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

    const venta: Ref<IVenta> = ref({});
    const detalles: Ref<Array<{ id: number | string; vehiculo: any; precioUnitario: number; subtotal: number }>> = ref([]);
    const pagos: Ref<IPago[]> = ref([]);
    const comprobantes: Ref<IComprobante[]> = ref([]);
    const historial: Ref<IVentaHistorial[]> = ref([]);
    const tasacionUsadaCargada: Ref<ITasacionUsado | null> = ref(null);
    const entregaUnidad: Ref<IEntregaUnidad | null> = ref(null);
    const loadingEntrega = ref(false);
    const showProgramarEntrega = ref(false);
    const fechaProgramada = ref('');
    const observacionesEntrega = ref('');
    const showConfirmarEntrega = ref(false);
    const kilometrajeEntrega = ref<number | null>(null);
    const nivelCombustible = ref('');
    const observacionesConfirmacion = ref('');

    const loadingPagos = ref(false);
    const loadingComprobantes = ref(false);
    const mostrarModalAnulacionComprobante = ref(false);
    const comprobanteAAnular = ref<number | null>(null);
    const motivoAnulacionComprobante = ref('');
    const mostrarModalAnulacionPago = ref(false);
    const pagoAAnular = ref<number | null>(null);
    const motivoAnulacionPago = ref('');

    const ventaMonedaDisplay = computed(() => {
      return venta.value.moneda ?? pagos.value.find(p => p.moneda)?.moneda ?? comprobantes.value.find(c => c.moneda)?.moneda ?? null;
    });

    const totalPagado = computed(() =>
      pagos.value.filter(p => p.estado !== EstadoPago.ANULADO).reduce((sum, p) => sum + Number(p.montoAplicadoVenta ?? p.monto ?? 0), 0),
    );

    const tieneComprobanteActivo = computed(() => comprobantes.value.some(c => c.estado === EstadoComprobante.EMITIDO));
    const puedeRegistrarPago = computed(() => venta.value.estado !== EstadoVenta.CANCELADA && Number(venta.value.saldo ?? 0) > 0);
    const puedeEmitirComprobante = computed(() =>
      venta.value.estado !== EstadoVenta.CANCELADA &&
      Number(venta.value.total ?? 0) > 0 &&
      (venta.value.estado === EstadoVenta.PAGADA || venta.value.estado === EstadoVenta.FINALIZADA) &&
      !tieneComprobanteActivo.value,
    );
    const pagosAnulables = computed(() => pagos.value.filter(p => p.estado === EstadoPago.REGISTRADO && p.id));
    const puedeAnularPago = computed(() => pagosAnulables.value.length > 0 && venta.value.estado !== EstadoVenta.CANCELADA);

    const pagosPlanAhorro = computed(() =>
      pagos.value.filter(p => p.metodoPago?.codigo?.toUpperCase() === 'PLAN_AHORRO' && p.estado !== EstadoPago.ANULADO),
    );

    const creditoPlanAhorro = computed(() =>
      pagosPlanAhorro.value.reduce((sum, p) => sum + Number(p.montoAplicadoVenta ?? p.monto ?? 0), 0),
    );

    const diferenciaRestantePlan = computed(() => Math.max(Number(venta.value.total ?? 0) - creditoPlanAhorro.value, 0));

    const contratoPlanAsociado = computed(() => pagosPlanAhorro.value.find(p => p.contratoPlanAhorroId)?.contratoPlanAhorroId ?? null);

    const tasacionUsada = computed<ITasacionUsado | null>(() => {
      if (tasacionUsadaCargada.value) return tasacionUsadaCargada.value;
      if (venta.value.tasacionUsado) return venta.value.tasacionUsado;
      return pagos.value.find(p => p.tasacionUsado)?.tasacionUsado ?? null;
    });

    const tasacionUsadaId = computed<number | null>(() => {
      if (tasacionUsada.value?.id) return tasacionUsada.value.id;
      if (venta.value.tasacionUsado?.id) return venta.value.tasacionUsado.id;
      const fromPago = pagos.value.find(p => p.tasacionUsado?.id || p.tasacionUsadoId);
      return fromPago?.tasacionUsado?.id ?? fromPago?.tasacionUsadoId ?? null;
    });

    const timeline = computed<TimelineEvent[]>(() => {
      const events: TimelineEvent[] = [];
      if (venta.value.createdDate) {
        events.push({
          key: `venta-creada-${venta.value.id ?? 'x'}`,
          fecha: venta.value.createdDate,
          titulo: 'Venta creada',
          detalle: venta.value.createdBy ? `Registrada por ${venta.value.createdBy}` : undefined,
          tono: 'primary',
        });
      }

      historial.value.forEach(h => {
        events.push({
          key: `historial-${h.id ?? Math.random()}`,
          fecha: h.fecha ?? null,
          titulo: h.accion ?? 'Actualizacion de venta',
          detalle: h.detalle ?? (h.usuario ? `Usuario: ${h.usuario}` : undefined),
          tono: h.estadoNuevo === EstadoVenta.CANCELADA ? 'danger' : h.estadoNuevo === EstadoVenta.PAGADA ? 'success' : 'muted',
        });
      });

      pagos.value.forEach(p => {
        const esAnulado = p.estado === EstadoPago.ANULADO;
        events.push({
          key: `pago-${p.id ?? Math.random()}`,
          fecha: p.fecha ?? p.createdDate ?? null,
          titulo: esAnulado ? 'Pago anulado' : 'Pago registrado',
          detalle: `${p.metodoPago?.codigo ?? 'METODO'} - ${formatPrecio(p.monto)} ${p.moneda?.codigo ?? ''}`,
          tono: esAnulado ? 'warning' : 'success',
        });
      });

      comprobantes.value.forEach(c => {
        events.push({
          key: `comp-${c.id ?? Math.random()}`,
          fecha: c.fechaEmision ?? c.createdDate ?? null,
          titulo: c.estado === EstadoComprobante.ANULADO ? 'Comprobante anulado' : 'Comprobante emitido',
          detalle: `${c.numeroComprobante ?? 'Sin numero'} - ${c.tipoComprobante?.codigo ?? ''}`,
          tono: c.estado === EstadoComprobante.ANULADO ? 'warning' : 'primary',
        });
      });

      if (tasacionUsada.value?.inventarioGenerado?.id) {
        events.push({
          key: `inventario-usado-${tasacionUsada.value.id ?? 'x'}`,
          fecha: venta.value.lastModifiedDate ?? venta.value.fecha ?? null,
          titulo: 'Ingreso de usado a inventario',
          detalle: `Inventario #${tasacionUsada.value.inventarioGenerado.id}`,
          tono: 'success',
        });
      }

      return events.sort((a, b) => parseDateSafe(b.fecha) - parseDateSafe(a.fecha));
    });

    const retrieveVenta = async (ventaId: any) => {
      try {
        venta.value = await ventaService().find(ventaId);
        detalles.value = venta.value.vehiculo
          ? [
              {
                id: `vehiculo-${venta.value.vehiculo.id}`,
                vehiculo: venta.value.vehiculo,
                precioUnitario: Number(venta.value.precioBaseVehiculo ?? venta.value.vehiculo.precio ?? 0),
                subtotal: Number(venta.value.importeConvertido ?? venta.value.importeNeto ?? 0),
              },
            ]
          : [];
        await Promise.all([cargarPagos(ventaId), cargarComprobantes(ventaId), cargarHistorial(ventaId), cargarEntrega(ventaId)]);
        await cargarTasacionUsadaSiFalta();
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const cargarEntrega = async (ventaId: any) => {
      loadingEntrega.value = true;
      try {
        entregaUnidad.value = await entregaUnidadService().findByVenta(Number(ventaId));
      } catch {
        entregaUnidad.value = null;
      } finally {
        loadingEntrega.value = false;
      }
    };

    const cargarPagos = async (ventaId: any) => {
      loadingPagos.value = true;
      try {
        pagos.value = await pagoService().findByVentaId(Number(ventaId));
      } catch {
        pagos.value = [];
      } finally {
        loadingPagos.value = false;
      }
    };

    const cargarComprobantes = async (ventaId: any) => {
      loadingComprobantes.value = true;
      try {
        comprobantes.value = await comprobanteService().findByVentaId(Number(ventaId));
      } catch {
        comprobantes.value = [];
      } finally {
        loadingComprobantes.value = false;
      }
    };

    const cargarHistorial = async (ventaId: any) => {
      try {
        historial.value = await ventaService().historial(Number(ventaId));
      } catch {
        historial.value = [];
      }
    };

    const cargarTasacionUsadaSiFalta = async () => {
      const id = tasacionUsadaId.value;
      if (!id) return;
      if (tasacionUsada.value?.patenteUsado || tasacionUsada.value?.marcaModeloUsado) return;
      try {
        tasacionUsadaCargada.value = await tasacionUsadoService().find(id);
      } catch {
        tasacionUsadaCargada.value = tasacionUsada.value;
      }
    };

    const anularComprobante = async (comprobanteId: number, motivo: string) => {
      if (!venta.value.id) return;
      try {
        await comprobanteService().anular(comprobanteId, motivo);
        await cargarComprobantes(venta.value.id);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const anularPago = async (pagoId: number, motivo: string) => {
      if (!venta.value.id) return;
      try {
        await pagoService().anular(pagoId, motivo);
        await Promise.all([cargarPagos(venta.value.id), retrieveVenta(venta.value.id)]);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const abrirModalAnulacionComprobante = (comprobanteId: number) => {
      comprobanteAAnular.value = comprobanteId;
      motivoAnulacionComprobante.value = '';
      mostrarModalAnulacionComprobante.value = true;
    };
    const cerrarModalAnulacionComprobante = () => {
      mostrarModalAnulacionComprobante.value = false;
      comprobanteAAnular.value = null;
      motivoAnulacionComprobante.value = '';
    };
    const confirmarAnulacionComprobante = async () => {
      if (!comprobanteAAnular.value || !motivoAnulacionComprobante.value.trim()) return;
      await anularComprobante(comprobanteAAnular.value, motivoAnulacionComprobante.value.trim());
      cerrarModalAnulacionComprobante();
    };

    const abrirModalAnulacionPago = (pagoId: number) => {
      pagoAAnular.value = pagoId;
      motivoAnulacionPago.value = '';
      mostrarModalAnulacionPago.value = true;
    };
    const cerrarModalAnulacionPago = () => {
      mostrarModalAnulacionPago.value = false;
      pagoAAnular.value = null;
      motivoAnulacionPago.value = '';
    };
    const confirmarAnulacionPago = async () => {
      if (!pagoAAnular.value || !motivoAnulacionPago.value.trim()) return;
      await anularPago(pagoAAnular.value, motivoAnulacionPago.value.trim());
      cerrarModalAnulacionPago();
    };

    const descargarPdfComprobante = async (comprobanteId?: number) => {
      if (!comprobanteId) return;
      try {
        await comprobanteService().descargarPdf(comprobanteId);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const programarEntrega = async () => {
      if (!venta.value.id || !fechaProgramada.value) return;
      try {
        await entregaUnidadService().programarEntrega(venta.value.id, new Date(`${fechaProgramada.value}T12:00:00`).toISOString(), observacionesEntrega.value);
        showProgramarEntrega.value = false;
        fechaProgramada.value = '';
        observacionesEntrega.value = '';
        await cargarEntrega(venta.value.id);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const guardarChecklist = async () => {
      if (!entregaUnidad.value?.id || !entregaUnidad.value.checklist) return;
      try {
        entregaUnidad.value = await entregaUnidadService().actualizarChecklist(entregaUnidad.value.id, entregaUnidad.value.checklist as IEntregaChecklistItem[]);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const confirmarEntrega = async () => {
      if (!entregaUnidad.value?.id) return;
      try {
        entregaUnidad.value = await entregaUnidadService().confirmarEntrega(
          entregaUnidad.value.id,
          kilometrajeEntrega.value,
          nivelCombustible.value || null,
          observacionesConfirmacion.value || null,
        );
        showConfirmarEntrega.value = false;
        if (venta.value.id) {
          await retrieveVenta(venta.value.id);
        }
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const cancelarEntrega = async () => {
      if (!entregaUnidad.value?.id) return;
      try {
        entregaUnidad.value = await entregaUnidadService().cancelarEntrega(entregaUnidad.value.id, 'Cancelada desde detalle de venta');
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const checklistObligatorioCompleto = computed(() => {
      const items = entregaUnidad.value?.checklist ?? [];
      return items.filter(i => i.obligatorio).every(i => i.completado);
    });

    if (route.params?.ventaId) retrieveVenta(route.params.ventaId);

    function parseDateSafe(value?: Date | string | null): number {
      if (!value) return 0;
      return new Date(value).getTime();
    }

    function formatPrecio(valor?: number | null): string {
      return Number(valor ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function formatCotizacion(cotizacion?: number | null): string {
      if (cotizacion === null || cotizacion === undefined) return '-';
      return Number(cotizacion).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 8 });
    }

    function formatFecha(fecha?: Date | string | null): string {
      if (!fecha) return '-';
      return new Date(fecha).toLocaleString('es-AR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    }

    function badgeEstado(estado?: string | null): string {
      const map: Record<string, string> = {
        [EstadoVenta.PENDIENTE]: 'bg-warning text-dark',
        [EstadoVenta.PAGADA]: 'bg-success',
        [EstadoVenta.CANCELADA]: 'bg-danger',
        [EstadoVenta.RESERVADA]: 'bg-info text-dark',
        [EstadoVenta.FINALIZADA]: 'bg-primary',
      };
      return map[estado as string] ?? 'bg-light text-dark border';
    }

    function labelEstado(estado?: string | null): string {
      const map: Record<string, string> = {
        [EstadoVenta.PENDIENTE]: 'Pendiente',
        [EstadoVenta.PAGADA]: 'Pagada',
        [EstadoVenta.CANCELADA]: 'Cancelada',
        [EstadoVenta.RESERVADA]: 'Reservada',
        [EstadoVenta.FINALIZADA]: 'Finalizada',
      };
      return map[estado as string] ?? estado ?? '-';
    }

    function esPagoPlanAhorro(pago?: IPago | null): boolean {
      return pago?.metodoPago?.codigo?.toUpperCase() === 'PLAN_AHORRO';
    }

    function labelEstadoComprobante(estado?: string | null): string {
      const map: Record<string, string> = {
        [EstadoComprobante.EMITIDO]: 'Emitido',
        [EstadoComprobante.ANULADO]: 'Anulado',
      };
      return map[estado as string] ?? estado ?? '-';
    }

    function timelineToneClass(tono: TimelineTone): string {
      const map: Record<TimelineTone, string> = {
        primary: 'text-primary',
        success: 'text-success',
        warning: 'text-warning',
        danger: 'text-danger',
        muted: 'text-muted',
      };
      return map[tono];
    }

    return {
      ...dateFormat,
      venta,
      detalles,
      pagos,
      comprobantes,
      totalPagado,
      ventaMonedaDisplay,
      historial,
      timeline,
      tasacionUsada,
      tasacionUsadaId,
      loadingPagos,
      loadingComprobantes,
      mostrarModalAnulacionComprobante,
      motivoAnulacionComprobante,
      mostrarModalAnulacionPago,
      motivoAnulacionPago,
      puedeRegistrarPago,
      puedeEmitirComprobante,
      puedeAnularPago,
      creditoPlanAhorro,
      diferenciaRestantePlan,
      contratoPlanAsociado,
      previousState,
      formatPrecio,
      formatCotizacion,
      formatFecha,
      badgeEstado,
      labelEstado,
      labelEstadoComprobante,
      esPagoPlanAhorro,
      timelineToneClass,
      entregaUnidad,
      loadingEntrega,
      showProgramarEntrega,
      fechaProgramada,
      observacionesEntrega,
      showConfirmarEntrega,
      kilometrajeEntrega,
      nivelCombustible,
      observacionesConfirmacion,
      checklistObligatorioCompleto,
      EstadoEntregaUnidad,
      programarEntrega,
      guardarChecklist,
      confirmarEntrega,
      cancelarEntrega,
      descargarPdfComprobante,
      abrirModalAnulacionComprobante,
      cerrarModalAnulacionComprobante,
      confirmarAnulacionComprobante,
      abrirModalAnulacionPago,
      cerrarModalAnulacionPago,
      confirmarAnulacionPago,
    };
  },
});
