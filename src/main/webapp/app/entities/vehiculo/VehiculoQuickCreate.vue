<template>
  <div class="modal-backdrop-custom" @click.self="emit('cerrar')">
    <div class="modal-dialog-custom shadow-lg rounded-3 bg-white p-4" style="width:100%;max-width:540px">

      <!-- HEADER -->
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="mb-0 fw-semibold">Nuevo vehículo</h5>
        <button class="btn-close" @click="emit('cerrar')" />
      </div>

      <!-- FORM -->
      <form @submit.prevent="guardar" novalidate>

        <div class="row g-3">

          <!-- PATENTE -->
          <div class="col-6">
            <label class="form-label">Patente *</label>
            <input
              v-model="form.patente"
              class="form-control text-uppercase"
              :class="{ 'is-invalid': errores.patente }"
              placeholder="ABC123 o AB123CD"
              @input="form.patente = (form.patente ?? '').toUpperCase()"
            />
            <div class="invalid-feedback">{{ errores.patente }}</div>
          </div>

          <!-- ESTADO -->
          <div class="col-6">
            <label class="form-label">Estado *</label>
            <select v-model="form.estado" class="form-select" :class="{ 'is-invalid': errores.estado }">
              <option value="">Seleccionar...</option>
              <option value="NUEVO">Nuevo</option>
              <option value="USADO">Usado</option>
            </select>
            <div class="invalid-feedback">{{ errores.estado }}</div>
          </div>

          <!-- FECHA -->
          <div class="col-6">
            <label class="form-label">Año fabricación *</label>
            <input v-model="form.fechaFabricacion" type="date" class="form-control" />
          </div>

          <!-- PRECIO -->
          <div class="col-6">
            <label class="form-label">Precio *</label>
            <div class="input-group">
              <span class="input-group-text">$</span>
              <input
                v-model.number="form.precio"
                type="number"
                min="0"
                class="form-control"
                :class="{ 'is-invalid': errores.precio }"
              />
            </div>
            <div class="invalid-feedback d-block" v-if="errores.precio">{{ errores.precio }}</div>
          </div>

          <!-- KM -->
          <div class="col-6">
            <label class="form-label">Kilómetros *</label>
            <input v-model.number="form.km" type="number" min="0" class="form-control" />
          </div>

          <!-- MARCA -->
          <div class="col-6">
            <label class="form-label">Marca</label>
            <select v-model="selectedMarca" class="form-select" @change="onMarcaChange">
              <option :value="null">— Seleccionar —</option>
              <option v-for="m in marcas" :key="m.id" :value="m">
                {{ m.nombre }}
              </option>
            </select>
          </div>

          <!-- MODELO -->
          <div class="col-6">
            <label class="form-label">Modelo</label>
            <select v-model="selectedModelo" class="form-select" @change="onModeloChange">
              <option :value="null">— Seleccionar —</option>
              <option v-for="m in modelosFiltrados" :key="m.id" :value="m">
                {{ m.nombre }}
              </option>
            </select>
          </div>

          <!-- VERSION -->
          <div class="col-6">
            <label class="form-label">Versión</label>
            <select v-model="form.version" class="form-select">
              <option :value="null">— Seleccionar —</option>
              <option v-for="v in versionesFiltradas" :key="v.id" :value="v">
                {{ v.nombre }}
              </option>
            </select>
          </div>

          <!-- MOTOR -->
          <div class="col-6">
            <label class="form-label">Motor</label>
            <select v-model="form.motor" class="form-select">
              <option :value="null">— Seleccionar —</option>
              <option v-for="m in motors" :key="m.id" :value="m">
                {{ m.nombre }}
              </option>
            </select>
          </div>

          <!-- ERROR -->
          <div v-if="errorServidor" class="col-12">
            <div class="alert alert-danger py-2 mb-0">{{ errorServidor }}</div>
          </div>

        </div>

        <!-- BOTONES -->
        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" class="btn btn-outline-secondary" @click="emit('cerrar')">
            Cancelar
          </button>

          <button type="submit" class="btn btn-primary" :disabled="guardando">
            <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
            Guardar vehículo
          </button>
        </div>

      </form>

    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, computed, onMounted } from 'vue';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import MarcaService from '@/entities/marca/marca.service';
import ModeloService from '@/entities/modelo/modelo.service';
import VersionService from '@/entities/version/version.service';
import MotorService from '@/entities/motor/motor.service';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

const emit = defineEmits<{ guardado: [vehiculo: IVehiculo]; cerrar: [] }>();

const vehiculoService = new VehiculoService();
const marcaService = new MarcaService();
const modeloService = new ModeloService();
const versionService = new VersionService();
const motorService = new MotorService();

const marcas = ref<any[]>([]);
const modelos = ref<any[]>([]);
const versiones = ref<any[]>([]);
const motors = ref<any[]>([]);

const selectedMarca = ref<any>(null);
const selectedModelo = ref<any>(null);

const form = reactive({
  patente: '',
  estado: '' as 'NUEVO' | 'USADO' | '',
  condicion: 'EN_VENTA',
  precio: null as number | null,
  km: 0,
  fechaFabricacion: '',
  motor: null as any,
  version: null as any,
});

const errores = reactive({
  patente: '',
  estado: '',
  precio: '',
});

const errorServidor = ref<string | null>(null);
const guardando = ref(false);

const PATENTE_REGEX = /^[A-Z]{3}\d{3}$|^[A-Z]{2}\d{3}[A-Z]{2}$/;

// =====================
// COMPUTED
// =====================

const modelosFiltrados = computed(() =>
  modelos.value.filter((m: any) => m.marca?.id === selectedMarca.value?.id)
);

const versionesFiltradas = computed(() =>
  versiones.value.filter((v: any) => v.modelo?.id === selectedModelo.value?.id)
);

// =====================
// LOAD DATA
// =====================

async function cargarDatos() {
  marcas.value = (await marcaService.retrieve({ size: 100 })).data;
  modelos.value = (await modeloService.retrieve({ size: 100 })).data;
  versiones.value = (await versionService.retrieve({ size: 100 })).data;
  motors.value = (await motorService.retrieve({ size: 100 })).data;
}

onMounted(cargarDatos);

// =====================
// EVENTS
// =====================

function onMarcaChange() {
  selectedModelo.value = null;
  form.version = null;
  form.motor = null;
}

function onModeloChange() {
  form.version = null;
  form.motor = null;
}

// =====================
// VALIDACION
// =====================

function validar(): boolean {
  errores.patente = PATENTE_REGEX.test(form.patente)
    ? ''
    : 'Formato inválido (ABC123 o AB123CD)';

  errores.estado = form.estado ? '' : 'Seleccione estado';

  errores.precio =
    form.precio && form.precio > 0 ? '' : 'Precio inválido';

  return !Object.values(errores).some(e => e);
}

// =====================
// SAVE
// =====================

async function guardar() {
  if (!validar()) return;

  guardando.value = true;
  errorServidor.value = null;

  try {
    const payload = {
      patente: form.patente,
      estado: form.estado,
      condicion: form.condicion,
      precio: form.precio,
      km: form.km,
      fechaFabricacion: form.fechaFabricacion
        ? new Date(form.fechaFabricacion)
        : null,
      motor: form.motor ? { id: form.motor.id } : null,
      version: form.version ? { id: form.version.id } : null,
    };

    const vehiculo = await vehiculoService.create(payload as IVehiculo);

    emit('guardado', vehiculo);

  } catch (e: any) {
    errorServidor.value =
      e.response?.data?.message ?? 'Error al guardar vehículo';
  } finally {
    guardando.value = false;
  }
}
</script>

<style scoped>
.modal-backdrop-custom {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}
.modal-dialog-custom {
  animation: slideIn .2s ease;
}
@keyframes slideIn {
  from { transform: translateY(-16px); opacity: 0; }
  to   { transform: translateY(0); opacity: 1; }
}
</style>
