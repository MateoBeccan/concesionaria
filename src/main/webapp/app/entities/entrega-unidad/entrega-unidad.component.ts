import { computed, defineComponent, inject, onMounted, ref, type Ref } from 'vue';
import { useAlertService } from '@/shared/alert/alert.service';
import type { IEntregaUnidad } from '@/shared/model/entrega-unidad.model';
import { EstadoEntregaUnidad } from '@/shared/model/enumerations/estado-entrega-unidad.model';
import EntregaUnidadService from './entrega-unidad.service';

export default defineComponent({
  name: 'EntregaUnidad',
  setup() {
    const alertService = inject('alertService', () => useAlertService(), true);
    const entregaUnidadService = inject('entregaUnidadService', () => new EntregaUnidadService());

    const loading = ref(false);
    const entregas: Ref<IEntregaUnidad[]> = ref([]);
    const page = ref(1);
    const itemsPerPage = ref(20);
    const totalItems = ref(0);

    const filtroEstado = ref<string>('');
    const filtroCliente = ref('');
    const filtroVentaId = ref<number | null>(null);
    const filtroDesde = ref<string>('');
    const filtroHasta = ref<string>('');

    const estados = Object.values(EstadoEntregaUnidad);

    const retrieve = async () => {
      loading.value = true;
      try {
        const params: any = {
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: ['fechaProgramada,desc'],
        };
        if (filtroEstado.value) params.estado = filtroEstado.value;
        if (filtroCliente.value.trim()) params.cliente = filtroCliente.value.trim();
        if (filtroVentaId.value) params.ventaId = filtroVentaId.value;
        if (filtroDesde.value) params.fromDate = new Date(`${filtroDesde.value}T00:00:00`).toISOString();
        if (filtroHasta.value) params.toDate = new Date(`${filtroHasta.value}T23:59:59`).toISOString();

        const res = await entregaUnidadService().retrieve(params);
        entregas.value = res.data ?? [];
        totalItems.value = Number(res.headers['x-total-count'] ?? 0);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        loading.value = false;
      }
    };

    const clearFilters = async () => {
      filtroEstado.value = '';
      filtroCliente.value = '';
      filtroVentaId.value = null;
      filtroDesde.value = '';
      filtroHasta.value = '';
      page.value = 1;
      await retrieve();
    };

    const totals = computed(() => ({
      programadas: entregas.value.filter(e => e.estado === EstadoEntregaUnidad.PROGRAMADA).length,
      entregadas: entregas.value.filter(e => e.estado === EstadoEntregaUnidad.ENTREGADA).length,
      canceladas: entregas.value.filter(e => e.estado === EstadoEntregaUnidad.CANCELADA).length,
    }));

    const formatMoney = (valor?: number | null) => Number(valor ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const formatDate = (valor?: Date | string | null) =>
      valor
        ? new Date(valor).toLocaleString('es-AR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
          })
        : '-';

    onMounted(retrieve);

    return {
      loading,
      entregas,
      page,
      itemsPerPage,
      totalItems,
      filtroEstado,
      filtroCliente,
      filtroVentaId,
      filtroDesde,
      filtroHasta,
      estados,
      totals,
      retrieve,
      clearFilters,
      formatMoney,
      formatDate,
    };
  },
});

