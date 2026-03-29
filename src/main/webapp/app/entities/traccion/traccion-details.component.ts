import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type ITraccion } from '@/shared/model/traccion.model';

import TraccionService from './traccion.service';

export default defineComponent({
  name: 'TraccionDetails',
  setup() {
    const traccionService = inject('traccionService', () => new TraccionService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const traccion: Ref<ITraccion> = ref({});

    const retrieveTraccion = async traccionId => {
      try {
        const res = await traccionService().find(traccionId);
        traccion.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.traccionId) {
      retrieveTraccion(route.params.traccionId);
    }

    return {
      alertService,
      traccion,

      previousState,
    };
  },
});
