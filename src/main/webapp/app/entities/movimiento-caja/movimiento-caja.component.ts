import { type Ref, defineComponent, inject, onMounted, ref, watch } from 'vue';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IMovimientoCaja, type IResumenDiarioCaja } from '@/shared/model/movimiento-caja.model';
import { type IMetodoPago } from '@/shared/model/metodo-pago.model';
import { type IEntidadFinanciera } from '@/shared/model/entidad-financiera.model';
import axios from 'axios';
import MovimientoCajaService from './movimiento-caja.service';

export default defineComponent({
  name: 'MovimientoCaja',
  setup() {
    const dateFormat = useDateFormat();
    const movimientoCajaService = inject('movimientoCajaService', () => new MovimientoCajaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const isFetching = ref(false);
    const movimientos: Ref<IMovimientoCaja[]> = ref([]);
    const resumen: Ref<IResumenDiarioCaja | null> = ref(null);
    const metodosPago: Ref<IMetodoPago[]> = ref([]);
    const entidadesFinancieras: Ref<IEntidadFinanciera[]> = ref([]);

    const itemsPerPage = ref(20);
    const queryCount: Ref<number> = ref(0);
    const page: Ref<number> = ref(1);
    const propOrder = ref('fecha');
    const reverse = ref(true);
    const totalItems = ref(0);

    const filtros = ref({
      fechaDesde: '',
      fechaHasta: '',
      usuario: '',
      metodoPagoId: null as number | null,
      entidadFinancieraId: null as number | null,
      tipo: '',
      estado: '',
      fechaResumen: new Date().toISOString().slice(0, 10),
    });

    const sort = (): Array<any> => {
      const result = [`${propOrder.value},${reverse.value ? 'desc' : 'asc'}`];
      if (propOrder.value !== 'id') result.push('id');
      return result;
    };

    const normalizeDateStart = (value: string): string | undefined => (value ? `${value}T00:00:00Z` : undefined);
    const normalizeDateEnd = (value: string): string | undefined => (value ? `${value}T23:59:59Z` : undefined);

    const retrieveMovimientos = async () => {
      isFetching.value = true;
      try {
        const res = await movimientoCajaService().retrieveWithFilters({
          fechaDesde: normalizeDateStart(filtros.value.fechaDesde),
          fechaHasta: normalizeDateEnd(filtros.value.fechaHasta),
          usuario: filtros.value.usuario || undefined,
          metodoPagoId: filtros.value.metodoPagoId,
          entidadFinancieraId: filtros.value.entidadFinancieraId,
          tipo: filtros.value.tipo || undefined,
          estado: filtros.value.estado || undefined,
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: sort(),
        });
        totalItems.value = Number(res.headers['x-total-count'] ?? 0);
        queryCount.value = totalItems.value;
        movimientos.value = res.data;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const retrieveResumen = async () => {
      try {
        resumen.value = await movimientoCajaService().resumenDiario(filtros.value.fechaResumen || undefined);
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const cargarReferenciales = async () => {
      const [metodosRes, entidadesRes] = await Promise.all([axios.get('api/metodo-pagos'), axios.get('api/entidades-financieras')]);
      metodosPago.value = metodosRes.data ?? [];
      entidadesFinancieras.value = entidadesRes.data ?? [];
    };

    const aplicarFiltros = async () => {
      page.value = 1;
      await retrieveMovimientos();
    };

    const limpiarFiltros = async () => {
      filtros.value.fechaDesde = '';
      filtros.value.fechaHasta = '';
      filtros.value.usuario = '';
      filtros.value.metodoPagoId = null;
      filtros.value.entidadFinancieraId = null;
      filtros.value.tipo = '';
      filtros.value.estado = '';
      await aplicarFiltros();
    };

    const changeOrder = (newOrder: string) => {
      if (propOrder.value === newOrder) reverse.value = !reverse.value;
      else {
        reverse.value = true;
        propOrder.value = newOrder;
      }
    };

    watch(page, async () => retrieveMovimientos());
    watch([propOrder, reverse], async () => {
      if (page.value === 1) await retrieveMovimientos();
      else page.value = 1;
    });

    onMounted(async () => {
      await cargarReferenciales();
      await retrieveMovimientos();
      await retrieveResumen();
    });

    return {
      ...dateFormat,
      isFetching,
      movimientos,
      resumen,
      metodosPago,
      entidadesFinancieras,
      filtros,
      itemsPerPage,
      queryCount,
      page,
      propOrder,
      reverse,
      totalItems,
      retrieveMovimientos,
      retrieveResumen,
      aplicarFiltros,
      limpiarFiltros,
      changeOrder,
    };
  },
});
