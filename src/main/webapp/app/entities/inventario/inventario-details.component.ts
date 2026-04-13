import { computed, defineComponent, inject, ref, type Ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import dayjs from 'dayjs';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { CondicionVehiculo } from '@/shared/model/enumerations/condicion-vehiculo.model';
import { EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import type { IInventario } from '@/shared/model/inventario.model';

import InventarioService from './inventario.service';

function mapEstadoToCondicion(estado?: keyof typeof EstadoInventario | null): keyof typeof CondicionVehiculo | null {
  if (estado === EstadoInventario.DISPONIBLE) return CondicionVehiculo.EN_VENTA;
  if (estado === EstadoInventario.RESERVADO) return CondicionVehiculo.RESERVADO;
  if (estado === EstadoInventario.VENDIDO) return CondicionVehiculo.VENDIDO;
  return null;
}

export default defineComponent({
  name: 'InventarioDetails',
  setup() {
    const dateFormat = useDateFormat();
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

    const reservaVencida = computed(() => {
      if (inventario.value.estadoInventario !== EstadoInventario.RESERVADO || !inventario.value.fechaVencimientoReserva) return false;
      return dayjs(inventario.value.fechaVencimientoReserva).isBefore(dayjs());
    });

    const incoherencias = computed(() => {
      const issues: string[] = [];
      const expectedCondicion = mapEstadoToCondicion(inventario.value.estadoInventario);

      if (expectedCondicion && inventario.value.vehiculo?.condicion && inventario.value.vehiculo.condicion !== expectedCondicion) {
        issues.push(`La condición del vehículo no coincide con el estado del inventario. Debería ser ${expectedCondicion}.`);
      }

      if (inventario.value.estadoInventario === EstadoInventario.DISPONIBLE && !inventario.value.disponible) {
        issues.push('El inventario figura DISPONIBLE pero la unidad no está marcada como disponible.');
      }

      if (inventario.value.estadoInventario === EstadoInventario.RESERVADO) {
        if (!inventario.value.clienteReserva?.id) issues.push('La reserva no tiene cliente asociado.');
        if (!inventario.value.fechaReserva) issues.push('La reserva no tiene fecha de inicio.');
        if (!inventario.value.fechaVencimientoReserva) issues.push('La reserva no tiene fecha de vencimiento.');
      }

      if (inventario.value.estadoInventario === EstadoInventario.VENDIDO && inventario.value.disponible) {
        issues.push('La unidad figura vendida pero todavía aparece disponible.');
      }

      return issues;
    });

    return {
      ...dateFormat,
      alertService,
      inventario,
      reservaVencida,
      incoherencias,
      previousState,
    };
  },
});
