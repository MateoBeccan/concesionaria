<template>
  <div class="container py-4" style="max-width: 1080px">
    <div class="d-flex justify-content-between align-items-start mb-3">
      <div>
        <h4 class="mb-0">{{ form.id ? `Editar tasacion #${form.id}` : 'Nueva tasacion' }}</h4>
        <small class="text-muted">Carga profesional de usado para potencial entrega en parte de pago.</small>
      </div>
      <router-link :to="{ name: 'TasacionUsado' }" class="btn btn-outline-secondary">Volver</router-link>
    </div>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <div class="row g-3 mb-3">
          <div class="col-md-8">
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
              <input v-model="busquedaCliente" class="form-control" placeholder="Buscar cliente..." @input="buscarClientes" />
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
          <div class="col-md-4">
            <label class="form-label">Estado <span class="text-danger">*</span></label>
            <select v-model="form.estado" class="form-select">
              <option :value="EstadoTasacionUsado.PENDIENTE">PENDIENTE</option>
              <option :value="EstadoTasacionUsado.ACEPTADA">ACEPTADA</option>
              <option :value="EstadoTasacionUsado.RECHAZADA">RECHAZADA</option>
            </select>
          </div>
        </div>

        <div class="card mb-3">
          <div class="card-header fw-semibold">Datos tecnicos del vehiculo usado</div>
          <div class="card-body row g-3">
            <div class="col-md-4">
              <label class="form-label">Marca</label>
              <select v-model.number="selectedMarcaId" class="form-select" @change="onMarcaChange">
                <option :value="null">Seleccionar</option>
                <option v-for="item in marcas" :key="item.id" :value="item.id">{{ item.nombre }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Modelo</label>
              <select v-model.number="selectedModeloId" class="form-select" :disabled="!selectedMarcaId" @change="onModeloChange">
                <option :value="null">Seleccionar</option>
                <option v-for="item in modelos" :key="item.id" :value="item.id">{{ item.nombre }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Version <span class="text-danger" v-if="esAceptada">*</span></label>
              <select v-model.number="selectedVersionId" class="form-select" :disabled="!selectedModeloId" @change="onVersionChange">
                <option :value="null">Seleccionar</option>
                <option v-for="item in versiones" :key="item.id" :value="item.id">{{ item.nombre }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Motor</label>
              <select v-model.number="selectedMotorId" class="form-select" :disabled="!selectedVersionId">
                <option :value="null">Seleccionar</option>
                <option v-for="item in motoresCompatibles" :key="item.id" :value="item.id">{{ item.nombre }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Tipo de vehiculo <span class="text-danger" v-if="esAceptada">*</span></label>
              <select v-model.number="selectedTipoVehiculoId" class="form-select">
                <option :value="null">Seleccionar</option>
                <option v-for="item in tiposVehiculo" :key="item.id" :value="item.id">{{ item.nombre }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Anio usado <span class="text-danger" v-if="esAceptada">*</span></label>
              <input v-model.number="form.anioUsado" type="number" min="1950" :max="anioMaximo" class="form-control" />
            </div>
            <div class="col-12">
              <div class="small text-muted rounded border bg-light p-2">
                {{ versionResumen }}
              </div>
            </div>
          </div>
        </div>

        <div class="card mb-3">
          <div class="card-header fw-semibold">Identificacion y tasacion</div>
          <div class="card-body row g-3">
            <div class="col-md-4">
              <label class="form-label">Patente</label>
              <input v-model="form.patenteUsado" class="form-control" maxlength="30" />
            </div>
            <div class="col-md-4">
              <label class="form-label">VIN/Chasis</label>
              <input v-model="form.vinChasisUsado" class="form-control" maxlength="30" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Color <span class="text-danger" v-if="esAceptada">*</span></label>
              <input v-model="form.colorUsado" class="form-control" maxlength="50" />
            </div>
            <div class="col-md-4">
              <label class="form-label">KM usado <span class="text-danger" v-if="esAceptada">*</span></label>
              <input v-model.number="form.kmUsado" type="number" min="0" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Fecha tasacion <span class="text-danger">*</span></label>
              <input v-model="fechaLocal" type="datetime-local" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Monto tasacion <span class="text-danger">*</span></label>
              <input v-model.number="form.montoTasacion" type="number" min="0.01" step="0.01" class="form-control" />
            </div>
            <div class="col-md-6">
              <label class="form-label">Usuario tasador <span class="text-danger" v-if="esAceptada">*</span></label>
              <select v-model.number="selectedTasadorUserId" class="form-select">
                <option :value="null">Seleccionar tasador</option>
                <option v-for="u in usuariosActivos" :key="u.id" :value="u.id">{{ userLabel(u) }}</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label">Marca/Modelo (legacy)</label>
              <input v-model="form.marcaModeloUsado" class="form-control" maxlength="120" readonly />
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header fw-semibold">Observaciones</div>
          <div class="card-body">
            <textarea v-model="form.observaciones" rows="3" class="form-control" maxlength="500" />
          </div>
        </div>

        <div v-if="validacion" class="alert alert-warning mt-3 mb-0">{{ validacion }}</div>
      </div>

      <div class="card-footer bg-white d-flex justify-content-end gap-2">
        <router-link :to="{ name: 'TasacionUsado' }" class="btn btn-outline-secondary">Cancelar</router-link>
        <button class="btn btn-primary" @click="guardar" :disabled="guardando || !!validacion">
          <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
          Guardar
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { useAlertService } from '@/shared/alert/alert.service';
import { EstadoTasacionUsado } from '@/shared/model/enumerations/estado-tasacion-usado.model';
import type { ICliente } from '@/shared/model/cliente.model';
import type { IMarca } from '@/shared/model/marca.model';
import type { IModelo } from '@/shared/model/modelo.model';
import type { IMotor } from '@/shared/model/motor.model';
import type { ITasacionUsado } from '@/shared/model/tasacion-usado.model';
import type { ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';
import type { IUser } from '@/shared/model/user.model';
import type { IVersion } from '@/shared/model/version.model';
import TasacionUsadoService from './tasacion-usado.service';

const route = useRoute();
const router = useRouter();
const alertService = useAlertService();
const service = new TasacionUsadoService();

const guardando = ref(false);
const error = ref<string | null>(null);
const busquedaCliente = ref('');
const resultadosCliente = ref<ICliente[]>([]);
const marcas = ref<IMarca[]>([]);
const modelos = ref<IModelo[]>([]);
const versiones = ref<IVersion[]>([]);
const motoresCompatibles = ref<IMotor[]>([]);
const tiposVehiculo = ref<ITipoVehiculo[]>([]);
const usuariosActivos = ref<IUser[]>([]);
const selectedMarcaId = ref<number | null>(null);
const selectedModeloId = ref<number | null>(null);
const selectedVersionId = ref<number | null>(null);
const selectedMotorId = ref<number | null>(null);
const selectedTipoVehiculoId = ref<number | null>(null);
const selectedTasadorUserId = ref<number | null>(null);
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

const form = reactive<ITasacionUsado>({
  id: undefined,
  cliente: null,
  fechaTasacion: new Date(),
  estado: EstadoTasacionUsado.PENDIENTE,
  montoTasacion: 0,
  marcaModeloUsado: '',
  patenteUsado: '',
  vinChasisUsado: '',
  anioUsado: null,
  kmUsado: null,
  colorUsado: '',
  observaciones: '',
  usuarioTasador: '',
  version: null,
  motor: null,
  tipoVehiculo: null,
  tasadorUser: null,
});

const anioMaximo = new Date().getFullYear() + 1;
const esAceptada = computed(() => form.estado === EstadoTasacionUsado.ACEPTADA);

const versionResumen = computed(() => {
  const version = versiones.value.find(item => item.id === selectedVersionId.value);
  if (!version) return 'Selecciona una version para visualizar su ficha tecnica.';
  const modelo = version.modelo?.nombre ?? '-';
  const marca = version.modelo?.marca?.nombre ?? '-';
  return `Version: ${version.nombre ?? '-'} | Modelo: ${modelo} | Marca: ${marca} | Inicio: ${version.anioInicio ?? '-'} | Fin: ${
    version.anioFin ?? '-'
  }`;
});

const fechaLocal = computed({
  get: () => toLocalInput(form.fechaTasacion),
  set: value => {
    form.fechaTasacion = value ? new Date(value) : undefined;
  },
});

const validacion = computed(() => {
  if (!form.cliente?.id) return 'El cliente es obligatorio.';
  if (!form.fechaTasacion) return 'La fecha de tasacion es obligatoria.';
  if (!form.estado) return 'El estado es obligatorio.';
  if (!Number.isFinite(Number(form.montoTasacion)) || Number(form.montoTasacion) <= 0) return 'El monto de tasacion debe ser mayor a 0.';

  if (esAceptada.value) {
    if (!selectedVersionId.value) return 'Para ACEPTADA debes seleccionar una version valida.';
    if (!selectedTipoVehiculoId.value) return 'Para ACEPTADA debes seleccionar tipo de vehiculo.';
    if (!selectedTasadorUserId.value) return 'Para ACEPTADA debes seleccionar usuario tasador.';
    if (!form.patenteUsado?.trim() && !form.vinChasisUsado?.trim()) return 'Para ACEPTADA debes informar patente o VIN/chasis.';
    if (!form.anioUsado || form.anioUsado < 1950 || form.anioUsado > anioMaximo) return 'Para ACEPTADA debes informar anio valido.';
    if (form.kmUsado === null || form.kmUsado === undefined || form.kmUsado < 0) return 'Para ACEPTADA debes informar kilometraje valido.';
    if (!form.colorUsado?.trim()) return 'Para ACEPTADA debes informar color.';
  }
  return '';
});

watch([selectedVersionId, selectedModeloId, selectedMarcaId], () => {
  const version = versiones.value.find(item => item.id === selectedVersionId.value);
  if (version?.modelo?.marca?.nombre && version?.modelo?.nombre) {
    form.marcaModeloUsado = `${version.modelo.marca.nombre} ${version.modelo.nombre}`.trim();
  } else {
    form.marcaModeloUsado = '';
  }
});

watch(selectedTasadorUserId, () => {
  const user = usuariosActivos.value.find(item => item.id === selectedTasadorUserId.value);
  form.usuarioTasador = user?.login ?? '';
});

onMounted(async () => {
  await Promise.all([cargarMarcas(), cargarTiposVehiculo(), cargarUsuariosActivos()]);

  const id = Number(route.params.tasacionUsadoId);
  if (Number.isFinite(id) && id > 0) {
    try {
      const data = await service.find(id);
      Object.assign(form, data);
      selectedVersionId.value = data.version?.id ?? null;
      selectedTipoVehiculoId.value = data.tipoVehiculo?.id ?? null;
      selectedMotorId.value = data.motor?.id ?? null;
      selectedTasadorUserId.value = data.tasadorUser?.id ?? null;

      if (data.version?.modelo?.marca?.id) {
        selectedMarcaId.value = data.version.modelo.marca.id;
        await onMarcaChange();
        if (data.version?.modelo?.id) {
          selectedModeloId.value = data.version.modelo.id;
          await onModeloChange();
        }
      }
      if (selectedVersionId.value) {
        await onVersionChange();
      }
    } catch (e: any) {
      error.value = 'No se pudo cargar la tasacion';
      alertService.showHttpError(e?.response);
    }
  }

  const clienteIdQuery = Number(route.query.clienteId);
  if (!form.cliente?.id && Number.isFinite(clienteIdQuery) && clienteIdQuery > 0) {
    try {
      const cliente = await axios.get<ICliente>(`api/clientes/${clienteIdQuery}`);
      form.cliente = cliente.data;
    } catch {
      // no-op
    }
  }
});

async function cargarMarcas() {
  const res = await axios.get<IMarca[]>('api/marcas', { params: { page: 0, size: 500, sort: 'nombre,asc' } });
  marcas.value = Array.isArray(res.data) ? res.data : [];
}

async function cargarTiposVehiculo() {
  const res = await axios.get<ITipoVehiculo[]>('api/tipo-vehiculos', { params: { page: 0, size: 500, sort: 'nombre,asc' } });
  tiposVehiculo.value = Array.isArray(res.data) ? res.data : [];
}

async function cargarUsuariosActivos() {
  const res = await axios.get<IUser[]>('api/users/active');
  usuariosActivos.value = Array.isArray(res.data) ? res.data : [];
}

async function onMarcaChange() {
  selectedModeloId.value = null;
  selectedVersionId.value = null;
  selectedMotorId.value = null;
  modelos.value = [];
  versiones.value = [];
  motoresCompatibles.value = [];
  if (!selectedMarcaId.value) {
    return;
  }
  const res = await axios.get<IModelo[]>('api/modelos', { params: { marcaId: selectedMarcaId.value } });
  modelos.value = Array.isArray(res.data) ? res.data : [];
}

async function onModeloChange() {
  selectedVersionId.value = null;
  selectedMotorId.value = null;
  versiones.value = [];
  motoresCompatibles.value = [];
  if (!selectedModeloId.value) {
    return;
  }
  const res = await axios.get<IVersion[]>('api/versions', { params: { modeloId: selectedModeloId.value } });
  versiones.value = Array.isArray(res.data) ? res.data : [];
}

async function onVersionChange() {
  selectedMotorId.value = null;
  motoresCompatibles.value = [];
  if (!selectedVersionId.value) {
    return;
  }
  const res = await axios.get<IMotor[]>(`api/versions/${selectedVersionId.value}/motors`);
  motoresCompatibles.value = Array.isArray(res.data) ? res.data : [];
}

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
  form.cliente = cliente;
  busquedaCliente.value = '';
  resultadosCliente.value = [];
}

function userLabel(user: IUser) {
  const nombre = [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
  if (nombre) {
    return `${nombre} (${user.login ?? user.email ?? 'sin-login'})`;
  }
  return user.login ?? user.email ?? `Usuario #${user.id}`;
}

async function guardar() {
  if (validacion.value) {
    alertService.showError(validacion.value);
    return;
  }

  guardando.value = true;
  try {
    const payload: ITasacionUsado = {
      ...form,
      cliente: form.cliente?.id ? { id: form.cliente.id } : null,
      inventarioGenerado: form.inventarioGenerado?.id ? { id: form.inventarioGenerado.id } : null,
      version: selectedVersionId.value ? { id: selectedVersionId.value } : null,
      motor: selectedMotorId.value ? { id: selectedMotorId.value } : null,
      tipoVehiculo: selectedTipoVehiculoId.value ? { id: selectedTipoVehiculoId.value } : null,
      tasadorUser: selectedTasadorUserId.value ? { id: selectedTasadorUserId.value } : null,
    };
    const saved = form.id ? await service.update(payload) : await service.create(payload);
    alertService.showSuccess(`Tasacion #${saved.id} guardada`);
    const returnTo = typeof route.query.returnTo === 'string' ? route.query.returnTo : '';
    if (returnTo) {
      await router.push(`${returnTo}${returnTo.includes('?') ? '&' : '?'}refreshTasaciones=1`);
      return;
    }
    await router.push({ name: 'TasacionUsadoView', params: { tasacionUsadoId: saved.id } });
  } catch (e: any) {
    alertService.showHttpError(e?.response);
  } finally {
    guardando.value = false;
  }
}

function toLocalInput(value?: Date | string) {
  if (!value) return '';
  const date = new Date(value);
  const tzOffset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - tzOffset).toISOString().slice(0, 16);
}
</script>
