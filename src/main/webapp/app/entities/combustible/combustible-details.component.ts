import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type ICombustible } from '@/shared/model/combustible.model';

import CombustibleService from './combustible.service';

export default defineComponent({
  name: 'CombustibleDetails',
  setup() {
    const combustibleService = inject('combustibleService', () => new CombustibleService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const combustible: Ref<ICombustible> = ref({});

    const retrieveCombustible = async combustibleId => {
      try {
        const res = await combustibleService().find(combustibleId);
        combustible.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.combustibleId) {
      retrieveCombustible(route.params.combustibleId);
    }

    return {
      alertService,
      combustible,

      previousState,
    };
  },
});
