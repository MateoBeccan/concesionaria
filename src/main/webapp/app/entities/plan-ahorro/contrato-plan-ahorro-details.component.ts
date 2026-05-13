import { defineComponent, inject, onMounted, ref, type Ref } from 'vue';
import { useRoute } from 'vue-router';
import { useDateFormat } from '@/shared/composables';
import { useAlertService } from '@/shared/alert/alert.service';
import type { IContratoPlanAhorro } from '@/shared/model/contrato-plan-ahorro.model';
import type { ICuotaPlanAhorro } from '@/shared/model/cuota-plan-ahorro.model';
import ContratoPlanAhorroService from './contrato-plan-ahorro.service';
import ComprobantePlanAhorroService from './comprobante-plan-ahorro.service';

export default defineComponent({
  name: 'ContratoPlanAhorroDetails',
  setup() {
    const route = useRoute();
    const { formatDateShort } = useDateFormat();
    const alertService = inject('alertService', () => useAlertService(), true);
    const contratoService = inject('contratoPlanAhorroService', () => new ContratoPlanAhorroService());
    const comprobantePlanService = inject('comprobantePlanAhorroService', () => new ComprobantePlanAhorroService());

    const contrato: Ref<IContratoPlanAhorro | null> = ref(null);
    const cuotas: Ref<ICuotaPlanAhorro[]> = ref([]);
    const showPay = ref(false);
    const cuotaActiva: Ref<ICuotaPlanAhorro | null> = ref(null);
    const pagoMonto = ref(0);
    const pagoObservaciones = ref('');

    const contratoId = () => Number(route.params.contratoId);

    const load = async () => {
      contrato.value = await contratoService().find(contratoId());
      cuotas.value = await contratoService().cuotas(contratoId());
    };

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

    const descargarComprobante = (cuota: ICuotaPlanAhorro) => {
      if (!cuota.comprobantePlanAhorroId) return;
      comprobantePlanService().descargarPdf(cuota.comprobantePlanAhorroId);
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
      formatDateShort,
      showPay,
      cuotaActiva,
      pagoMonto,
      pagoObservaciones,
      abrirPago,
      confirmarPago,
      descargarComprobante,
      anularComprobante,
    };
  },
});
