import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type IEstadoVenta } from '@/shared/model/estado-venta.model';

import EstadoVentaService from './estado-venta.service';

export default defineComponent({
  name: 'EstadoVentaDetails',
  setup() {
    const estadoVentaService = inject('estadoVentaService', () => new EstadoVentaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const estadoVenta: Ref<IEstadoVenta> = ref({});

    const retrieveEstadoVenta = async estadoVentaId => {
      try {
        const res = await estadoVentaService().find(estadoVentaId);
        estadoVenta.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.estadoVentaId) {
      retrieveEstadoVenta(route.params.estadoVentaId);
    }

    return {
      alertService,
      estadoVenta,

      previousState,
    };
  },
});
