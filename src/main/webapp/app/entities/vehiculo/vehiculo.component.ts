import { type Ref, defineComponent, inject, onMounted, ref, watch } from 'vue';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IVehiculo } from '@/shared/model/vehiculo.model';

import VehiculoService from './vehiculo.service';

export default defineComponent({
  name: 'Vehiculo',
  setup() {
    const dateFormat = useDateFormat();
    const vehiculoService = inject('vehiculoService', () => new VehiculoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const itemsPerPage = ref(20);
    const queryCount: Ref<number | null> = ref(null);
    const page: Ref<number> = ref(1);
    const propOrder = ref('id');
    const reverse = ref(false);
    const totalItems = ref(0);

    const vehiculos: Ref<IVehiculo[]> = ref([]);

    const isFetching = ref(false);

    const clear = () => {
      page.value = 1;
    };

    const sort = (): Array<any> => {
      const result = [`${propOrder.value},${reverse.value ? 'desc' : 'asc'}`];
      if (propOrder.value !== 'id') {
        result.push('id');
      }
      return result;
    };

    const retrieveVehiculos = async () => {
      isFetching.value = true;
      try {
        const paginationQuery = {
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: sort(),
        };
        const res = await vehiculoService().retrieve(paginationQuery);
        totalItems.value = Number(res.headers['x-total-count']);
        queryCount.value = totalItems.value;
        vehiculos.value = res.data;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrieveVehiculos();
    };

    onMounted(async () => {
      await retrieveVehiculos();
    });

    const removeId: Ref<number | null> = ref(null);
    const removeEntity = ref<any>(null);
    const prepareRemove = (instance: IVehiculo) => {
      removeId.value = instance.id ?? null;
      removeEntity.value.show();
    };
    const closeDialog = () => {
      removeEntity.value.hide();
    };
    const removeVehiculo = async () => {
      try {
        if (removeId.value != null) {
          await vehiculoService().delete(removeId.value);
        }

        const message = `A Vehiculo is deleted with identifier ${removeId.value}`;
        alertService.showInfo(message, { variant: 'danger' });

        removeId.value = null;
        retrieveVehiculos();
        closeDialog();
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const changeOrder = (newOrder: string) => {
      if (propOrder.value === newOrder) {
        reverse.value = !reverse.value;
      } else {
        reverse.value = false;
      }
      propOrder.value = newOrder;
    };

    // Whenever order changes, reset the pagination
    watch([propOrder, reverse], async () => {
      if (page.value === 1) {
        // first page, retrieve new data
        await retrieveVehiculos();
      } else {
        // reset the pagination
        clear();
      }
    });

    // Whenever page changes, switch to the new page.
    watch(page, async () => {
      await retrieveVehiculos();
    });

    return {
      vehiculos,
      handleSyncList,
      isFetching,
      retrieveVehiculos,
      clear,
      ...dateFormat,
      removeId,
      removeEntity,
      prepareRemove,
      closeDialog,
      removeVehiculo,
      itemsPerPage,
      queryCount,
      page,
      propOrder,
      reverse,
      totalItems,
      changeOrder,
    };
  },
});
