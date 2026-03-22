import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type IPrueba1 } from '@/shared/model/prueba-1.model';

import Prueba1Service from './prueba-1.service';

export default defineComponent({
  name: 'Prueba1Details',
  setup() {
    const prueba1Service = inject('prueba1Service', () => new Prueba1Service());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const prueba1: Ref<IPrueba1> = ref({});

    const retrievePrueba1 = async prueba1Id => {
      try {
        const res = await prueba1Service().find(prueba1Id);
        prueba1.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.prueba1Id) {
      retrievePrueba1(route.params.prueba1Id);
    }

    return {
      alertService,
      prueba1,

      previousState,
    };
  },
});
