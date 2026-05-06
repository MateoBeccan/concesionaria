<template>
  <div class="modal-backdrop-custom" @click.self="emit('cerrar')">
    <div class="modal-dialog-custom rounded-3 bg-white p-4 shadow-lg" style="width: 100%; max-width: 640px">
      <div class="mb-3 d-flex justify-content-between align-items-center">
        <div>
          <h5 class="mb-0 fw-semibold">Registrar vehiculo</h5>
          <p class="mb-0 text-muted small">Carga rapida para continuar con la venta o reserva.</p>
        </div>
        <button class="btn-close" @click="emit('cerrar')" />
      </div>

      <form @submit.prevent="guardar" novalidate>
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Patente <span v-if="patenteRequerida" class="text-danger">*</span></label>
            <input
              v-model="form.patente"
              class="form-control text-uppercase"
              :class="{ 'is-invalid': errores.patente }"
              placeholder="ABC123 o AB123CD"
              @input="form.patente = normalizarPatente(form.patente)"
            />
            <div class="invalid-feedback">{{ errores.patente }}</div>
            <small class="text-muted d-block">{{ patenteHint }}</small>
          </div>

          <div class="col-md-3">
            <label class="form-label">Estado *</label>
            <select v-model="form.estado" class="form-select" :class="{ 'is-invalid': errores.estado }">
              <option value="">Seleccionar</option>
              <option value="NUEVO">Nuevo</option>
              <option value="USADO">Usado</option>
            </select>
            <div class="invalid-feedback">{{ errores.estado }}</div>
          </div>

          <div class="col-md-3">
            <label class="form-label">Color *</label>
            <input
              v-model="form.color"
              class="form-control"
              maxlength="50"
              :class="{ 'is-invalid': errores.color }"
              placeholder="Ej: Negro"
            />
            <div class="invalid-feedback">{{ errores.color }}</div>
          </div>

          <div class="col-md-4">
            <label class="form-label">Fecha fabricacion *</label>
            <input v-model="form.fechaFabricacion" type="date" class="form-control" :class="{ 'is-invalid': errores.fechaFabricacion }" />
            <div class="invalid-feedback">{{ errores.fechaFabricacion }}</div>
          </div>

          <div class="col-md-4">
            <label class="form-label">Kilometros *</label>
            <input v-model.number="form.km" type="number" min="0" class="form-control" :class="{ 'is-invalid': errores.km }" />
            <div class="invalid-feedback">{{ errores.km }}</div>
          </div>

          <div class="col-md-4">
            <label class="form-label">Precio *</label>
            <div class="input-group">
              <span class="input-group-text">{{ form.moneda?.simbolo ?? '$' }}</span>
              <input
                v-model.number="form.precio"
                type="number"
                min="0"
                step="0.01"
                class="form-control"
                :class="{ 'is-invalid': errores.precio }"
              />
            </div>
            <div class="invalid-feedback d-block" v-if="errores.precio">{{ errores.precio }}</div>
          </div>

          <div class="col-md-4">
            <label class="form-label">Moneda *</label>
            <select v-model="form.moneda" class="form-select" :class="{ 'is-invalid': errores.moneda }">
              <option :value="null">Seleccionar</option>
              <option v-for="moneda in monedas" :key="moneda.id" :value="moneda">
                {{ moneda.simbolo ?? '' }} {{ moneda.codigo }} - {{ moneda.descripcion }}
              </option>
            </select>
            <div class="invalid-feedback">{{ errores.moneda }}</div>
          </div>

          <div class="col-md-8">
            <label class="form-label">VIN / Chasis</label>
            <input
              v-model="form.vinChasis"
              class="form-control text-uppercase"
              maxlength="30"
              :class="{ 'is-invalid': errores.vinChasis }"
              placeholder="Opcional"
            />
            <div class="invalid-feedback">{{ errores.vinChasis }}</div>
          </div>

          <div class="col-md-6">
            <label class="form-label">Marca *</label>
            <select v-model="selectedMarca" class="form-select" @change="handleMarcaChange" :class="{ 'is-invalid': errores.marca }">
              <option :value="null">Seleccionar</option>
              <option v-for="marca in marcas" :key="marca.id" :value="marca">{{ marca.nombre }}</option>
            </select>
            <div class="invalid-feedback">{{ errores.marca }}</div>
          </div>

          <div class="col-md-6">
            <label class="form-label">Modelo *</label>
            <select
              v-model="selectedModelo"
              class="form-select"
              @change="handleModeloChange"
              :disabled="!selectedMarca"
              :class="{ 'is-invalid': errores.modelo }"
            >
              <option :value="null">Seleccionar</option>
              <option v-for="modelo in modelosFiltrados" :key="modelo.id" :value="modelo">{{ modelo.nombre }}</option>
            </select>
            <div class="invalid-feedback">{{ errores.modelo }}</div>
          </div>

          <div class="col-md-6">
            <label class="form-label">Tipo de vehiculo *</label>
            <select v-model="form.tipoVehiculo" class="form-select" :class="{ 'is-invalid': errores.tipoVehiculo }">
              <option :value="null">Seleccionar</option>
              <option v-for="tipo in tipoVehiculos" :key="tipo.id" :value="tipo">{{ tipo.nombre }}</option>
            </select>
            <div class="invalid-feedback">{{ errores.tipoVehiculo }}</div>
          </div>

          <div class="col-md-6">
            <label class="form-label">Version *</label>
            <select
              v-model="form.version"
              class="form-select"
              :disabled="!selectedModelo"
              :class="{ 'is-invalid': errores.version }"
              @change="handleVersionChange"
            >
              <option :value="null">Seleccionar</option>
              <option v-for="version in versionesFiltradas" :key="version.id" :value="version">{{ version.nombre }}</option>
            </select>
            <div class="invalid-feedback">{{ errores.version }}</div>
          </div>

          <div class="col-md-6">
            <label class="form-label">Motor *</label>
            <select
              v-model="form.motor"
              class="form-select"
              :disabled="!form.version || loadingMotores"
              :class="{ 'is-invalid': errores.motor }"
            >
              <option :value="null">
                {{
                  !form.version
                    ? 'Selecciona una version primero'
                    : loadingMotores
                      ? 'Cargando motores compatibles...'
                      : motoresCompatibles.length === 0
                        ? 'No hay motores configurados'
                        : 'Seleccionar motor compatible'
                }}
              </option>
              <option v-for="motor in motoresCompatibles" :key="motor.id" :value="motor">{{ motor.nombre }} - {{ motor.potenciaHp ?? '-' }} HP</option>
            </select>
            <div class="invalid-feedback">{{ errores.motor }}</div>
            <small v-if="loadingMotores" class="text-muted">Buscando motores compatibles para la version seleccionada...</small>
            <small v-else-if="motorHint" class="text-muted">{{ motorHint }}</small>
          </div>

          <div class="col-12" v-if="form.version && !loadingMotores && motoresCompatibles.length === 0">
            <div class="alert alert-warning mb-0 py-2">La version seleccionada no tiene motores configurados.</div>
          </div>

          <div class="col-12">
            <label class="form-label">Observaciones</label>
            <textarea
              v-model="form.observaciones"
              rows="2"
              class="form-control"
              maxlength="500"
              :class="{ 'is-invalid': errores.observaciones }"
              placeholder="Opcional"
            />
            <div class="invalid-feedback">{{ errores.observaciones }}</div>
          </div>

          <div class="col-12" v-if="errorServidor">
            <div class="alert alert-danger mb-0 py-2">{{ errorServidor }}</div>
          </div>
        </div>

        <div class="mt-4 d-flex justify-content-end gap-2">
          <button type="button" class="btn btn-outline-secondary" @click="emit('cerrar')">Cancelar</button>
          <button type="submit" class="btn btn-primary" :disabled="guardando || loadingMotores">
            <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
            Guardar vehiculo
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';

import MarcaService from '@/entities/marca/marca.service';
import MonedaService from '@/entities/moneda/moneda.service';
import ModeloService from '@/entities/modelo/modelo.service';
import TipoVehiculoService from '@/entities/tipo-vehiculo/tipo-vehiculo.service';
import VersionService from '@/entities/version/version.service';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import type { IMotor } from '@/shared/model/motor.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import type { IVersion } from '@/shared/model/version.model';

import { useVehiculoForm } from './useVehiculoForm';

const props = defineProps<{
  patenteInicial?: string;
}>();

const emit = defineEmits<{ guardado: [vehiculo: IVehiculo]; cerrar: [] }>();

const vehiculoService = new VehiculoService();
const marcaService = new MarcaService();
const modeloService = new ModeloService();
const versionService = new VersionService();
const tipoVehiculoService = new TipoVehiculoService();
const monedaService = new MonedaService();

const PATENTE_REGEX = /^(?:[A-Z]{3}\d{3}|[A-Z]{2}\d{3}[A-Z]{2})$/;

const formCatalog = useVehiculoForm({
  marcaService,
  modeloService,
  versionService,
  tipoVehiculoService,
});

const {
  marcas,
  tipoVehiculos,
  selectedMarca,
  selectedModelo,
  modelosFiltrados,
  versionesFiltradas,
  motoresCompatibles,
  loadingMotores,
  motorHint,
  cargarCatalogos,
  onMarcaChange,
  onModeloChange,
  onVersionChange,
  isMotorCompatible,
} = formCatalog;

const form = reactive({
  patente: (props.patenteInicial ?? '').toUpperCase(),
  estado: '' as 'NUEVO' | 'USADO' | '',
  precio: null as number | null,
  moneda: null as IMoneda | null,
  km: 0,
  fechaFabricacion: '',
  color: '',
  vinChasis: '',
  observaciones: '',
  motor: null as IMotor | null,
  version: null as IVersion | null,
  tipoVehiculo: null as ITipoVehiculo | null,
});

const errores = reactive({
  patente: '',
  estado: '',
  precio: '',
  moneda: '',
  km: '',
  fechaFabricacion: '',
  color: '',
  vinChasis: '',
  observaciones: '',
  marca: '',
  modelo: '',
  tipoVehiculo: '',
  version: '',
  motor: '',
});

const errorServidor = ref<string | null>(null);
const guardando = ref(false);
const monedas = ref<IMoneda[]>([]);
const patenteRequerida = computed(() => form.estado === 'USADO');
const patenteHint = computed(() =>
  patenteRequerida.value ? 'Para unidades usadas la patente es obligatoria.' : 'La patente puede cargarse mas adelante.',
);

function normalizarPatente(value?: string | null) {
  return (value ?? '').trim().toUpperCase();
}

function normalizarTexto(value?: string | null) {
  return (value ?? '').trim();
}

function normalizarVin(value?: string | null) {
  return normalizarTexto(value).toUpperCase();
}

onMounted(async () => {
  const [, monedasRes] = await Promise.all([
    cargarCatalogos(form),
    monedaService.retrieve({ 'activo.equals': true, page: 0, size: 100, sort: ['codigo,asc'] }),
  ]);
  monedas.value = monedasRes.data ?? [];
  if (!form.moneda && monedas.value.length > 0) {
    form.moneda = monedas.value[0];
  }
});

async function handleMarcaChange() {
  onMarcaChange(form, false);
}

async function handleModeloChange() {
  onModeloChange(form);
}

async function handleVersionChange() {
  await onVersionChange(form);
}

async function validarPatenteUnica() {
  if (!form.patente) {
    return true;
  }

  const existente = await vehiculoService.findByPatente(form.patente);
  return !existente?.id;
}

function validarFecha(fecha: string) {
  if (!fecha) return false;
  const parsed = new Date(fecha);
  if (Number.isNaN(parsed.getTime())) return false;
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  parsed.setHours(0, 0, 0, 0);
  return parsed <= hoy;
}

async function validar(): Promise<boolean> {
  form.patente = normalizarPatente(form.patente);
  form.color = normalizarTexto(form.color);
  form.vinChasis = normalizarVin(form.vinChasis);
  form.observaciones = normalizarTexto(form.observaciones);

  if (patenteRequerida.value && !form.patente) {
    errores.patente = 'La patente es obligatoria para vehiculos usados';
  } else if (form.patente && !PATENTE_REGEX.test(form.patente)) {
    errores.patente = 'Usa formato ABC123 o AB123CD';
  } else {
    errores.patente = '';
  }

  if (!errores.patente && form.patente) {
    const disponible = await validarPatenteUnica();
    errores.patente = disponible ? '' : `La patente ${form.patente} ya esta registrada`;
  }

  errores.estado = form.estado ? '' : 'Selecciona el estado';
  errores.precio = form.precio && form.precio > 0 ? '' : 'El precio debe ser mayor a 0';
  errores.moneda = form.moneda?.id ? '' : 'Selecciona la moneda del vehiculo';
  errores.km = Number.isInteger(form.km) && form.km >= 0 ? '' : 'Los kilometros no pueden ser negativos';
  errores.fechaFabricacion = validarFecha(form.fechaFabricacion) ? '' : 'La fecha de fabricacion es obligatoria y no puede ser futura';
  errores.color = form.color ? '' : 'El color es obligatorio';
  errores.vinChasis = form.vinChasis && form.vinChasis.length > 30 ? 'El VIN/chasis no puede superar 30 caracteres' : '';
  errores.observaciones =
    form.observaciones && form.observaciones.length > 500 ? 'Las observaciones no pueden superar 500 caracteres' : '';
  errores.marca = selectedMarca.value ? '' : 'Selecciona una marca';
  errores.modelo = selectedModelo.value ? '' : 'Selecciona un modelo';
  errores.tipoVehiculo = form.tipoVehiculo ? '' : 'Selecciona el tipo de vehiculo';
  errores.version = form.version ? '' : 'Selecciona una version';

  if (!form.version) {
    errores.motor = 'Selecciona una version primero';
  } else if (loadingMotores.value || motoresCompatibles.value.length === 0) {
    errores.motor = 'La version seleccionada no tiene motores configurados';
  } else if (!form.motor) {
    errores.motor = 'Selecciona un motor compatible';
  } else if (!isMotorCompatible(form.motor.id)) {
    errores.motor = 'El motor seleccionado no corresponde a la version elegida';
  } else {
    errores.motor = '';
  }

  return !Object.values(errores).some(Boolean);
}

async function guardar() {
  if (!(await validar())) return;

  guardando.value = true;
  errorServidor.value = null;

  try {
    const payload = {
      patente: form.patente || undefined,
      estado: form.estado,
      precio: form.precio,
      moneda: form.moneda ? { id: form.moneda.id } : null,
      km: form.km,
      fechaFabricacion: form.fechaFabricacion,
      color: form.color,
      vinChasis: form.vinChasis || undefined,
      observaciones: form.observaciones || undefined,
      motor: form.motor ? { id: form.motor.id } : null,
      version: form.version ? { id: form.version.id } : null,
      tipoVehiculo: form.tipoVehiculo ? { id: form.tipoVehiculo.id } : null,
    };

    const vehiculo = await vehiculoService.create(payload as IVehiculo);
    emit('guardado', vehiculo);
  } catch (e: any) {
    const detalle = String(e?.response?.data?.detail ?? e?.response?.data?.message ?? '');
    if (detalle.includes('Field \'disponible\' doesn\'t have a default value') || detalle.includes('constraint [disponible]')) {
      errorServidor.value =
        'No se pudo crear el inventario del vehiculo por una inconsistencia de esquema (campo legacy "disponible"). Reinicia backend para aplicar migraciones y vuelve a intentar.';
    } else {
      errorServidor.value = e.response?.data?.message ?? 'Error al guardar vehiculo';
    }
  } finally {
    guardando.value = false;
  }
}
</script>

<style scoped>
.modal-backdrop-custom {
  position: fixed;
  inset: 0;
  z-index: 1050;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
}

.modal-dialog-custom {
  animation: slideIn 0.2s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-16px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
