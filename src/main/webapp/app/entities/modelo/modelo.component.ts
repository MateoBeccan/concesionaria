import { type Ref, defineComponent, inject, onMounted, ref, watch } from 'vue';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IModelo } from '@/shared/model/modelo.model';

import ModeloService from './modelo.service';

export default defineComponent({
  name: 'Modelo',
  setup() {
    const dateFormat = useDateFormat();
    const modeloService = inject('modeloService', () => new ModeloService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const itemsPerPage = ref(20);
    const queryCount: Ref<number> = ref(null);
    const page: Ref<number> = ref(1);
    const propOrder = ref('id');
    const reverse = ref(false);
    const totalItems = ref(0);

    const modelos: Ref<IModelo[]> = ref([]);

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

    const retrieveModelos = async () => {
      isFetching.value = true;
      try {
        const paginationQuery = {
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: sort(),
        };
        const res = await modeloService().retrieve(paginationQuery);
        totalItems.value = Number(res.headers['x-total-count']);
        queryCount.value = totalItems.value;
        modelos.value = res.data;
      } catch (err) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrieveModelos();
    };

    onMounted(async () => {
      await retrieveModelos();
    });

    const removeId: Ref<number> = ref(null);
    const removeEntity = ref<any>(null);
    const prepareRemove = (instance: IModelo) => {
      removeId.value = instance.id;
      removeEntity.value.show();
    };
    const closeDialog = () => {
      removeEntity.value.hide();
    };
    const removeModelo = async () => {
      try {
        await modeloService().delete(removeId.value);
        const message = `A Modelo is deleted with identifier ${removeId.value}`;
        alertService.showInfo(message, { variant: 'danger' });
        removeId.value = null;
        retrieveModelos();
        closeDialog();
      } catch (error) {
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
        await retrieveModelos();
      } else {
        // reset the pagination
        clear();
      }
    });

    // Whenever page changes, switch to the new page.
    watch(page, async () => {
      await retrieveModelos();
    });

    return {
      modelos,
      handleSyncList,
      isFetching,
      retrieveModelos,
      clear,
      ...dateFormat,
      removeId,
      removeEntity,
      prepareRemove,
      closeDialog,
      removeModelo,
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
