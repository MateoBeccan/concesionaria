import { computed, defineComponent, inject, onMounted, ref, type Ref } from 'vue';
import axios from 'axios';
import { useAlertService } from '@/shared/alert/alert.service';
import type { IContratoPlanAhorro } from '@/shared/model/contrato-plan-ahorro.model';
import type { IPlanAhorro } from '@/shared/model/plan-ahorro.model';
import type { ICliente } from '@/shared/model/cliente.model';
import ContratoPlanAhorroService from './contrato-plan-ahorro.service';

export default defineComponent({
  name: 'ContratoPlanAhorro',
  setup() {
    const alertService = inject('alertService', () => useAlertService(), true);
    const contratoService = inject('contratoPlanAhorroService', () => new ContratoPlanAhorroService());

    const contratos: Ref<IContratoPlanAhorro[]> = ref([]);
    const planes: Ref<IPlanAhorro[]> = ref([]);
    const clientes: Ref<ICliente[]> = ref([]);
    const showCreate = ref(false);
    const draft = ref({ clienteId: null as number | null, planId: null as number | null, observaciones: '' });
    const searchTerm = ref('');
    const estadoFiltro = ref('');

    const contratosFiltrados = computed(() => {
      const term = searchTerm.value.trim().toLowerCase();
      return contratos.value.filter(contrato => {
        const byEstado = !estadoFiltro.value || contrato.estado === estadoFiltro.value;
        if (!byEstado) return false;
        if (!term) return true;
        const cliente = `${contrato.cliente?.apellido ?? ''} ${contrato.cliente?.nombre ?? ''}`.trim();
        return [contrato.numeroContrato, contrato.plan?.nombre, cliente, contrato.estado].some(value =>
          String(value ?? '')
            .toLowerCase()
            .includes(term),
        );
      });
    });
    const contratosActivos = computed(() => contratos.value.filter(item => item.estado === 'ACTIVO').length);

    const retrieve = async () => {
      const res = await contratoService().retrieve({ size: 200, sort: ['id,desc'] });
      contratos.value = res.data ?? [];
    };

    const loadReferenciales = async () => {
      const [planesRes, clientesRes] = await Promise.all([
        axios.get('api/plan-ahorros', { params: { size: 200, sort: ['id,desc'] } }),
        axios.get('api/clientes', { params: { size: 200, sort: ['apellido,asc'] } }),
      ]);
      planes.value = planesRes.data ?? [];
      clientes.value = clientesRes.data ?? [];
    };

    const openCreate = () => {
      draft.value = { clienteId: null, planId: null, observaciones: '' };
      showCreate.value = true;
    };

    const save = async () => {
      if (!draft.value.clienteId || !draft.value.planId) {
        alertService.showError('Seleccioná cliente y plan');
        return;
      }
      try {
        await contratoService().create({
          cliente: { id: draft.value.clienteId } as any,
          plan: { id: draft.value.planId } as any,
          fechaInicio: new Date(),
          estado: 'ACTIVO' as any,
          observaciones: draft.value.observaciones,
        });
        showCreate.value = false;
        await retrieve();
        alertService.showInfo('Contrato creado correctamente');
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    onMounted(async () => {
      try {
        await loadReferenciales();
        await retrieve();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    });

    return {
      contratos,
      planes,
      clientes,
      showCreate,
      draft,
      searchTerm,
      estadoFiltro,
      contratosFiltrados,
      contratosActivos,
      openCreate,
      save,
    };
  },
});
