import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IModelo } from '@/shared/model/modelo.model';

import ModeloService from './modelo.service';

export default defineComponent({
  name: 'ModeloDetails',
  setup() {
    const dateFormat = useDateFormat();
    const modeloService = inject('modeloService', () => new ModeloService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const modelo: Ref<IModelo> = ref({});

    const retrieveModelo = async modeloId => {
      try {
        const res = await modeloService().find(modeloId);
        modelo.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.modeloId) {
      retrieveModelo(route.params.modeloId);
    }

    return {
      ...dateFormat,
      alertService,
      modelo,

      previousState,
    };
  },
});
