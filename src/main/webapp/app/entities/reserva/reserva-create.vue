<template>
  <div class="container py-4" style="max-width: 980px">
    <div class="d-flex justify-content-between align-items-start mb-3">
      <div>
        <h4 class="mb-0">Nueva reserva</h4>
        <small class="text-muted">Inventario -> Reserva</small>
      </div>
      <router-link :to="{ name: 'Reserva' }" class="btn btn-outline-secondary">Volver</router-link>
    </div>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Inventario</label>
            <input class="form-control" :value="inventario?.id ? `#${inventario.id}` : '-'" readonly />
          </div>

          <div class="col-md-6">
            <label class="form-label">Unidad / Patente</label>
            <input class="form-control" :value="vehiculoLabel" readonly />
          </div>

          <div class="col-md-6">
            <label class="form-label">Cliente <span class="text-danger">*</span></label>
            <div v-if="form.cliente">
              <div class="d-flex align-items-center gap-2 rounded border bg-light p-2">
                <div class="flex-grow-1">
                  <span class="fw-semibold">{{ form.cliente.nombre }} {{ form.cliente.apellido }}</span>
                  <span class="text-muted ms-2 small">{{ form.cliente.nroDocumento }}</span>
                </div>
                <button class="btn btn-sm btn-outline-secondary" @click="form.cliente = null">Cambiar</button>
              </div>
            </div>
            <div v-else>
              <input v-model="busquedaCliente" class="form-control" placeholder="Buscar por nombre, DNI o email" @input="buscarClientes" />
              <ul v-if="resultadosCliente.length" class="list-group mt-1 shadow-sm">
                <li
                  v-for="clienteItem in resultadosCliente"
                  :key="clienteItem.id"
                  class="list-group-item list-group-item-action d-flex justify-content-between"
                  style="cursor: pointer"
                  @click="seleccionarCliente(clienteItem)"
                >
                  <span class="fw-semibold">{{ clienteItem.nombre }} {{ clienteItem.apellido }}</span>
                  <span class="text-muted small">{{ clienteItem.nroDocumento }}</span>
                </li>
              </ul>
            </div>
          </div>

          <div class="col-md-3">
            <label class="form-label">Fecha reserva <span class="text-danger">*</span></label>
            <input v-model="fechaReservaLocal" type="datetime-local" class="form-control" />
          </div>

          <div class="col-md-3">
            <label class="form-label">Vencimiento <span class="text-danger">*</span></label>
            <input v-model="fechaVencimientoLocal" type="datetime-local" class="form-control" />
          </div>

          <div class="col-md-4">
            <label class="form-label">Seña</label>
            <input v-model.number="form.montoSenia" type="number" min="0" step="0.01" class="form-control" />
          </div>

          <div class="col-md-4">
            <label class="form-label">Moneda</label>
            <select v-model="form.moneda" class="form-select">
              <option :value="null">Seleccionar</option>
              <option v-for="moneda in monedas" :key="moneda.id" :value="moneda">
                {{ moneda.simbolo ?? '' }} {{ moneda.codigo }} - {{ moneda.descripcion }}
              </option>
            </select>
          </div>

          <div class="col-md-12">
            <label class="form-label">Observaciones</label>
            <textarea v-model="form.observaciones" rows="2" class="form-control" />
          </div>
        </div>
      </div>
      <div class="card-footer bg-white d-flex justify-content-end gap-2">
        <router-link :to="{ name: 'Inventario' }" class="btn btn-outline-secondary">Cancelar</router-link>
        <button class="btn btn-warning" @click="guardar" :disabled="guardando || !inventario?.id">
          <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
          Guardar reserva
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { useAlertService } from '@/shared/alert/alert.service';
import type { ICliente } from '@/shared/model/cliente.model';
import type { IInventario } from '@/shared/model/inventario.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { IReserva } from '@/shared/model/reserva.model';
import InventarioService from '@/entities/inventario/inventario.service';
import ReservaService from './reserva.service';

const route = useRoute();
const router = useRouter();
const alertService = useAlertService();
const inventarioService = new InventarioService();
const reservaService = new ReservaService();

const inventario = ref<IInventario | null>(null);
const monedas = ref<IMoneda[]>([]);
const guardando = ref(false);
const error = ref<string | null>(null);
const busquedaCliente = ref('');
const resultadosCliente = ref<ICliente[]>([]);
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

const form = ref<IReserva>({
  fechaReserva: new Date(),
  fechaVencimiento: new Date(new Date().getTime() + 30 * 24 * 60 * 60 * 1000),
  montoSenia: null,
  observaciones: null,
  cliente: null,
  moneda: null,
});

const vehiculoLabel = computed(() => {
  const v = inventario.value?.vehiculo;
  if (!v) return '-';
  return `${v.patente ?? 'Sin patente'} - ${v.version?.nombre ?? 'Sin version'}`;
});

const fechaReservaLocal = computed({
  get: () => toLocalInput(form.value.fechaReserva),
  set: value => {
    form.value.fechaReserva = value ? new Date(value) : new Date();
  },
});

const fechaVencimientoLocal = computed({
  get: () => toLocalInput(form.value.fechaVencimiento),
  set: value => {
    form.value.fechaVencimiento = value ? new Date(value) : null;
  },
});

onMounted(async () => {
  const inventarioId = Number(route.query.inventarioId);
  if (!Number.isFinite(inventarioId) || inventarioId <= 0) {
    error.value = 'Debes ingresar desde una unidad de inventario valida';
    return;
  }
  try {
    inventario.value = await inventarioService.find(inventarioId);
    if (inventario.value.estadoInventario !== 'DISPONIBLE') {
      error.value = `La unidad seleccionada no esta disponible (estado: ${inventario.value.estadoInventario ?? '-'})`;
    }
    const monedasRes = await axios.get<IMoneda[]>('api/monedas?activo.equals=true&page=0&size=50');
    monedas.value = monedasRes.data;
    if (!form.value.moneda && monedas.value.length === 1) {
      form.value.moneda = monedas.value[0];
    }
  } catch (e: any) {
    error.value = 'No se pudo cargar el inventario para reservar';
    alertService.showHttpError(e?.response);
  }
});

function buscarClientes() {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(async () => {
    const q = busquedaCliente.value.trim();
    if (q.length < 2) {
      resultadosCliente.value = [];
      return;
    }
    try {
      const res = await axios.get<ICliente[]>('api/clientes/buscar', { params: { q } });
      resultadosCliente.value = res.data;
    } catch {
      resultadosCliente.value = [];
    }
  }, 300);
}

function seleccionarCliente(cliente: ICliente) {
  form.value.cliente = cliente;
  busquedaCliente.value = '';
  resultadosCliente.value = [];
}

async function guardar() {
  if (!inventario.value?.id) {
    alertService.showError('Inventario invalido para la reserva');
    return;
  }
  if (!form.value.cliente?.id) {
    alertService.showError('Debes seleccionar un cliente');
    return;
  }
  if (!form.value.fechaReserva || !form.value.fechaVencimiento) {
    alertService.showError('Debes completar fecha de reserva y vencimiento');
    return;
  }
  if (new Date(form.value.fechaVencimiento).getTime() <= new Date(form.value.fechaReserva).getTime()) {
    alertService.showError('La fecha de vencimiento debe ser mayor a la fecha de reserva');
    return;
  }
  guardando.value = true;
  try {
    const payload: IReserva = {
      fechaReserva: form.value.fechaReserva,
      fechaVencimiento: form.value.fechaVencimiento,
      montoSenia: form.value.montoSenia ?? null,
      observaciones: form.value.observaciones ?? null,
      cliente: { id: form.value.cliente.id },
      inventario: { id: inventario.value.id },
      moneda: form.value.moneda?.id ? { id: form.value.moneda.id } : null,
    };
    const created = await reservaService.create(payload);
    alertService.showSuccess(`Reserva #${created.id} creada correctamente`);
    await router.push({ name: 'ReservaView', params: { reservaId: created.id } });
  } catch (e: any) {
    alertService.showHttpError(e?.response);
  } finally {
    guardando.value = false;
  }
}

function toLocalInput(value?: Date | string | null) {
  if (!value) return '';
  const date = new Date(value);
  const tzOffset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - tzOffset).toISOString().slice(0, 16);
}
</script>
