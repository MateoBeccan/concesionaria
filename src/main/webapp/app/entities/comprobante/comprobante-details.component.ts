import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IComprobante } from '@/shared/model/comprobante.model';

import ComprobanteService from './comprobante.service';

export default defineComponent({
  name: 'ComprobanteDetails',
  setup() {
    const dateFormat = useDateFormat();
    const comprobanteService = inject('comprobanteService', () => new ComprobanteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const comprobante: Ref<IComprobante> = ref({});

    const retrieveComprobante = async comprobanteId => {
      try {
        const res = await comprobanteService().find(comprobanteId);
        comprobante.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.comprobanteId) {
      retrieveComprobante(route.params.comprobanteId);
    }

    return {
      ...dateFormat,
      alertService,
      comprobante,

      previousState,
    };
  },
});
