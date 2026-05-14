import { computed, defineComponent, inject, onMounted, ref, type Ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDateFormat } from '@/shared/composables';
import { useAlertService } from '@/shared/alert/alert.service';
import type { IContratoPlanAhorro } from '@/shared/model/contrato-plan-ahorro.model';
import type { ICuotaPlanAhorro } from '@/shared/model/cuota-plan-ahorro.model';
import type { IInventario } from '@/shared/model/inventario.model';
import type { IAdjudicacionPlanAhorro } from '@/shared/model/adjudicacion-plan-ahorro.model';
import ContratoPlanAhorroService from './contrato-plan-ahorro.service';
import ComprobantePlanAhorroService from './comprobante-plan-ahorro.service';
import AdjudicacionPlanAhorroService from './adjudicacion-plan-ahorro.service';

export default defineComponent({
  name: 'ContratoPlanAhorroDetails',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const { formatDateShort } = useDateFormat();
    const alertService = inject('alertService', () => useAlertService(), true);
    const contratoService = inject('contratoPlanAhorroService', () => new ContratoPlanAhorroService());
    const comprobantePlanService = inject('comprobantePlanAhorroService', () => new ComprobantePlanAhorroService());
    const adjudicacionService = inject('adjudicacionPlanAhorroService', () => new AdjudicacionPlanAhorroService());

    const contrato: Ref<IContratoPlanAhorro | null> = ref(null);
    const cuotas: Ref<ICuotaPlanAhorro[]> = ref([]);
    const adjudicacion: Ref<IAdjudicacionPlanAhorro | null> = ref(null);
    const inventariosCompatibles: Ref<IInventario[]> = ref([]);
    const showPay = ref(false);
    const cuotaActiva: Ref<ICuotaPlanAhorro | null> = ref(null);
    const pagoMonto = ref(0);
    const pagoObservaciones = ref('');
    const adjudicacionObservaciones = ref('');
    const inventarioSeleccionadoId = ref<number | null>(null);
    const loadingAdjudicacion = ref(false);
    const cuotaSeleccionadaId = ref<number | null>(null);

    const contratoId = () => Number(route.params.contratoId);

    const load = async () => {
      contrato.value = await contratoService().find(contratoId());
      cuotas.value = await contratoService().cuotas(contratoId());
      adjudicacion.value = await adjudicacionService().findByContrato(contratoId());
      inventariosCompatibles.value = await adjudicacionService().inventariosCompatibles(contratoId());
      inventarioSeleccionadoId.value = adjudicacion.value?.inventario?.id ?? null;
      if (!cuotaSeleccionadaId.value && cuotas.value.length > 0) {
        cuotaSeleccionadaId.value = cuotas.value[0].id ?? null;
      } else if (cuotaSeleccionadaId.value && !cuotas.value.some(item => item.id === cuotaSeleccionadaId.value)) {
        cuotaSeleccionadaId.value = cuotas.value[0]?.id ?? null;
      }
    };

    const cuotasPagadasCount = computed(() => cuotas.value.filter(cuota => cuota.estado === 'PAGADA').length);
    const cuotasPendientesCount = computed(() => cuotas.value.filter(cuota => cuota.estado === 'PENDIENTE' || cuota.estado === 'VENCIDA').length);
    const cuotaSeleccionada = computed(() => cuotas.value.find(cuota => cuota.id === cuotaSeleccionadaId.value) ?? null);

    const abrirPago = (cuota: ICuotaPlanAhorro) => {
      cuotaActiva.value = cuota;
      pagoMonto.value = Number(cuota.importe ?? 0);
      pagoObservaciones.value = '';
      showPay.value = true;
    };

    const confirmarPago = async () => {
      if (!cuotaActiva.value?.id) return;
      try {
        await contratoService().pagarCuota(cuotaActiva.value.id, pagoMonto.value, pagoObservaciones.value);
        showPay.value = false;
        await load();
        alertService.showInfo('Cuota pagada correctamente');
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const descargarComprobante = async (cuota: ICuotaPlanAhorro) => {
      if (!cuota.comprobantePlanAhorroId) return;
      try {
        await comprobantePlanService().descargarPdf(cuota.comprobantePlanAhorroId);
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const anularComprobante = async (cuota: ICuotaPlanAhorro) => {
      if (!cuota.comprobantePlanAhorroId) return;
      const motivo = window.prompt('Motivo de anulación del comprobante');
      if (!motivo || !motivo.trim()) return;
      try {
        await comprobantePlanService().anular(cuota.comprobantePlanAhorroId, motivo.trim());
        await load();
        alertService.showInfo('Comprobante anulado');
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const puedeAdjudicar = computed(() =>
      contrato.value?.estado === 'ACTIVO' || contrato.value?.estado === 'EN_MORA' || contrato.value?.estado === 'ADJUDICADO',
    );

    const adjudicarContrato = async () => {
      if (!contrato.value?.id) return;
      loadingAdjudicacion.value = true;
      try {
        adjudicacion.value = await adjudicacionService().adjudicarContrato(contrato.value.id, adjudicacionObservaciones.value);
        alertService.showInfo('Contrato adjudicado correctamente');
        await load();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        loadingAdjudicacion.value = false;
      }
    };

    const asignarInventario = async () => {
      if (!adjudicacion.value?.id || !inventarioSeleccionadoId.value) return;
      loadingAdjudicacion.value = true;
      try {
        adjudicacion.value = await adjudicacionService().asignarInventario(adjudicacion.value.id, inventarioSeleccionadoId.value);
        alertService.showInfo('Inventario asignado a la adjudicación');
        await load();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        loadingAdjudicacion.value = false;
      }
    };

    const generarVentaDesdeAdjudicacion = async () => {
      if (!adjudicacion.value?.id) return;
      loadingAdjudicacion.value = true;
      try {
        adjudicacion.value = await adjudicacionService().generarVenta(adjudicacion.value.id);
        alertService.showInfo('Venta generada desde adjudicación');
        if (adjudicacion.value.venta?.id) {
          await router.push({ name: 'VentaView', params: { ventaId: adjudicacion.value.venta.id } });
          return;
        }
        await load();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        loadingAdjudicacion.value = false;
      }
    };

    onMounted(async () => {
      try {
        await load();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    });

    return {
      contrato,
      cuotas,
      cuotasPagadasCount,
      cuotasPendientesCount,
      cuotaSeleccionadaId,
      cuotaSeleccionada,
      formatDateShort,
      showPay,
      cuotaActiva,
      pagoMonto,
      pagoObservaciones,
      abrirPago,
      confirmarPago,
      descargarComprobante,
      anularComprobante,
      adjudicacion,
      inventariosCompatibles,
      adjudicacionObservaciones,
      inventarioSeleccionadoId,
      loadingAdjudicacion,
      puedeAdjudicar,
      adjudicarContrato,
      asignarInventario,
      generarVentaDesdeAdjudicacion,
    };
  },
});
