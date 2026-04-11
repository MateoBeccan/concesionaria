<template>
  <div class="d-grid gap-3">
    <div v-if="vehiculo" class="border rounded-3 p-3 bg-light">
      <div class="d-flex justify-content-between align-items-start gap-3">
        <div>
          <div class="fw-semibold">{{ vehiculo.patente }}</div>
          <div class="text-muted small">
            {{ vehiculo.version?.nombre ?? 'Sin versión' }}{{ vehiculo.precio != null ? ` · $ ${formatPrecio(vehiculo.precio)}` : '' }}
          </div>
        </div>
        <div class="d-flex gap-2 align-items-center">
          <span class="badge" :class="badgeCondicion(vehiculo.condicion)">{{ vehiculo.condicion ?? 'N/D' }}</span>
          <button class="btn btn-sm btn-outline-secondary" @click="limpiarSeleccion">Cambiar</button>
        </div>
      </div>
    </div>

    <template v-else>
      <div class="d-flex gap-2">
        <input v-model="patente" class="form-control text-uppercase" placeholder="ABC123 o AB123CD" @keyup.enter="buscar" />
        <button class="btn btn-primary" type="button" :disabled="loading || !patente.trim()" @click="buscar">Buscar</button>
      </div>

      <div v-if="loading" class="text-muted small">Buscando vehículo...</div>
      <div v-else-if="error" class="alert alert-danger py-2 mb-0">{{ error }}</div>
      <div v-else-if="notFound" class="alert alert-warning mb-0 d-flex justify-content-between align-items-center">
        <span>No encontramos el vehículo "{{ patente.toUpperCase() }}".</span>
        <button class="btn btn-sm btn-success" type="button" @click="mostrarAlta = true">+ Registrar vehículo</button>
      </div>
    </template>

    <div class="d-flex justify-content-between gap-2 pt-2">
      <button class="btn btn-outline-secondary" type="button" @click="emit('atras')">Atrás</button>
      <button class="btn btn-primary" type="button" :disabled="!vehiculo || vehiculo.condicion === 'VENDIDO'" @click="confirmarSeleccion">
        Continuar
      </button>
    </div>

    <VehiculoQuickCreate
      v-if="mostrarAlta"
      :patente-inicial="patente.toUpperCase()"
      @cerrar="mostrarAlta = false"
      @guardado="onVehiculoGuardado"
    />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import VehiculoQuickCreate from '@/entities/vehiculo/VehiculoQuickCreate.vue';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import { useVehiculo } from '@/shared/composables';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

const props = defineProps<{
  initialVehiculoId?: number | null;
}>();

const emit = defineEmits<{ seleccionado: [vehiculo: IVehiculo]; atras: [] }>();

const patente = ref('');
const mostrarAlta = ref(false);
const vehiculoService = new VehiculoService();

const { vehiculo, loading, error, notFound, buscarPorPatente, setVehiculo, limpiar } = useVehiculo();

async function buscar() {
  await buscarPorPatente(patente.value);
}

function limpiarSeleccion() {
  patente.value = '';
  limpiar();
}

function confirmarSeleccion() {
  if (!vehiculo.value || vehiculo.value.condicion === 'VENDIDO') return;
  emit('seleccionado', vehiculo.value);
}

function onVehiculoGuardado(nuevoVehiculo: IVehiculo) {
  mostrarAlta.value = false;
  patente.value = nuevoVehiculo.patente ?? '';
  setVehiculo(nuevoVehiculo);
}

function badgeCondicion(condicion?: string | null) {
  const map: Record<string, string> = {
    EN_VENTA: 'bg-primary',
    RESERVADO: 'bg-warning text-dark',
    VENDIDO: 'bg-danger',
  };
  return map[condicion ?? ''] ?? 'bg-light text-dark border';
}

function formatPrecio(precio?: number | null) {
  return Number(precio ?? 0).toLocaleString('es-AR');
}

onMounted(async () => {
  if (!props.initialVehiculoId) return;
  try {
    const existingVehiculo = await vehiculoService.find(props.initialVehiculoId);
    patente.value = existingVehiculo.patente ?? '';
    setVehiculo(existingVehiculo);
  } catch {
    // Ignora IDs inválidos y deja el paso en búsqueda manual.
  }
});
</script>
