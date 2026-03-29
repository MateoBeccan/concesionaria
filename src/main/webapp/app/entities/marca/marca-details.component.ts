import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IMarca } from '@/shared/model/marca.model';

import MarcaService from './marca.service';

export default defineComponent({
  name: 'MarcaDetails',
  setup() {
    const dateFormat = useDateFormat();
    const marcaService = inject('marcaService', () => new MarcaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const marca: Ref<IMarca> = ref({});

    const retrieveMarca = async marcaId => {
      try {
        const res = await marcaService().find(marcaId);
        marca.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.marcaId) {
      retrieveMarca(route.params.marcaId);
    }

    return {
      ...dateFormat,
      alertService,
      marca,

      previousState,
    };
  },
});
