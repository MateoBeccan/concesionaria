<template>
  <div>
    <p class="text-muted mb-4">Revisá los datos antes de confirmar la venta.</p>

    <!-- RESUMEN CLIENTE -->
    <div class="card mb-3 border-0 shadow-sm">
      <div class="card-body">
        <div class="d-flex align-items-center gap-2 mb-2">
          <span class="badge bg-primary">Cliente</span>
        </div>
        <h5 class="mb-1">{{ cliente.nombre }} {{ cliente.apellido }}</h5>
        <p class="text-muted mb-0 small">
          <span class="me-3">📄 {{ cliente.nroDocumento }}</span>
          <span v-if="cliente.email">✉️ {{ cliente.email }}</span>
        </p>
      </div>
    </div>

    <!-- RESUMEN VEHÍCULO -->
    <div class="card mb-3 border-0 shadow-sm">
      <div class="card-body">
        <div class="d-flex align-items-center gap-2 mb-2">
          <span class="badge bg-secondary">Vehículo</span>
          <span class="badge" :class="vehiculo.estado === 'NUEVO' ? 'bg-success' : 'bg-warning text-dark'">
            {{ vehiculo.estado }}
          </span>
        </div>
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <h5 class="mb-1 fw-bold">{{ vehiculo.patente }}</h5>
            <p class="text-muted mb-0 small">
              {{ vehiculo.version?.modelo?.marca?.nombre }} {{ vehiculo.version?.modelo?.nombre }} {{ vehiculo.version?.nombre }}
            </p>
          </div>
          <h4 class="text-primary mb-0">$ {{ formatPrecio(vehiculo.precio) }}</h4>
        </div>
      </div>
    </div>

    <!-- TOTAL -->
    <div class="card bg-light border-0 mb-4">
      <div class="card-body d-flex justify-content-between align-items-center">
        <span class="fw-semibold text-muted">Total de la operación</span>
        <h3 class="mb-0 text-primary fw-bold">$ {{ formatPrecio(vehiculo.precio) }}</h3>
      </div>
    </div>

    <!-- NAVEGACIÓN -->
    <div class="d-flex justify-content-between">
      <button class="btn btn-outline-secondary" @click="emit('atras')" :disabled="guardando">
        ← Atrás
      </button>
      <button class="btn btn-success px-5" @click="emit('confirmar')" :disabled="guardando">
        <span v-if="guardando" class="spinner-border spinner-border-sm me-2" />
        <span v-else>✓</span>
        {{ guardando ? 'Procesando...' : 'Confirmar venta' }}
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

const emit = defineEmits<{
  confirmar: [];
  atras: [];
}>();

function formatPrecio(precio?: number | null) {
  return Number(precio ?? 0).toLocaleString('es-AR');
}
</script>
