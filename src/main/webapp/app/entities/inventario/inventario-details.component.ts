import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IInventario } from '@/shared/model/inventario.model';

import InventarioService from './inventario.service';

export default defineComponent({
  name: 'InventarioDetails',
  setup() {
    const dateFormat = useDateFormat();
    const inventarioService = inject('inventarioService', () => new InventarioService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const inventario: Ref<IInventario> = ref({});

    const retrieveInventario = async inventarioId => {
      try {
        const res = await inventarioService().find(inventarioId);
        inventario.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.inventarioId) {
      retrieveInventario(route.params.inventarioId);
    }

    return {
      ...dateFormat,
      alertService,
      inventario,

      previousState,
    };
  },
});
