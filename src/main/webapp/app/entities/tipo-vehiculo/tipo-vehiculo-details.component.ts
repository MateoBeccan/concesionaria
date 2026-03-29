import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';

import TipoVehiculoService from './tipo-vehiculo.service';

export default defineComponent({
  name: 'TipoVehiculoDetails',
  setup() {
    const tipoVehiculoService = inject('tipoVehiculoService', () => new TipoVehiculoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const tipoVehiculo: Ref<ITipoVehiculo> = ref({});

    const retrieveTipoVehiculo = async tipoVehiculoId => {
      try {
        const res = await tipoVehiculoService().find(tipoVehiculoId);
        tipoVehiculo.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.tipoVehiculoId) {
      retrieveTipoVehiculo(route.params.tipoVehiculoId);
    }

    return {
      alertService,
      tipoVehiculo,

      previousState,
    };
  },
});
