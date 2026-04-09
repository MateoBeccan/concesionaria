<template>
  <div class="modal-backdrop-custom" @click.self="emit('cerrar')">
    <div class="modal-dialog-custom shadow-lg rounded-3 bg-white p-4" style="width:100%;max-width:540px">

      <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="mb-0 fw-semibold">Nuevo vehículo</h5>
        <button class="btn-close" @click="emit('cerrar')" />
      </div>

      <form @submit.prevent="guardar" novalidate>
        <div class="row g-3">

          <div class="col-6">
            <label class="form-label">Patente <span class="text-danger">*</span></label>
            <input
              v-model="form.patente"
              class="form-control text-uppercase"
              :class="{ 'is-invalid': errores.patente }"
              placeholder="ABC123 o AB123CD"
              @input="form.patente = (form.patente ?? '').toUpperCase()"
            />
            <div class="invalid-feedback">{{ errores.patente }}</div>
          </div>

          <div class="col-6">
            <label class="form-label">Estado <span class="text-danger">*</span></label>
            <select v-model="form.estado" class="form-select" :class="{ 'is-invalid': errores.estado }">
              <option value="">Seleccionar...</option>
              <option value="NUEVO">Nuevo</option>
              <option value="USADO">Usado</option>
            </select>
            <div class="invalid-feedback">{{ errores.estado }}</div>
          </div>

          <div class="col-6">
            <label class="form-label">Precio <span class="text-danger">*</span></label>
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

          <div class="col-6">
            <label class="form-label">Kilómetros</label>
            <input v-model.number="form.km" type="number" min="0" class="form-control" />
          </div>

          <div class="col-6">
            <label class="form-label">Año fabricación</label>
            <input v-model="form.fechaFabricacion" type="date" class="form-control" />
          </div>

          <div v-if="errorServidor" class="col-12">
            <div class="alert alert-danger py-2 mb-0">{{ errorServidor }}</div>
          </div>

        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" class="btn btn-outline-secondary" @click="emit('cerrar')">Cancelar</button>
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
import { reactive, ref } from 'vue';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

const props = defineProps<{ patenteInicial?: string }>();
const emit = defineEmits<{ guardado: [vehiculo: IVehiculo]; cerrar: [] }>();

const service = new VehiculoService();
const PATENTE_REGEX = /^[A-Z]{3}\d{3}$|^[A-Z]{2}\d{3}[A-Z]{2}$/;

const form = reactive({
  patente: '',
  estado: '' as 'NUEVO' | 'USADO' | '',
  condicion: 'EN_VENTA' as 'EN_VENTA' | 'RESERVADO' | 'VENDIDO',
  precio: null as number | null,
  km: 0,
  fechaFabricacion: '',
  motor: null as any,
  tipoVehiculo: null as any,
  version: null as any,
});

const errores = reactive({ patente: '', estado: '', precio: '' });
const errorServidor = ref<string | null>(null);
const guardando = ref(false);

function validar(): boolean {
  errores.patente = PATENTE_REGEX.test(form.patente) ? '' : 'Formato inválido (ej: ABC123 o AB123CD)';
  errores.estado = form.estado ? '' : 'Seleccione un estado';
  errores.precio = form.precio && form.precio > 0 ? '' : 'Ingrese un precio válido';
  return !Object.values(errores).some(e => e);
}

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

      // relaciones
      motorId: form.motor?.id,
      tipoVehiculoId: form.tipoVehiculo?.id,
      versionId: form.version?.id,
    };

    const vehiculo = await service.create(payload as unknown as IVehiculo);

    emit('guardado', vehiculo);

  } catch (e: any) {
    errorServidor.value =
      e.response?.data?.message ?? 'Error al guardar el vehículo';
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
  to   { transform: translateY(0);     opacity: 1; }
}
</style>
