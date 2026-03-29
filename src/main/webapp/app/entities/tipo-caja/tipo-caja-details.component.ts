import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type ITipoCaja } from '@/shared/model/tipo-caja.model';

import TipoCajaService from './tipo-caja.service';

export default defineComponent({
  name: 'TipoCajaDetails',
  setup() {
    const tipoCajaService = inject('tipoCajaService', () => new TipoCajaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const tipoCaja: Ref<ITipoCaja> = ref({});

    const retrieveTipoCaja = async tipoCajaId => {
      try {
        const res = await tipoCajaService().find(tipoCajaId);
        tipoCaja.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.tipoCajaId) {
      retrieveTipoCaja(route.params.tipoCajaId);
    }

    return {
      alertService,
      tipoCaja,

      previousState,
    };
  },
});
