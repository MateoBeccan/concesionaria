import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type ITipoComprobante } from '@/shared/model/tipo-comprobante.model';

import TipoComprobanteService from './tipo-comprobante.service';

export default defineComponent({
  name: 'TipoComprobanteDetails',
  setup() {
    const tipoComprobanteService = inject('tipoComprobanteService', () => new TipoComprobanteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const tipoComprobante: Ref<ITipoComprobante> = ref({});

    const retrieveTipoComprobante = async tipoComprobanteId => {
      try {
        const res = await tipoComprobanteService().find(tipoComprobanteId);
        tipoComprobante.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.tipoComprobanteId) {
      retrieveTipoComprobante(route.params.tipoComprobanteId);
    }

    return {
      alertService,
      tipoComprobante,

      previousState,
    };
  },
});
