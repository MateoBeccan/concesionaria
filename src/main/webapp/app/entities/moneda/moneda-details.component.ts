import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type IMoneda } from '@/shared/model/moneda.model';

import MonedaService from './moneda.service';

export default defineComponent({
  name: 'MonedaDetails',
  setup() {
    const monedaService = inject('monedaService', () => new MonedaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const moneda: Ref<IMoneda> = ref({});

    const retrieveMoneda = async monedaId => {
      try {
        const res = await monedaService().find(monedaId);
        moneda.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.monedaId) {
      retrieveMoneda(route.params.monedaId);
    }

    return {
      alertService,
      moneda,

      previousState,
    };
  },
});
