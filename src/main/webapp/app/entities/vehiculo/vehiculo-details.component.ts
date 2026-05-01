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

    function formatPrecio(precio?: number | null, simbolo?: string | null, codigo?: string | null): string {
      const monto = Number(precio ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      const simboloMoneda = simbolo ? `${simbolo} ` : '$ ';
      const codigoMoneda = codigo ?? 'ARS';
      return `${simboloMoneda}${monto} ${codigoMoneda}`;
    }

    function formatFecha(fecha?: Date | string | null): string {
      if (!fecha) return '—';
      return new Date(fecha).toLocaleDateString('es-AR');
    }

    function badgeStock(estadoInventario?: string): string {
      const map: Record<string, string> = {
        DISPONIBLE: 'bg-primary',
        RESERVADO: 'bg-warning text-dark',
        VENDIDO: 'bg-danger',
      };
      return map[estadoInventario ?? ''] ?? 'bg-light text-dark border';
    }

    function labelStock(estadoInventario?: string): string {
      const map: Record<string, string> = {
        DISPONIBLE: 'Disponible',
        RESERVADO: 'Reservado',
        VENDIDO: 'Vendido',
      };
      return map[estadoInventario ?? ''] ?? estadoInventario ?? '—';
    }

    const traceStatus = () => {
      const estado = vehiculo.value.estado === 'NUEVO' ? 'Nuevo' : vehiculo.value.estado === 'USADO' ? 'Usado' : 'Sin estado';
      const stock = labelStock(vehiculo.value.estadoInventario);
      return `${estado} · ${stock}`;
    };

    const traceLastAction = () => {
      if (vehiculo.value.estadoInventario === 'VENDIDO') return 'Unidad marcada como vendida';
      if (vehiculo.value.estadoInventario === 'RESERVADO') return 'Unidad reservada';
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
      badgeStock,
      labelStock,
      traceStatus,
      traceLastAction,
    };
  },
});
