import { computed, defineComponent, inject, onMounted, ref, type Ref } from 'vue';
import { useAlertService } from '@/shared/alert/alert.service';
import type { IReglaAdjudicacionPlan } from '@/shared/model/regla-adjudicacion-plan.model';
import type { TipoReglaAdjudicacionPlan } from '@/shared/model/enumerations/tipo-regla-adjudicacion-plan.model';
import ReglaAdjudicacionPlanService from './regla-adjudicacion-plan.service';

const TIPOS: TipoReglaAdjudicacionPlan[] = ['SIN_REGLA', 'POR_CUOTAS', 'POR_PORCENTAJE', 'CUOTAS_O_PORCENTAJE', 'CUOTAS_Y_PORCENTAJE', 'MANUAL'];

export default defineComponent({
  name: 'ReglaAdjudicacionPlan',
  setup() {
    const alertService = inject('alertService', () => useAlertService(), true);
    const service = inject('reglaAdjudicacionPlanService', () => new ReglaAdjudicacionPlanService());
    const reglas: Ref<IReglaAdjudicacionPlan[]> = ref([]);
    const showForm = ref(false);
    const editing = ref(false);
    const draft = ref<IReglaAdjudicacionPlan>({
      nombre: '',
      descripcion: '',
      tipoRegla: 'SIN_REGLA',
      minimoCuotas: null,
      minimoPorcentaje: null,
      permiteMora: false,
      requiereContratoActivo: true,
      activo: true,
    });

    const activeCount = computed(() => reglas.value.filter(r => r.activo).length);

    const load = async () => {
      try {
        reglas.value = await service().retrieve();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const newRegla = () => {
      editing.value = false;
      draft.value = {
        nombre: '',
        descripcion: '',
        tipoRegla: 'SIN_REGLA',
        minimoCuotas: null,
        minimoPorcentaje: null,
        permiteMora: false,
        requiereContratoActivo: true,
        activo: true,
      };
      showForm.value = true;
    };

    const editRegla = (regla: IReglaAdjudicacionPlan) => {
      editing.value = true;
      draft.value = { ...regla };
      showForm.value = true;
    };

    const save = async () => {
      try {
        if (editing.value) {
          await service().update(draft.value);
          alertService.showInfo('Regla actualizada');
        } else {
          await service().create(draft.value);
          alertService.showInfo('Regla creada');
        }
        showForm.value = false;
        await load();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const deactivate = async (regla: IReglaAdjudicacionPlan) => {
      if (!regla.id) return;
      try {
        await service().deactivate(regla.id);
        alertService.showInfo('Regla desactivada');
        await load();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    onMounted(load);

    return { reglas, showForm, draft, editing, TIPOS, activeCount, newRegla, editRegla, save, deactivate };
  },
});
