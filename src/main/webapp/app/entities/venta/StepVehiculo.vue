<template>
  <div>
    <p class="text-muted mb-3">Ingresá la patente del vehículo (formato: ABC123 o AB123CD).</p>

    <div class="d-flex gap-2 mb-2">
      <div class="flex-grow-1">
        <EntitySearchInput
          v-model="patente"
          placeholder="Ej: ABC123"
          :loading="loading"
          :error="error"
          :debounce="0"
          @clear="limpiarBusqueda"
        />
      </div>
      <button class="btn btn-primary" @click="buscar" :disabled="loading || !patente">
        <span v-if="loading" class="spinner-border spinner-border-sm me-1" />
        Buscar
      </button>
    </div>

    <!-- VEHÍCULO ENCONTRADO -->
    <div v-if="vehiculo" class="mt-3">

      <div v-if="esVendido(vehiculo)" class="alert alert-danger d-flex align-items-center gap-2">
        <span style="font-size:1.2rem">🚫</span>
        <div>
          <strong>Vehículo no disponible</strong>
          <p class="mb-0 small">Este vehículo ya fue vendido y no puede seleccionarse.</p>
        </div>
      </div>

      <div v-else class="card border-0 shadow-sm mt-2">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <h5 class="mb-0 fw-bold">{{ vehiculo.patente }}</h5>
            <div class="d-flex gap-2">
              <span class="badge" :class="badgeEstado(vehiculo.estado)">{{ vehiculo.estado }}</span>
              <span class="badge" :class="badgeCondicion(vehiculo.condicion)">{{ vehiculo.condicion }}</span>
            </div>
          </div>
          <h4 class="text-primary mb-3">$ {{ formatPrecio(vehiculo.precio) }}</h4>
          <div class="row small text-muted">
            <div class="col-6">
              <p class="mb-1"><strong>Km:</strong> {{ vehiculo.km?.toLocaleString('es-AR') ?? '-' }}</p>
              <p class="mb-1"><strong>Tipo:</strong> {{ vehiculo.tipoVehiculo?.nombre ?? '-' }}</p>
            </div>
            <div class="col-6">
              <p class="mb-1"><strong>Versión:</strong> {{ vehiculo.version?.nombre ?? '-' }}</p>
              <p class="mb-1"><strong>Marca:</strong> {{ vehiculo.version?.modelo?.marca?.nombre ?? '-' }}</p>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- NO ENCONTRADO -->
    <div v-if="notFound && !loading" class="alert alert-warning mt-3 d-flex justify-content-between align-items-center">
      <div>
        <strong>Vehículo no encontrado</strong>
        <p class="mb-0 small">No existe ningún vehículo con patente "{{ patente }}"</p>
      </div>
      <button class="btn btn-sm btn-success" @click="mostrarModal = true">
        + Registrar vehículo
      </button>
    </div>

    <!-- NAVEGACIÓN -->
    <div class="d-flex justify-content-between mt-4">
      <button class="btn btn-outline-secondary" @click="emit('atras')">← Atrás</button>
      <button
        class="btn btn-primary px-4"
        :disabled="!vehiculo || esVendido(vehiculo)"
        @click="emit('seleccionado', vehiculo!)"
      >
        Siguiente →
      </button>
    </div>

    <VehiculoQuickCreate
      v-if="mostrarModal"
      :patente-inicial="patente"
      @guardado="onVehiculoCreado"
      @cerrar="mostrarModal = false"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useVehiculo } from '@/shared/composables/useVehiculo';
import EntitySearchInput from '@/shared/composables/EntitySearchInput.vue';
import VehiculoQuickCreate from '@/entities/vehiculo/VehiculoQuickCreate.vue';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

const emit = defineEmits<{ seleccionado: [v: IVehiculo]; atras: [] }>();

const patente = ref('');
const mostrarModal = ref(false);
const { vehiculo, loading, error, notFound, buscarPorPatente, setVehiculo, esVendido, limpiar } = useVehiculo();

async function buscar() {
  await buscarPorPatente(patente.value);
}

function limpiarBusqueda() {
  patente.value = '';
  limpiar();
}

function onVehiculoCreado(v: IVehiculo) {
  mostrarModal.value = false;
  setVehiculo(v);
  patente.value = v.patente ?? '';
}

function badgeEstado(estado?: string) {
  return estado === 'NUEVO' ? 'bg-success' : 'bg-secondary';
}

function badgeCondicion(condicion?: string) {
  const map: Record<string, string> = { EN_VENTA: 'bg-primary', RESERVADO: 'bg-warning text-dark', VENDIDO: 'bg-danger' };
  return map[condicion ?? ''] ?? 'bg-light text-dark';
}

function formatPrecio(precio?: number | null) {
  return Number(precio ?? 0).toLocaleString('es-AR');
}
</script>
