<template>
  <div class="d-grid gap-3">
    <div class="border rounded-3 p-3">
      <div class="fw-semibold mb-2">Cliente</div>
      <div>{{ cliente.nombre }} {{ cliente.apellido }}</div>
      <div class="text-muted small">{{ cliente.nroDocumento }}{{ cliente.email ? ` · ${cliente.email}` : '' }}</div>
    </div>

    <div class="border rounded-3 p-3">
      <div class="fw-semibold mb-2">Vehículo</div>
      <div>{{ vehiculo.patente }}</div>
      <div class="text-muted small">
        {{ vehiculo.version?.nombre ?? 'Sin versión' }}{{ vehiculo.precio != null ? ` · $ ${formatPrecio(vehiculo.precio)}` : '' }}
      </div>
    </div>

    <div class="alert alert-info mb-0">Se va a registrar la venta del vehículo seleccionado para el cliente indicado.</div>

    <div class="d-flex justify-content-between gap-2 pt-2">
      <button class="btn btn-outline-secondary" type="button" @click="emit('atras')">Atrás</button>
      <button class="btn btn-success" type="button" :disabled="guardando" @click="emit('confirmar')">
        <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
        Confirmar venta
      </button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { ICliente } from '@/shared/model/cliente.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

defineProps<{
  cliente: ICliente;
  vehiculo: IVehiculo;
  guardando: boolean;
}>();

const emit = defineEmits<{ confirmar: []; atras: [] }>();

function formatPrecio(precio?: number | null) {
  return Number(precio ?? 0).toLocaleString('es-AR');
}
</script>
