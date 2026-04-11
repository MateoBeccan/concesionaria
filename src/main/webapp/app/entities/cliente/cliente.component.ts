import { type Ref, defineComponent, inject, onMounted, ref, watch, computed } from 'vue';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type ICliente } from '@/shared/model/cliente.model';

import ClienteService from './cliente.service';

export default defineComponent({
  name: 'Cliente',
  setup() {
    const dateFormat = useDateFormat();
    const clienteService = inject('clienteService', () => new ClienteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    // 🔹 PAGINACIÓN / ORDEN
    const itemsPerPage = ref(20);
    const queryCount: Ref<number> = ref(null);
    const page: Ref<number> = ref(1);
    const propOrder = ref('id');
    const reverse = ref(false);
    const totalItems = ref(0);

    // 🔹 DATA
    const clientes: Ref<ICliente[]> = ref([]);
    const isFetching = ref(false);

    // 🔹 🔥 FILTROS
    const search = ref('');
    const filtroActivo = ref('');
    const filtroCiudad = ref('');

    const resetFiltros = () => {
      search.value = '';
      filtroActivo.value = '';
      filtroCiudad.value = '';
    };

    // 🔹 COMPUTED FILTRADO
    const filteredClientes = computed(() => {
      return (clientes.value || []).filter((c: any) => {
        const texto = `${c.nombre} ${c.apellido} ${c.email} ${c.nroDocumento}`.toLowerCase();

        const matchSearch = texto.includes(search.value.toLowerCase());

        const matchActivo =
          filtroActivo.value === ''
            ? true
            : c.activo.toString() === filtroActivo.value;

        const matchCiudad =
          !filtroCiudad.value ||
          c.ciudad?.toLowerCase().includes(filtroCiudad.value.toLowerCase());

        return matchSearch && matchActivo && matchCiudad;
      });
    });

    // 🔹 FUNCIONES BASE
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

    const retrieveClientes = async () => {
      isFetching.value = true;
      try {
        const paginationQuery = {
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: sort(),
        };
        const res = await clienteService().retrieve(paginationQuery);
        totalItems.value = Number(res.headers['x-total-count']);
        queryCount.value = totalItems.value;
        clientes.value = res.data;
      } catch (err) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrieveClientes();
    };

    onMounted(async () => {
      await retrieveClientes();
    });

    // 🔹 DELETE
    const removeId: Ref<number> = ref(null);
    const removeEntity = ref<any>(null);

    const prepareRemove = (instance: ICliente) => {
      removeId.value = instance.id;
      removeEntity.value.show();
    };

    const closeDialog = () => {
      removeEntity.value.hide();
    };

    const removeCliente = async () => {
      try {
        await clienteService().delete(removeId.value);
        const message = `Cliente eliminado con ID ${removeId.value}`;
        alertService.showInfo(message, { variant: 'danger' });
        removeId.value = null;
        retrieveClientes();
        closeDialog();
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    // 🔹 ORDEN
    const changeOrder = (newOrder: string) => {
      if (propOrder.value === newOrder) {
        reverse.value = !reverse.value;
      } else {
        reverse.value = false;
      }
      propOrder.value = newOrder;
    };

    // 🔹 WATCHERS
    watch([propOrder, reverse], async () => {
      if (page.value === 1) {
        await retrieveClientes();
      } else {
        clear();
      }
    });

    watch(page, async () => {
      await retrieveClientes();
    });

    return {
      clientes,
      filteredClientes, // 🔥 IMPORTANTE (para el template)
      search,
      filtroActivo,
      filtroCiudad,
      resetFiltros,

      handleSyncList,
      isFetching,
      retrieveClientes,
      clear,
      ...dateFormat,

      removeId,
      removeEntity,
      prepareRemove,
      closeDialog,
      removeCliente,

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
