import { computed, defineComponent, inject, ref, type Ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import OperationalTraceCard from '@/shared/components/OperationalTraceCard.vue';
import { useDateFormat } from '@/shared/composables';
import { EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import type { IInventario } from '@/shared/model/inventario.model';

import InventarioService from './inventario.service';

export default defineComponent({
  name: 'InventarioDetails',
  components: {
    OperationalTraceCard,
  },
  setup() {
    const dateFormat = useDateFormat();
    const { formatDateLong } = dateFormat;
    const inventarioService = inject('inventarioService', () => new InventarioService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const inventario: Ref<IInventario> = ref({});

    const retrieveInventario = async inventarioId => {
      try {
        inventario.value = await inventarioService().find(inventarioId);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.inventarioId) {
      retrieveInventario(route.params.inventarioId);
    }

    const traceStatus = computed(() => {
      const disponible = inventario.value.estadoInventario === EstadoInventario.DISPONIBLE ? 'Disponible' : 'No disponible';
      return `${inventario.value.estadoInventario ?? 'Sin estado'} · ${disponible}`;
    });

    const traceLastAction = computed(() => {
      if (inventario.value.estadoInventario === EstadoInventario.RESERVADO) return 'Reserva activa';
      if (inventario.value.estadoInventario === EstadoInventario.VENDIDO) return 'Unidad marcada como vendida';
      if (inventario.value.lastModifiedDate && inventario.value.createdDate && inventario.value.lastModifiedDate !== inventario.value.createdDate) {
        return 'Inventario actualizado';
      }
      return 'Unidad ingresada a inventario';
    });

    const traceLastActionAt = computed(() => {
      const actionDate = inventario.value.lastModifiedDate ?? inventario.value.createdDate ?? inventario.value.fechaIngreso;

      return actionDate ? formatDateLong(actionDate) : 'No disponible';
    });

    return {
      ...dateFormat,
      alertService,
      inventario,
      traceStatus,
      traceLastAction,
      traceLastActionAt,
      previousState,
    };
  },
});
