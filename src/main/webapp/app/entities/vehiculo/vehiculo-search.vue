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
        <button v-if="vehiculo.condicion === 'EN_VENTA'" class="btn btn-warning" @click="abrirReserva">Reservar</button>
        <button
          v-if="vehiculo.condicion === 'RESERVADO'"
          class="btn btn-outline-warning"
          :disabled="reserving"
          @click="liberarReserva"
        >
          Liberar reserva
        </button>
        <button class="btn btn-outline-primary" @click="editar">Editar</button>
      </div>

      <div v-if="mostrarReserva" class="reserve-panel mt-4">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h3 class="h6 mb-0">Asignar reserva</h3>
          <button class="btn btn-sm btn-outline-secondary" @click="cerrarReserva">Cerrar</button>
        </div>

        <p class="text-muted small mb-3">Selecciona el cliente para reservar esta unidad.</p>

        <input
          v-model="clienteQuery"
          class="form-control"
          placeholder="Buscar cliente por nombre, DNI o email"
          @input="buscarClientes"
        />

        <div v-if="clientesLoading" class="small text-muted mt-2">Buscando clientes...</div>
        <div v-else-if="clienteQuery.trim().length >= 2 && clientes.length === 0" class="small text-muted mt-2">
          No hay resultados para la busqueda.
        </div>

        <ul v-if="clientes.length > 0" class="client-list mt-3">
          <li
            v-for="cliente in clientes"
            :key="cliente.id"
            class="client-item"
            :class="{ active: selectedCliente?.id === cliente.id }"
            @click="selectedCliente = cliente"
          >
            <div>
              <strong>{{ cliente.apellido }}, {{ cliente.nombre }}</strong>
              <div class="small text-muted">{{ cliente.nroDocumento ?? 'Sin documento' }}</div>
            </div>
            <span class="small text-muted">{{ cliente.email ?? cliente.telefono ?? 'Sin contacto' }}</span>
          </li>
        </ul>

        <div class="d-flex flex-wrap gap-2 mt-3">
          <router-link :to="{ name: 'ClienteCreate' }" class="btn btn-sm btn-outline-secondary">Nuevo cliente</router-link>
          <button class="btn btn-primary btn-sm" :disabled="!selectedCliente?.id || reserving" @click="confirmarReserva">
            <span v-if="reserving" class="spinner-border spinner-border-sm me-1" />
            Confirmar reserva
          </button>
        </div>
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

import ClienteService from '@/entities/cliente/cliente.service';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import { useAlertService } from '@/shared/alert/alert.service';
import EntitySearchInput from '@/shared/composables/EntitySearchInput.vue';
import { useVehiculo } from '@/shared/composables/useVehiculo';
import type { ICliente } from '@/shared/model/cliente.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

import VehiculoQuickCreate from './VehiculoQuickCreate.vue';

const router = useRouter();
const alertService = useAlertService();
const clienteService = new ClienteService();
const vehiculoService = new VehiculoService();
const { vehiculo, loading, error, notFound, buscarPorPatente, setVehiculo, limpiar: limpiarComposable } = useVehiculo();

const patente = ref('');
const modo = ref<'BUSCAR' | 'EXISTENTE' | 'NO_ENCONTRADO' | 'CREAR'>('BUSCAR');

const mostrarReserva = ref(false);
const reserving = ref(false);
const clienteQuery = ref('');
const clientes = ref<ICliente[]>([]);
const selectedCliente = ref<ICliente | null>(null);
const clientesLoading = ref(false);
let clientesDebounce: ReturnType<typeof setTimeout> | null = null;

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
  if (!vehiculo.value) {
    mostrarReserva.value = false;
  }
}

function limpiar() {
  limpiarComposable();
  modo.value = 'BUSCAR';
  mostrarReserva.value = false;
  clienteQuery.value = '';
  clientes.value = [];
  selectedCliente.value = null;
}

function onVehiculoCreado(value: IVehiculo) {
  setVehiculo(value);
  patente.value = value.patente ?? '';
  modo.value = 'EXISTENTE';
  alertService.showSuccess('Vehiculo registrado correctamente');
}

function irAVenta() {
  if (!vehiculo.value?.id) return;
  router.push({ name: 'VentaEditor', query: { vehiculoId: vehiculo.value.id } });
}

function abrirReserva() {
  mostrarReserva.value = true;
}

function cerrarReserva() {
  mostrarReserva.value = false;
  clienteQuery.value = '';
  clientes.value = [];
  selectedCliente.value = null;
}

async function buscarClientes() {
  if (clientesDebounce) {
    clearTimeout(clientesDebounce);
  }

  clientesDebounce = setTimeout(async () => {
    const q = clienteQuery.value.trim();
    if (q.length < 2) {
      clientes.value = [];
      return;
    }

    clientesLoading.value = true;
    try {
      clientes.value = await clienteService.buscarPorQuery(q);
    } catch (e: any) {
      clientes.value = [];
      alertService.showHttpError(e.response);
    } finally {
      clientesLoading.value = false;
    }
  }, 250);
}

async function confirmarReserva() {
  if (!vehiculo.value?.id || !selectedCliente.value?.id) return;

  reserving.value = true;
  try {
    await vehiculoService.reservar(vehiculo.value.id, selectedCliente.value.id);
    alertService.showSuccess(`Reserva asignada a ${selectedCliente.value.apellido}, ${selectedCliente.value.nombre}`);
    cerrarReserva();
    await buscar();
  } catch (e: any) {
    alertService.showHttpError(e.response);
  } finally {
    reserving.value = false;
  }
}

async function liberarReserva() {
  if (!vehiculo.value?.id) return;
  reserving.value = true;
  try {
    await vehiculoService.cancelarReserva(vehiculo.value.id);
    alertService.showSuccess('Reserva liberada correctamente');
    await buscar();
  } catch (e: any) {
    alertService.showHttpError(e.response);
  } finally {
    reserving.value = false;
  }
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

.reserve-panel {
  border-top: 1px solid #e5e7eb;
  padding-top: 1rem;
}

.client-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 0.5rem;
}

.client-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0.65rem 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.client-item.active {
  border-color: #1d4ed8;
  background: #eff6ff;
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
