<template>
  <div>
    <div v-if="!mostrandoSelector" class="mb-3">
      <button class="btn btn-outline-primary btn-sm" @click="mostrandoSelector = true">+ Agregar vehiculo</button>
    </div>

    <div v-else class="card mb-3 border-primary" style="border-style: dashed !important">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <span class="fw-semibold small text-primary">Buscar vehiculo</span>
          <button class="btn-close btn-sm" @click="cerrarSelector" />
        </div>

        <div class="d-flex gap-2 mb-2">
          <div class="flex-grow-1">
            <input
              v-model="busquedaPatente"
              class="form-control form-control-sm"
              placeholder="Patente (ej: ABC123)"
              @keyup.enter="buscarVehiculo"
              @input="busquedaPatente = busquedaPatente.toUpperCase()"
            />
          </div>
          <button class="btn btn-primary btn-sm" @click="buscarVehiculo" :disabled="buscando">
            <span v-if="buscando" class="spinner-border spinner-border-sm" />
            <span v-else>Buscar</span>
          </button>
        </div>

        <div v-if="errorBusqueda" class="alert alert-warning py-2 small mb-2">
          {{ errorBusqueda }}
        </div>

        <div v-if="vehiculoEncontrado" class="border rounded p-2 bg-light">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <span class="fw-semibold">{{ vehiculoEncontrado.patente }}</span>
              <span class="text-muted ms-2 small">
                {{ vehiculoEncontrado.version?.modelo?.marca?.nombre }}
                {{ vehiculoEncontrado.version?.modelo?.nombre }}
                {{ vehiculoEncontrado.version?.nombre }}
              </span>
              <span class="badge ms-2" :class="vehiculoEncontrado.estado === 'NUEVO' ? 'bg-success' : 'bg-secondary'">
                {{ vehiculoEncontrado.estado }}
              </span>
              <span class="badge ms-1" :class="badgeStock(vehiculoEncontrado.estadoInventario)">
                {{ vehiculoEncontrado.estadoInventario ?? 'SIN_ESTADO' }}
              </span>
            </div>
            <div class="text-end">
              <div class="fw-bold text-primary">
                {{ vehiculoEncontrado.moneda?.simbolo ?? '$' }} {{ fmt(vehiculoEncontrado.precio) }} {{ vehiculoEncontrado.moneda?.codigo ?? '' }}
              </div>
              <button class="btn btn-success btn-sm mt-1" @click="seleccionarVehiculo" :disabled="vehiculoEncontrado.estadoInventario === 'VENDIDO'">
                Agregar
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="detalles.length === 0" class="text-center py-3 text-muted small border rounded">
      No hay vehiculos agregados. Usa el boton para agregar.
    </div>

    <div v-else class="table-responsive">
      <table class="table align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Patente</th>
            <th>Vehiculo</th>
            <th>Estado</th>
            <th style="width: 160px">Precio base</th>
            <th class="text-end">Subtotal base</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in detalles" :key="d._key">
            <td class="fw-semibold">{{ d.vehiculo.patente }}</td>
            <td class="text-muted small">
              {{ d.vehiculo.version?.modelo?.marca?.nombre ?? '' }}
              {{ d.vehiculo.version?.modelo?.nombre ?? '' }}
              {{ d.vehiculo.version?.nombre ?? '' }}
            </td>
            <td>
              <span class="badge" :class="d.vehiculo.estado === 'NUEVO' ? 'bg-success' : 'bg-secondary'">
                {{ d.vehiculo.estado }}
              </span>
            </td>
            <td>
              <div class="input-group input-group-sm">
                <span class="input-group-text">{{ d.vehiculo.moneda?.simbolo ?? '$' }}</span>
                <input type="number" class="form-control form-control-sm" :value="d.precioUnitario" readonly min="0" step="0.01" />
              </div>
            </td>
            <td class="text-end fw-semibold" style="color: var(--color-primary)">
              {{ d.vehiculo.moneda?.simbolo ?? '$' }} {{ fmt(d.subtotal) }} {{ d.vehiculo.moneda?.codigo ?? '' }}
            </td>
            <td class="text-end">
              <button class="btn btn-sm btn-outline-danger" @click="emit('quitar', d._key)" title="Quitar">X</button>
            </td>
          </tr>
        </tbody>
        <tfoot class="table-light">
          <tr>
            <td colspan="4" class="text-end fw-semibold">Subtotal:</td>
            <td class="text-end fw-bold" style="color: var(--color-primary)">
              {{ props.monedaVenta?.simbolo ?? '$' }} {{ fmt(props.sumaSubtotales) }} {{ props.monedaVenta?.codigo ?? '' }}
            </td>
            <td />
          </tr>
        </tfoot>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import axios from 'axios';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { DetalleLocal } from './useVentaEditor';

const props = defineProps<{
  detalles: DetalleLocal[];
  sumaSubtotales: number;
  monedaVenta?: IMoneda | null;
}>();

const emit = defineEmits<{
  agregar: [v: IVehiculo];
  quitar: [key: string];
  'actualizar-precio': [key: string, precio: number];
}>();

const mostrandoSelector = ref(false);
const busquedaPatente = ref('');
const vehiculoEncontrado = ref<IVehiculo | null>(null);
const buscando = ref(false);
const errorBusqueda = ref<string | null>(null);

async function buscarVehiculo() {
  const p = busquedaPatente.value.trim().toUpperCase();
  if (!p) return;
  buscando.value = true;
  errorBusqueda.value = null;
  vehiculoEncontrado.value = null;
  try {
    const res = await axios.get<IVehiculo>(`api/vehiculos/patente/${p}`);
    vehiculoEncontrado.value = res.data;
  } catch (e: any) {
    errorBusqueda.value = e.response?.status === 404 ? `No se encontro ningun vehiculo con patente "${p}"` : 'Error al buscar el vehiculo';
  } finally {
    buscando.value = false;
  }
}

function seleccionarVehiculo() {
  if (vehiculoEncontrado.value) {
    emit('agregar', vehiculoEncontrado.value);
    cerrarSelector();
  }
}

function cerrarSelector() {
  mostrandoSelector.value = false;
  busquedaPatente.value = '';
  vehiculoEncontrado.value = null;
  errorBusqueda.value = null;
}

function badgeStock(c?: string) {
  return { DISPONIBLE: 'bg-primary', RESERVADO: 'bg-warning text-dark', VENDIDO: 'bg-danger' }[c ?? ''] ?? 'bg-light text-dark border';
}

function fmt(n?: number | null) {
  return Number(n ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2 });
}
</script>
