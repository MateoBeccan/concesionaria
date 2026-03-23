import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type IMetodoPago } from '@/shared/model/metodo-pago.model';

import MetodoPagoService from './metodo-pago.service';

export default defineComponent({
  name: 'MetodoPagoDetails',
  setup() {
    const metodoPagoService = inject('metodoPagoService', () => new MetodoPagoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const metodoPago: Ref<IMetodoPago> = ref({});

    const retrieveMetodoPago = async metodoPagoId => {
      try {
        const res = await metodoPagoService().find(metodoPagoId);
        metodoPago.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.metodoPagoId) {
      retrieveMetodoPago(route.params.metodoPagoId);
    }

    return {
      alertService,
      metodoPago,

      previousState,
    };
  },
});
