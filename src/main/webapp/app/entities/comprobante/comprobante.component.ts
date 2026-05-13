import { computed, type Ref, defineComponent, inject, onMounted, ref, watch } from 'vue';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IComprobante } from '@/shared/model/comprobante.model';

import ComprobanteService from './comprobante.service';

export default defineComponent({
  name: 'Comprobante',
  setup() {
    const dateFormat = useDateFormat();
    const comprobanteService = inject('comprobanteService', () => new ComprobanteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const itemsPerPage = ref(20);
    const queryCount: Ref<number> = ref(null);
    const page: Ref<number> = ref(1);
    const propOrder = ref('id');
    const reverse = ref(false);
    const totalItems = ref(0);

    const comprobantes: Ref<IComprobante[]> = ref([]);

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

    const retrieveComprobantes = async () => {
      isFetching.value = true;
      try {
        const paginationQuery = {
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: sort(),
        };
        const res = await comprobanteService().retrieve(paginationQuery);
        totalItems.value = Number(res.headers['x-total-count']);
        queryCount.value = totalItems.value;
        comprobantes.value = res.data;
      } catch (err) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrieveComprobantes();
    };

    onMounted(async () => {
      await retrieveComprobantes();
    });

    const removeId: Ref<number> = ref(null);
    const removeEntity = ref<any>(null);
    const prepareRemove = (instance: IComprobante) => {
      removeId.value = instance.id;
      removeEntity.value.show();
    };
    const closeDialog = () => {
      removeEntity.value.hide();
    };
    const descargarPdf = async (id?: number) => {
      if (!id) return;
      try {
        await comprobanteService().descargarPdf(id);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };
    const motivoAnulacion = ref('');
    const removeComprobante = async () => {
      try {
        await comprobanteService().anular(removeId.value, motivoAnulacion.value.trim());
        const message = `Comprobante ${removeId.value} anulado correctamente`;
        alertService.showInfo(message, { variant: 'warning' });
        removeId.value = null;
        motivoAnulacion.value = '';
        retrieveComprobantes();
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
        await retrieveComprobantes();
      } else {
        // reset the pagination
        clear();
      }
    });

    // Whenever page changes, switch to the new page.
    watch(page, async () => {
      await retrieveComprobantes();
    });

    const formatMoney = (value?: number | null) =>
      Number(value ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    const estadoLabel = (estado?: string | null) => {
      if (estado === 'EMITIDO') return 'Emitido';
      if (estado === 'ANULADO') return 'Anulado';
      return estado ?? '-';
    };

    const estadoClass = (estado?: string | null) => (estado === 'ANULADO' ? 'bg-secondary' : 'bg-success');

    const filters = ref({
      estado: '',
      tipo: '',
      moneda: '',
      usuario: '',
      ventaId: '',
      fechaDesde: '',
      fechaHasta: '',
      q: '',
    });

    const filteredComprobantes = computed(() =>
      comprobantes.value.filter(comprobante => {
        const estadoOk = !filters.value.estado || (comprobante.estado ?? '') === filters.value.estado;
        const tipoOk = !filters.value.tipo || (comprobante.tipoComprobante?.codigo ?? '') === filters.value.tipo;
        const monedaOk = !filters.value.moneda || (comprobante.moneda?.codigo ?? '') === filters.value.moneda;
        const usuarioOk = !filters.value.usuario || (comprobante.usuarioEmision ?? comprobante.createdBy ?? '').toLowerCase().includes(filters.value.usuario.toLowerCase());
        const ventaOk = !filters.value.ventaId || String(comprobante.venta?.id ?? '') === filters.value.ventaId.trim();
        const texto = filters.value.q.trim().toLowerCase();
        const textOk =
          !texto ||
          String(comprobante.id ?? '').includes(texto) ||
          (comprobante.numeroComprobante ?? '').toLowerCase().includes(texto) ||
          (comprobante.tipoComprobante?.descripcion ?? '').toLowerCase().includes(texto) ||
          (comprobante.tipoComprobante?.codigo ?? '').toLowerCase().includes(texto);

        const fecha = comprobante.fechaEmision ? new Date(comprobante.fechaEmision).getTime() : undefined;
        const desdeOk = !filters.value.fechaDesde || (fecha !== undefined && fecha >= new Date(`${filters.value.fechaDesde}T00:00:00`).getTime());
        const hastaOk = !filters.value.fechaHasta || (fecha !== undefined && fecha <= new Date(`${filters.value.fechaHasta}T23:59:59`).getTime());

        return estadoOk && tipoOk && monedaOk && usuarioOk && ventaOk && textOk && desdeOk && hastaOk;
      }),
    );

    const resetFilters = () => {
      filters.value = {
        estado: '',
        tipo: '',
        moneda: '',
        usuario: '',
        ventaId: '',
        fechaDesde: '',
        fechaHasta: '',
        q: '',
      };
    };

    const tipoOptions = computed(() =>
      [...new Set(comprobantes.value.map(c => c.tipoComprobante?.codigo).filter(Boolean))].sort((a, b) => a.localeCompare(b)),
    );
    const monedaOptions = computed(() =>
      [...new Set(comprobantes.value.map(c => c.moneda?.codigo).filter(Boolean))].sort((a, b) => a.localeCompare(b)),
    );

    return {
      comprobantes,
      handleSyncList,
      isFetching,
      retrieveComprobantes,
      clear,
      ...dateFormat,
      removeId,
      removeEntity,
      motivoAnulacion,
      prepareRemove,
      closeDialog,
      removeComprobante,
      descargarPdf,
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
      filteredComprobantes,
      resetFilters,
      tipoOptions,
      monedaOptions,
    };
  },
});
