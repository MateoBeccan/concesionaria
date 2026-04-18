<template>
  <div class="vehiculo-search-page">
    <section class="hero">
      <p class="hero-kicker">Operacion comercial</p>
      <h1 class="hero-title">Buscar vehiculo</h1>
      <p class="hero-subtitle">Busca por patente y ejecuta acciones operativas sin salir del flujo.</p>
    </section>

    <section class="surface">
      <div class="search-row">
        <EntitySearchInput
          v-model="patente"
          placeholder="ABC123 o AB123CD"
          :loading="loading"
          :error="error"
          :debounce="0"
          @clear="limpiar"
        />
        <button class="btn btn-primary px-4" :disabled="loading || !patente.trim()" @click="buscar">Buscar</button>
      </div>
    </section>

    <section v-if="vehiculo && modo === 'EXISTENTE'" class="surface">
      <div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
        <div>
          <h2 class="h4 mb-1">{{ vehiculo.patente || 'Sin patente' }}</h2>
          <p class="text-muted mb-0">
            {{ vehiculo.version?.modelo?.marca?.nombre ?? '-' }}
            {{ vehiculo.version?.modelo?.nombre ?? '' }}
            {{ vehiculo.version?.nombre ?? '' }}
          </p>
        </div>
        <div class="d-flex gap-2">
          <span class="badge text-bg-secondary">{{ vehiculo.estado ?? 'Sin estado' }}</span>
          <span class="badge" :class="badgeCondicion(vehiculo.condicion)">{{ labelCondicion(vehiculo.condicion) }}</span>
        </div>
      </div>

      <div class="price mt-3">$ {{ formatPrecio(vehiculo.precio) }}</div>

      <div class="spec-grid mt-3">
        <div class="spec-card">
          <small>Kilometros</small>
          <strong>{{ vehiculo.km?.toLocaleString('es-AR') ?? '-' }} km</strong>
        </div>
        <div class="spec-card">
          <small>Motor</small>
          <strong>{{ vehiculo.motor?.nombre ?? '-' }}</strong>
        </div>
        <div class="spec-card">
          <small>Potencia</small>
          <strong>{{ vehiculo.motor?.potenciaHp ? `${vehiculo.motor.potenciaHp} HP` : '-' }}</strong>
        </div>
        <div class="spec-card">
          <small>Tipo</small>
          <strong>{{ vehiculo.tipoVehiculo?.nombre ?? '-' }}</strong>
        </div>
      </div>

      <div class="d-flex flex-wrap gap-2 mt-4">
        <button v-if="vehiculo.condicion !== 'VENDIDO'" class="btn btn-success" @click="irAVenta">Vender</button>
        <button class="btn btn-outline-primary" @click="editar">Editar</button>
      </div>
      <div class="alert alert-info mt-4 mb-0 py-2">
        Las reservas se gestionan desde el flujo de venta con seña minima registrada.
      </div>
    </section>

    <section v-if="modo === 'NO_ENCONTRADO'" class="surface warning">
      <strong>Vehiculo no encontrado</strong>
      <p class="mb-3">No existe un vehiculo con patente "{{ patente.toUpperCase() }}".</p>
      <button class="btn btn-success btn-sm" @click="modo = 'CREAR'">Registrar vehiculo</button>
    </section>

    <section v-if="modo === 'CREAR'" class="surface">
      <VehiculoQuickCreate :patente-inicial="patente" @guardado="onVehiculoCreado" @cerrar="modo = 'BUSCAR'" />
    </section>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import { useAlertService } from '@/shared/alert/alert.service';
import EntitySearchInput from '@/shared/composables/EntitySearchInput.vue';
import { useVehiculo } from '@/shared/composables/useVehiculo';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

import VehiculoQuickCreate from './VehiculoQuickCreate.vue';

const router = useRouter();
const alertService = useAlertService();
const vehiculoService = new VehiculoService();
const { vehiculo, loading, error, notFound, buscarPorPatente, setVehiculo, limpiar: limpiarComposable } = useVehiculo();

const patente = ref('');
const modo = ref<'BUSCAR' | 'EXISTENTE' | 'NO_ENCONTRADO' | 'CREAR'>('BUSCAR');

watch(vehiculo, value => {
  if (value) {
    modo.value = 'EXISTENTE';
  }
});

watch(notFound, value => {
  if (value) {
    modo.value = 'NO_ENCONTRADO';
  }
});

async function buscar() {
  await buscarPorPatente(patente.value);
}

function limpiar() {
  limpiarComposable();
  modo.value = 'BUSCAR';
}

async function onVehiculoCreado(value: IVehiculo) {
  patente.value = value.patente ?? '';

  try {
    if (value.id) {
      const vehiculoCompleto = await vehiculoService.find(value.id);
      setVehiculo(vehiculoCompleto);
    } else if (patente.value.trim()) {
      await buscarPorPatente(patente.value);
    } else {
      setVehiculo(value);
    }
  } catch {
    setVehiculo(value);
  }

  modo.value = 'EXISTENTE';
  alertService.showSuccess('Vehiculo registrado correctamente');
}

function irAVenta() {
  if (!vehiculo.value?.id) return;
  router.push({ name: 'VentaEditor', query: { vehiculoId: vehiculo.value.id } });
}

function editar() {
  if (!vehiculo.value?.id) return;
  router.push({ name: 'VehiculoEdit', params: { vehiculoId: vehiculo.value.id } });
}

function badgeCondicion(condicion?: string) {
  const map: Record<string, string> = {
    EN_VENTA: 'text-bg-primary',
    RESERVADO: 'text-bg-warning',
    VENDIDO: 'text-bg-danger',
  };
  return map[condicion ?? ''] ?? 'text-bg-light';
}

function labelCondicion(condicion?: string) {
  const map: Record<string, string> = {
    EN_VENTA: 'En venta',
    RESERVADO: 'Reservado',
    VENDIDO: 'Vendido',
  };
  return map[condicion ?? ''] ?? 'Sin condicion';
}

function formatPrecio(precio?: number | null) {
  return Number(precio ?? 0).toLocaleString('es-AR');
}
</script>

<style scoped>
.vehiculo-search-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 1rem 0;
  display: grid;
  gap: 1rem;
}

.surface {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 1rem;
}

.hero {
  background: linear-gradient(135deg, #f3f7ff 0%, #f9fafb 100%);
  border: 1px solid #dbe4f0;
  border-radius: 14px;
  padding: 1rem;
}

.hero-kicker {
  margin: 0;
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #4b5563;
}

.hero-title {
  margin: 0.25rem 0;
  font-size: 1.6rem;
}

.hero-subtitle {
  margin: 0;
  color: #4b5563;
}

.search-row {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: 1fr auto;
}

.price {
  font-size: 1.65rem;
  font-weight: 700;
  color: #0f172a;
}

.spec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 0.75rem;
}

.spec-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0.6rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.spec-card small {
  color: #6b7280;
}

.warning {
  border-color: #f59e0b;
  background: #fffbeb;
}

@media (max-width: 768px) {
  .search-row {
    grid-template-columns: 1fr;
  }
}
</style>
