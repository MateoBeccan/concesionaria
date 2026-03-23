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

    return {
      ...dateFormat,
      alertService,
      pago,

      previousState,
    };
  },
});
