import { computed, type Ref, defineComponent, inject, onMounted, ref, watch } from 'vue';

import dayjs from 'dayjs';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IInventario } from '@/shared/model/inventario.model';

import InventarioService from './inventario.service';

export default defineComponent({
  name: 'Inventario',
  setup() {
    const dateFormat = useDateFormat();
    const inventarioService = inject('inventarioService', () => new InventarioService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const itemsPerPage = ref(20);
    const queryCount: Ref<number | null> = ref(null);
    const page: Ref<number> = ref(1);
    const propOrder = ref('id');
    const reverse = ref(false);
    const totalItems = ref(0);

    const inventarios: Ref<IInventario[]> = ref([]);
    const isFetching = ref(false);

    const search = ref('');
    const filtroEstado = ref('');
    const filtroReserva = ref('');

    const isReservaVencida = (inventario: IInventario) =>
      inventario.estadoInventario === 'RESERVADO' &&
      !!inventario.fechaVencimientoReserva &&
      dayjs(inventario.fechaVencimientoReserva).isBefore(dayjs());

    const inventorySummary = computed(() => ({
      disponible: inventarios.value.filter(item => item.estadoInventario === 'DISPONIBLE').length,
      reservado: inventarios.value.filter(item => item.estadoInventario === 'RESERVADO').length,
      vendido: inventarios.value.filter(item => item.estadoInventario === 'VENDIDO').length,
      vencidas: inventarios.value.filter(item => isReservaVencida(item)).length,
    }));

    const filteredInventarios = computed(() => {
      return (inventarios.value || []).filter(inventario => {
        const texto = [
          inventario.vehiculo?.patente ?? '',
          inventario.vehiculo?.version?.modelo?.marca?.nombre ?? '',
          inventario.vehiculo?.version?.modelo?.nombre ?? '',
          inventario.vehiculo?.version?.nombre ?? '',
          inventario.ubicacion ?? '',
          inventario.clienteReserva ? `${inventario.clienteReserva.apellido ?? ''} ${inventario.clienteReserva.nombre ?? ''}` : '',
        ]
          .join(' ')
          .toLowerCase();

        const matchSearch = !search.value || texto.includes(search.value.toLowerCase());
        const matchEstado = !filtroEstado.value || inventario.estadoInventario === filtroEstado.value;

        let matchReserva = true;
        if (filtroReserva.value === 'activas') {
          matchReserva = inventario.estadoInventario === 'RESERVADO' && !isReservaVencida(inventario);
        } else if (filtroReserva.value === 'vencidas') {
          matchReserva = inventario.estadoInventario === 'RESERVADO' && isReservaVencida(inventario);
        } else if (filtroReserva.value === 'sin-reserva') {
          matchReserva = inventario.estadoInventario !== 'RESERVADO';
        }

        return matchSearch && matchEstado && matchReserva;
      });
    });

    const clear = () => {
      page.value = 1;
    };

    const resetFiltros = () => {
      search.value = '';
      filtroEstado.value = '';
      filtroReserva.value = '';
    };

    const sort = (): Array<any> => {
      const result = [`${propOrder.value},${reverse.value ? 'desc' : 'asc'}`];
      if (propOrder.value !== 'id') {
        result.push('id');
      }
      return result;
    };

    const retrieveInventarios = async () => {
      isFetching.value = true;
      try {
        const paginationQuery = {
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: sort(),
        };
        const res = await inventarioService().retrieve(paginationQuery);
        totalItems.value = Number(res.headers['x-total-count']);
        queryCount.value = totalItems.value;
        inventarios.value = res.data;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrieveInventarios();
    };

    onMounted(async () => {
      await retrieveInventarios();
    });

    const removeId: Ref<number | null> = ref(null);
    const removeEntity = ref<any>(null);
    const prepareRemove = (instance: IInventario) => {
      removeId.value = instance.id ?? null;
      removeEntity.value.show();
    };
    const closeDialog = () => {
      removeEntity.value.hide();
    };
    const removeInventario = async () => {
      try {
        if (removeId.value != null) {
          await inventarioService().delete(removeId.value);
        }
        alertService.showInfo(`Inventario eliminado con ID ${removeId.value}`, { variant: 'danger' });
        removeId.value = null;
        retrieveInventarios();
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

    const badgeEstado = (estado?: string | null) => {
      if (estado === 'DISPONIBLE') return 'bg-success';
      if (estado === 'RESERVADO') return 'bg-warning text-dark';
      if (estado === 'VENDIDO') return 'bg-danger';
      return 'bg-light text-dark border';
    };

    const badgeDisponibilidad = (estado?: string | null) => (estado === 'DISPONIBLE' ? 'bg-success' : 'bg-secondary');

    const vehiculoLabel = (inventario: IInventario) => {
      const patente = inventario.vehiculo?.patente || 'Sin patente';
      const version = inventario.vehiculo?.version?.nombre || 'Sin version';
      return `${patente} · ${version}`;
    };

    const clienteLabel = (inventario: IInventario) => {
      if (!inventario.clienteReserva) return 'Sin cliente';
      return `${inventario.clienteReserva.apellido ?? ''}, ${inventario.clienteReserva.nombre ?? ''}`.replace(/^, |, $/g, '');
    };

    watch([propOrder, reverse], async () => {
      if (page.value === 1) {
        await retrieveInventarios();
      } else {
        clear();
      }
    });

    watch(page, async () => {
      await retrieveInventarios();
    });

    return {
      inventarios,
      inventorySummary,
      filteredInventarios,
      search,
      filtroEstado,
      filtroReserva,
      resetFiltros,
      handleSyncList,
      isFetching,
      retrieveInventarios,
      clear,
      ...dateFormat,
      removeId,
      removeEntity,
      prepareRemove,
      closeDialog,
      removeInventario,
      itemsPerPage,
      queryCount,
      page,
      propOrder,
      reverse,
      totalItems,
      changeOrder,
      badgeEstado,
      badgeDisponibilidad,
      isReservaVencida,
      vehiculoLabel,
      clienteLabel,
    };
  },
});
