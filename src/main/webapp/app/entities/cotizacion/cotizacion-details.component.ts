import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type ICotizacion } from '@/shared/model/cotizacion.model';

import CotizacionService from './cotizacion.service';

export default defineComponent({
  name: 'CotizacionDetails',
  setup() {
    const dateFormat = useDateFormat();
    const cotizacionService = inject('cotizacionService', () => new CotizacionService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const cotizacion: Ref<ICotizacion> = ref({});

    const retrieveCotizacion = async cotizacionId => {
      try {
        const res = await cotizacionService().find(cotizacionId);
        cotizacion.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.cotizacionId) {
      retrieveCotizacion(route.params.cotizacionId);
    }

    return {
      ...dateFormat,
      alertService,
      cotizacion,

      previousState,
    };
  },
});
