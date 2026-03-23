import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type ICondicionIva } from '@/shared/model/condicion-iva.model';

import CondicionIvaService from './condicion-iva.service';

export default defineComponent({
  name: 'CondicionIvaDetails',
  setup() {
    const condicionIvaService = inject('condicionIvaService', () => new CondicionIvaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const condicionIva: Ref<ICondicionIva> = ref({});

    const retrieveCondicionIva = async condicionIvaId => {
      try {
        const res = await condicionIvaService().find(condicionIvaId);
        condicionIva.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.condicionIvaId) {
      retrieveCondicionIva(route.params.condicionIvaId);
    }

    return {
      alertService,
      condicionIva,

      previousState,
    };
  },
});
