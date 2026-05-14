import { computed, defineComponent, inject, onMounted, ref, type Ref } from 'vue';
import { useAlertService } from '@/shared/alert/alert.service';
import type { IAdjudicacionPlanAhorro } from '@/shared/model/adjudicacion-plan-ahorro.model';
import AdjudicacionPlanAhorroService from './adjudicacion-plan-ahorro.service';

interface AdjudicacionRow {
  adjudicacion: IAdjudicacionPlanAhorro;
}

export default defineComponent({
  name: 'AdjudicacionPlanAhorro',
  setup() {
    const alertService = inject('alertService', () => useAlertService(), true);
    const adjudicacionService = inject('adjudicacionPlanAhorroService', () => new AdjudicacionPlanAhorroService());

    const loading = ref(false);
    const rows: Ref<AdjudicacionRow[]> = ref([]);
    const searchTerm = ref('');
    const estadoFiltro = ref('');
    const page = ref(1);
    const itemsPerPage = ref(20);
    const totalItems = ref(0);

    const estadosDisponibles = computed(() => {
      const unique = new Set(rows.value.map(r => r.adjudicacion.estado).filter(Boolean) as string[]);
      return Array.from(unique);
    });
    const adjudicacionesConVenta = computed(() => rows.value.filter(row => !!row.adjudicacion.venta?.id).length);

    const filtradas = computed(() => {
      const term = searchTerm.value.trim().toLowerCase();
      return rows.value.filter(row => {
        const byEstado = !estadoFiltro.value || row.adjudicacion?.estado === estadoFiltro.value;
        if (!byEstado) return false;
        if (!term) return true;
        const clienteNombre = `${row.adjudicacion.cliente?.apellido ?? ''} ${row.adjudicacion.cliente?.nombre ?? ''}`.trim();
        return (
          (row.adjudicacion.numeroContrato ?? '').toLowerCase().includes(term) ||
          clienteNombre.toLowerCase().includes(term) ||
          (row.adjudicacion.planNombre ?? '').toLowerCase().includes(term) ||
          (row.adjudicacion?.estado ?? '').toLowerCase().includes(term)
        );
      });
    });

    const retrieve = async () => {
      loading.value = true;
      try {
        const res = await adjudicacionService().retrieve({
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: ['id,desc'],
        });
        totalItems.value = Number(res.headers['x-total-count'] ?? 0);
        rows.value = ((res.data ?? []) as IAdjudicacionPlanAhorro[]).map(adjudicacion => ({ adjudicacion }));
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        loading.value = false;
      }
    };

    const formatMoney = (value?: number | null): string => Number(value ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    onMounted(async () => {
      await retrieve();
    });

    return {
      loading,
      rows,
      filtradas,
      searchTerm,
      estadoFiltro,
      estadosDisponibles,
      adjudicacionesConVenta,
      page,
      itemsPerPage,
      totalItems,
      retrieve,
      formatMoney,
    };
  },
});
