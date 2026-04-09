import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type IVehiculo } from '@/shared/model/vehiculo.model';

import VehiculoService from './vehiculo.service';

export default defineComponent({
  name: 'VehiculoDetails',
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

    return {
      ...dateFormat,
      alertService,
      vehiculo,
      previousState,
      formatPrecio,
      formatFecha,
      badgeCondicion,
      labelCondicion,
    };
  },
});
