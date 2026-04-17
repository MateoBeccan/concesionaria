import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import OperationalTraceCard from '@/shared/components/OperationalTraceCard.vue';
import { useDateFormat } from '@/shared/composables';
import { type IVehiculo } from '@/shared/model/vehiculo.model';

import VehiculoService from './vehiculo.service';

export default defineComponent({
  name: 'VehiculoDetails',
  components: {
    OperationalTraceCard,
  },
  setup() {
    const dateFormat = useDateFormat();
    const vehiculoService = inject('vehiculoService', () => new VehiculoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);
    const vehiculo: Ref<IVehiculo> = ref({});

    const retrieveVehiculo = async (vehiculoId: any) => {
      try {
        vehiculo.value = await vehiculoService().find(vehiculoId);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.vehiculoId) {
      retrieveVehiculo(route.params.vehiculoId);
    }

    function formatPrecio(precio?: number | null): string {
      return Number(precio ?? 0).toLocaleString('es-AR');
    }

    function formatFecha(fecha?: Date | string | null): string {
      if (!fecha) return '—';
      return new Date(fecha).toLocaleDateString('es-AR');
    }

    function badgeCondicion(condicion?: string): string {
      const map: Record<string, string> = {
        EN_VENTA:  'bg-primary',
        RESERVADO: 'bg-warning text-dark',
        VENDIDO:   'bg-danger',
      };
      return map[condicion ?? ''] ?? 'bg-light text-dark border';
    }

    function labelCondicion(condicion?: string): string {
      const map: Record<string, string> = {
        EN_VENTA:  'En venta',
        RESERVADO: 'Reservado',
        VENDIDO:   'Vendido',
      };
      return map[condicion ?? ''] ?? condicion ?? '—';
    }

    const traceStatus = () => {
      const estado = vehiculo.value.estado === 'NUEVO' ? 'Nuevo' : vehiculo.value.estado === 'USADO' ? 'Usado' : 'Sin estado';
      const condicion = labelCondicion(vehiculo.value.condicion);
      return `${estado} · ${condicion}`;
    };

    const traceLastAction = () => {
      if (vehiculo.value.condicion === 'VENDIDO') return 'Unidad marcada como vendida';
      if (vehiculo.value.condicion === 'RESERVADO') return 'Unidad reservada';
      if (vehiculo.value.lastModifiedDate && vehiculo.value.createdDate && vehiculo.value.lastModifiedDate !== vehiculo.value.createdDate) {
        return 'Unidad actualizada';
      }
      return 'Unidad incorporada al catalogo';
    };

    return {
      ...dateFormat,
      alertService,
      vehiculo,
      previousState,
      formatPrecio,
      formatFecha,
      badgeCondicion,
      labelCondicion,
      traceStatus,
      traceLastAction,
    };
  },
});
