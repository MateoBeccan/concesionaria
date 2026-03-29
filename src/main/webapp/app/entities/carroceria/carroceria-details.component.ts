import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type ICarroceria } from '@/shared/model/carroceria.model';

import CarroceriaService from './carroceria.service';

export default defineComponent({
  name: 'CarroceriaDetails',
  setup() {
    const carroceriaService = inject('carroceriaService', () => new CarroceriaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const carroceria: Ref<ICarroceria> = ref({});

    const retrieveCarroceria = async carroceriaId => {
      try {
        const res = await carroceriaService().find(carroceriaId);
        carroceria.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.carroceriaId) {
      retrieveCarroceria(route.params.carroceriaId);
    }

    return {
      alertService,
      carroceria,

      previousState,
    };
  },
});
