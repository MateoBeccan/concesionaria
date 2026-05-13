import { computed, type Ref, defineComponent, inject, onMounted, ref, watch } from 'vue';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IPago } from '@/shared/model/pago.model';

import PagoService from './pago.service';

export default defineComponent({
  name: 'Pago',
  setup() {
    const dateFormat = useDateFormat();
    const pagoService = inject('pagoService', () => new PagoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const itemsPerPage = ref(20);
    const queryCount: Ref<number> = ref(null);
    const page: Ref<number> = ref(1);
    const propOrder = ref('id');
    const reverse = ref(false);
    const totalItems = ref(0);

    const pagos: Ref<IPago[]> = ref([]);

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

    const retrievePagos = async () => {
      isFetching.value = true;
      try {
        const paginationQuery = {
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: sort(),
        };
        const res = await pagoService().retrieve(paginationQuery);
        totalItems.value = Number(res.headers['x-total-count']);
        queryCount.value = totalItems.value;
        pagos.value = res.data;
      } catch (err) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrievePagos();
    };

    onMounted(async () => {
      await retrievePagos();
    });

    const removeId: Ref<number> = ref(null);
    const removeEntity = ref<any>(null);
    const prepareRemove = (instance: IPago) => {
      removeId.value = instance.id;
      removeEntity.value.show();
    };
    const closeDialog = () => {
      removeEntity.value.hide();
    };
    const motivoAnulacion = ref('');
    const removePago = async () => {
      try {
        await pagoService().anular(removeId.value, motivoAnulacion.value.trim());
        const message = `Pago ${removeId.value} anulado correctamente`;
        alertService.showInfo(message, { variant: 'warning' });
        removeId.value = null;
        motivoAnulacion.value = '';
        retrievePagos();
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
        await retrievePagos();
      } else {
        // reset the pagination
        clear();
      }
    });

    // Whenever page changes, switch to the new page.
    watch(page, async () => {
      await retrievePagos();
    });

    const formatMoney = (value?: number | null, currency = 'ARS') =>
      new Intl.NumberFormat('es-AR', {
        style: 'currency',
        currency,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }).format(Number(value ?? 0));

    const estadoLabel = (estado?: string) => (estado === 'ANULADO' ? 'Anulado' : 'Registrado');
    const estadoClass = (estado?: string) => (estado === 'ANULADO' ? 'bg-secondary-subtle text-secondary-emphasis' : 'bg-success-subtle text-success-emphasis');

    const filters = ref({
      estado: '',
      metodo: '',
      moneda: '',
      usuario: '',
      ventaId: '',
      fechaDesde: '',
      fechaHasta: '',
      q: '',
    });

    const filteredPagos = computed(() =>
      pagos.value.filter(pago => {
        const estadoOk = !filters.value.estado || (pago.estado ?? '') === filters.value.estado;
        const metodoOk = !filters.value.metodo || (pago.metodoPago?.codigo ?? '') === filters.value.metodo;
        const monedaOk = !filters.value.moneda || (pago.moneda?.codigo ?? '') === filters.value.moneda;
        const usuarioOk = !filters.value.usuario || (pago.usuarioRegistro ?? '').toLowerCase().includes(filters.value.usuario.toLowerCase());
        const ventaOk = !filters.value.ventaId || String(pago.venta?.id ?? '') === filters.value.ventaId.trim();
        const texto = filters.value.q.trim().toLowerCase();
        const textOk =
          !texto ||
          String(pago.id ?? '').includes(texto) ||
          (pago.referencia ?? '').toLowerCase().includes(texto) ||
          (pago.numeroOperacion ?? '').toLowerCase().includes(texto) ||
          (pago.comprobanteExterno ?? '').toLowerCase().includes(texto) ||
          (pago.entidadFinanciera?.nombre ?? '').toLowerCase().includes(texto) ||
          (pago.metodoPago?.descripcion ?? '').toLowerCase().includes(texto);

        const fechaPago = pago.fecha ? new Date(pago.fecha).getTime() : undefined;
        const desdeOk = !filters.value.fechaDesde || (fechaPago !== undefined && fechaPago >= new Date(`${filters.value.fechaDesde}T00:00:00`).getTime());
        const hastaOk = !filters.value.fechaHasta || (fechaPago !== undefined && fechaPago <= new Date(`${filters.value.fechaHasta}T23:59:59`).getTime());

        return estadoOk && metodoOk && monedaOk && usuarioOk && ventaOk && textOk && desdeOk && hastaOk;
      }),
    );

    const resetFilters = () => {
      filters.value = {
        estado: '',
        metodo: '',
        moneda: '',
        usuario: '',
        ventaId: '',
        fechaDesde: '',
        fechaHasta: '',
        q: '',
      };
    };

    const metodoOptions = computed(() =>
      [...new Set(pagos.value.map(p => p.metodoPago?.codigo).filter(Boolean))].sort((a, b) => a.localeCompare(b)),
    );
    const monedaOptions = computed(() =>
      [...new Set(pagos.value.map(p => p.moneda?.codigo).filter(Boolean))].sort((a, b) => a.localeCompare(b)),
    );

    return {
      pagos,
      handleSyncList,
      isFetching,
      retrievePagos,
      clear,
      ...dateFormat,
      removeId,
      removeEntity,
      motivoAnulacion,
      prepareRemove,
      closeDialog,
      removePago,
      itemsPerPage,
      queryCount,
      page,
      propOrder,
      reverse,
      totalItems,
      changeOrder,
      formatMoney,
      estadoLabel,
      estadoClass,
      filters,
      filteredPagos,
      resetFilters,
      metodoOptions,
      monedaOptions,
    };
  },
});
