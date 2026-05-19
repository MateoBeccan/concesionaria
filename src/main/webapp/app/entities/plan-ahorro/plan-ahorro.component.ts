import { computed, defineComponent, inject, onMounted, ref, type Ref } from 'vue';
import axios from 'axios';
import { useAlertService } from '@/shared/alert/alert.service';
import PlanAhorroService from './plan-ahorro.service';
import type { IPlanAhorro } from '@/shared/model/plan-ahorro.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { IReglaAdjudicacionPlan } from '@/shared/model/regla-adjudicacion-plan.model';

export default defineComponent({
  name: 'PlanAhorro',
  setup() {
    const alertService = inject('alertService', () => useAlertService(), true);
    const planAhorroService = inject('planAhorroService', () => new PlanAhorroService());
    const planes: Ref<IPlanAhorro[]> = ref([]);
    const monedas: Ref<IMoneda[]> = ref([]);
    const reglas: Ref<IReglaAdjudicacionPlan[]> = ref([]);
    const showCreate = ref(false);
    const draft = ref({
      nombre: '',
      descripcion: '',
      cantidadCuotas: 84,
      valorMovil: 0,
      monedaId: null as number | null,
      reglaAdjudicacionId: null as number | null,
    });
    const searchTerm = ref('');
    const estadoFiltro = ref('');

    const planesFiltrados = computed(() => {
      const term = searchTerm.value.trim().toLowerCase();
      return planes.value.filter(plan => {
        const byEstado = !estadoFiltro.value || plan.estado === estadoFiltro.value;
        if (!byEstado) return false;
        if (!term) return true;
        return [plan.nombre, plan.descripcion, plan.moneda?.codigo, plan.versionObjetivo?.nombre].some(value =>
          String(value ?? '')
            .toLowerCase()
            .includes(term),
        );
      });
    });

    const totalActivos = computed(() => planes.value.filter(plan => plan.estado === 'ACTIVO').length);

    const retrieve = async () => {
      try {
        const res = await planAhorroService().retrieve({ size: 200, sort: ['id,desc'] });
        planes.value = res.data ?? [];
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const loadReferenciales = async () => {
      const res = await axios.get('api/monedas', { params: { size: 200, sort: ['codigo,asc'] } });
      monedas.value = res.data ?? [];
      const reglasRes = await axios.get('api/reglas-adjudicacion-plan', { params: { active: true } });
      reglas.value = reglasRes.data ?? [];
    };

    const openCreate = () => {
      draft.value = { nombre: '', descripcion: '', cantidadCuotas: 84, valorMovil: 0, monedaId: null, reglaAdjudicacionId: null };
      showCreate.value = true;
    };

    const save = async () => {
      if (!draft.value.nombre || !draft.value.monedaId || !draft.value.cantidadCuotas || !draft.value.valorMovil) {
        alertService.showError('Completá nombre, moneda, cuotas y valor móvil');
        return;
      }
      try {
        await planAhorroService().create({
          nombre: draft.value.nombre,
          descripcion: draft.value.descripcion,
          cantidadCuotas: draft.value.cantidadCuotas,
          valorMovil: draft.value.valorMovil,
          estado: 'ACTIVO' as any,
          moneda: { id: draft.value.monedaId } as any,
          reglaAdjudicacion: draft.value.reglaAdjudicacionId ? ({ id: draft.value.reglaAdjudicacionId } as any) : null,
        });
        showCreate.value = false;
        await retrieve();
        alertService.showInfo('Plan creado correctamente');
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    onMounted(async () => {
      await loadReferenciales();
      await retrieve();
    });

    return {
      planes,
      monedas,
      reglas,
      showCreate,
      draft,
      searchTerm,
      estadoFiltro,
      planesFiltrados,
      totalActivos,
      openCreate,
      save,
    };
  },
});
