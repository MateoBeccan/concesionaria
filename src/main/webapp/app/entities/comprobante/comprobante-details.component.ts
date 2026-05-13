import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IComprobante } from '@/shared/model/comprobante.model';

import ComprobanteService from './comprobante.service';

export default defineComponent({
  name: 'ComprobanteDetails',
  setup() {
    const dateFormat = useDateFormat();
    const comprobanteService = inject('comprobanteService', () => new ComprobanteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const comprobante: Ref<IComprobante> = ref({});

    const retrieveComprobante = async comprobanteId => {
      try {
        const res = await comprobanteService().find(comprobanteId);
        comprobante.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    const descargarPdf = async () => {
      if (!comprobante.value?.id) return;
      try {
        await comprobanteService().descargarPdf(comprobante.value.id);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.comprobanteId) {
      retrieveComprobante(route.params.comprobanteId);
    }

    const formatMoney = (value?: number | null) =>
      Number(value ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    const estadoLabel = (estado?: string | null) => {
      if (estado === 'EMITIDO') return 'Emitido';
      if (estado === 'ANULADO') return 'Anulado';
      return estado ?? '-';
    };

    const estadoClass = (estado?: string | null) => (estado === 'ANULADO' ? 'bg-secondary' : 'bg-success');

    return {
      ...dateFormat,
      alertService,
      comprobante,
      descargarPdf,
      formatMoney,
      estadoLabel,
      estadoClass,

      previousState,
    };
  },
});
