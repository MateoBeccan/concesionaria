import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type IAuto } from '@/shared/model/auto.model';

import AutoService from './auto.service';

export default defineComponent({
  name: 'AutoDetails',
  setup() {
    const autoService = inject('autoService', () => new AutoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const auto: Ref<IAuto> = ref({});

    const retrieveAuto = async autoId => {
      try {
        const res = await autoService().find(autoId);
        auto.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.autoId) {
      retrieveAuto(route.params.autoId);
    }

    return {
      alertService,
      auto,

      previousState,
    };
  },
});
