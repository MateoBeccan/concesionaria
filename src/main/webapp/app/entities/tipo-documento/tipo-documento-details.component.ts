import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type ITipoDocumento } from '@/shared/model/tipo-documento.model';

import TipoDocumentoService from './tipo-documento.service';

export default defineComponent({
  name: 'TipoDocumentoDetails',
  setup() {
    const tipoDocumentoService = inject('tipoDocumentoService', () => new TipoDocumentoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const tipoDocumento: Ref<ITipoDocumento> = ref({});

    const retrieveTipoDocumento = async tipoDocumentoId => {
      try {
        const res = await tipoDocumentoService().find(tipoDocumentoId);
        tipoDocumento.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.tipoDocumentoId) {
      retrieveTipoDocumento(route.params.tipoDocumentoId);
    }

    return {
      alertService,
      tipoDocumento,

      previousState,
    };
  },
});
