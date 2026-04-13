import { computed, type Ref, defineComponent, inject, onMounted, ref, watch } from 'vue';

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

    const search = ref('');
    const filtroEstado = ref('');
    const filtroCondicion = ref('');
    const filtroTipo = ref('');

    const tipoVehiculoOptions = computed(() => {
      const tipos = new Set(
        (vehiculos.value || [])
          .map(vehiculo => vehiculo.tipoVehiculo?.nombre)
          .filter((value): value is string => !!value),
      );

      return [...tipos].sort((a, b) => a.localeCompare(b));
    });

    const filteredVehiculos = computed(() => {
      return (vehiculos.value || []).filter(vehiculo => {
        const texto = [
          vehiculo.patente ?? '',
          vehiculo.version?.nombre ?? '',
          vehiculo.version?.modelo?.nombre ?? '',
          vehiculo.version?.modelo?.marca?.nombre ?? '',
          vehiculo.motor?.nombre ?? '',
          vehiculo.tipoVehiculo?.nombre ?? '',
        ]
          .join(' ')
          .toLowerCase();

        const matchSearch = !search.value || texto.includes(search.value.toLowerCase());
        const matchEstado = !filtroEstado.value || vehiculo.estado === filtroEstado.value;
        const matchCondicion = !filtroCondicion.value || vehiculo.condicion === filtroCondicion.value;
        const matchTipo = !filtroTipo.value || vehiculo.tipoVehiculo?.nombre === filtroTipo.value;

        return matchSearch && matchEstado && matchCondicion && matchTipo;
      });
    });

    const clear = () => {
      page.value = 1;
    };

    const resetFiltros = () => {
      search.value = '';
      filtroEstado.value = '';
      filtroCondicion.value = '';
      filtroTipo.value = '';
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

        alertService.showInfo(`Vehiculo eliminado con ID ${removeId.value}`, { variant: 'danger' });
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

    const formatPrecio = (value?: number | null) =>
      typeof value === 'number' ? new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 }).format(value) : '-';

    const badgeEstado = (estado?: string | null) => {
      if (estado === 'NUEVO') return 'bg-success';
      if (estado === 'USADO') return 'bg-secondary';
      return 'bg-light text-dark border';
    };

    const badgeCondicion = (condicion?: string | null) => {
      if (condicion === 'EN_VENTA') return 'bg-primary';
      if (condicion === 'RESERVADO') return 'bg-warning text-dark';
      if (condicion === 'VENDIDO') return 'bg-danger';
      return 'bg-light text-dark border';
    };

    watch([propOrder, reverse], async () => {
      if (page.value === 1) {
        await retrieveVehiculos();
      } else {
        clear();
      }
    });

    watch(page, async () => {
      await retrieveVehiculos();
    });

    return {
      vehiculos,
      filteredVehiculos,
      search,
      filtroEstado,
      filtroCondicion,
      filtroTipo,
      tipoVehiculoOptions,
      resetFiltros,
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
      formatPrecio,
      badgeEstado,
      badgeCondicion,
    };
  },
});
