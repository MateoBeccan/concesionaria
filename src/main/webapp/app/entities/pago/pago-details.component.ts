import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IPago } from '@/shared/model/pago.model';

import PagoService from './pago.service';

export default defineComponent({
  name: 'PagoDetails',
  setup() {
    const dateFormat = useDateFormat();
    const pagoService = inject('pagoService', () => new PagoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const pago: Ref<IPago> = ref({});

    const retrievePago = async pagoId => {
      try {
        const res = await pagoService().find(pagoId);
        pago.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.pagoId) {
      retrievePago(route.params.pagoId);
    }

    const formatMoney = (value?: number | null, currency = 'ARS') =>
      new Intl.NumberFormat('es-AR', {
        style: 'currency',
        currency,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }).format(Number(value ?? 0));

    const estadoLabel = (estado?: string) => (estado === 'ANULADO' ? 'Anulado' : 'Registrado');
    const estadoClass = (estado?: string) => (estado === 'ANULADO' ? 'bg-secondary-subtle text-secondary-emphasis' : 'bg-success-subtle text-success-emphasis');

    return {
      ...dateFormat,
      alertService,
      pago,
      formatMoney,
      estadoLabel,
      estadoClass,

      previousState,
    };
  },
});
