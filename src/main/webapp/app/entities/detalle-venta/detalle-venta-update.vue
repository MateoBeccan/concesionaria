<template>
  <div class="container py-4" style="max-width:640px">

    <div class="page-header">
      <div>
        <h1 class="page-title">{{ detalleVenta.id ? 'Editar detalle de venta' : 'Nuevo detalle de venta' }}</h1>
        <p class="page-subtitle">Asociar un vehículo a una venta</p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="previousState()">← Volver</button>
    </div>

    <form @submit.prevent="save()" novalidate>
      <div class="card mb-3">
        <div class="card-header">Relaciones</div>
        <div class="card-body">
          <div class="row g-3">

            <div class="col-12">
              <label class="form-label">Venta <span class="text-danger">*</span></label>
              <select class="form-select" v-model="detalleVenta.venta">
                <option :value="null">— Seleccionar venta —</option>
                <option
                  v-for="v in ventas"
                  :key="v.id"
                  :value="detalleVenta.venta?.id === v.id ? detalleVenta.venta : v"
                >
                  Venta #{{ v.id }} — {{ v.cliente?.nombre }} {{ v.cliente?.apellido }}
                  ({{ formatFecha(v.fecha) }})
                </option>
              </select>
            </div>

            <div class="col-12">
              <label class="form-label">Vehículo <span class="text-danger">*</span></label>
              <select class="form-select" v-model="detalleVenta.vehiculo">
                <option :value="null">— Seleccionar vehículo —</option>
                <option
                  v-for="v in vehiculos"
                  :key="v.id"
                  :value="detalleVenta.vehiculo?.id === v.id ? detalleVenta.vehiculo : v"
                >
                  {{ v.patente }} —
                  {{ v.version?.modelo?.marca?.nombre ?? '' }}
                  {{ v.version?.modelo?.nombre ?? '' }}
                  {{ v.version?.nombre ?? '' }}
                  ($ {{ formatPrecio(v.precio) }})
                </option>
              </select>
            </div>

          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">Importes</div>
        <div class="card-body">
          <div class="row g-3">

            <div class="col-md-6">
              <label class="form-label">Precio unitario <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="v$.precioUnitario.$model"
                  :class="{ 'is-invalid': v$.precioUnitario.$dirty && v$.precioUnitario.$invalid }"
                  min="0"
                  @input="calcularSubtotal"
                />
              </div>
              <div class="invalid-feedback d-block" v-if="v$.precioUnitario.$dirty && v$.precioUnitario.$invalid">
                <span v-for="e of v$.precioUnitario.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Cantidad</label>
              <input
                type="number"
                class="form-control"
                v-model.number="v$.cantidad.$model"
                min="1"
                max="1"
                readonly
              />
              <small class="text-muted">Según JDL: máximo 1 vehículo por línea</small>
            </div>

            <div class="col-md-6">
              <label class="form-label">Subtotal</label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="v$.subtotal.$model"
                  :class="{ 'is-invalid': v$.subtotal.$dirty && v$.subtotal.$invalid }"
                  min="0"
                  readonly
                />
              </div>
              <small class="text-muted">Calculado automáticamente</small>
            </div>

          </div>
        </div>
      </div>

      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="previousState()">Cancelar</button>
        <button type="submit" class="btn btn-primary" :disabled="v$.$invalid || isSaving">
          <span v-if="isSaving" class="spinner-border spinner-border-sm me-1" />
          {{ detalleVenta.id ? 'Guardar cambios' : 'Crear detalle' }}
        </button>
      </div>

    </form>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, inject, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useVuelidate } from '@vuelidate/core';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import VentaService from '@/entities/venta/venta.service';
import DetalleVentaService from './detalle-venta.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { DetalleVenta, type IDetalleVenta } from '@/shared/model/detalle-venta.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import type { IVenta } from '@/shared/model/venta.model';

const detalleVentaService = inject('detalleVentaService', () => new DetalleVentaService());
const alertService = inject('alertService', () => useAlertService(), true);
const ventaService = inject('ventaService', () => new VentaService());
const vehiculoService = inject('vehiculoService', () => new VehiculoService());

const route = useRoute();
const router = useRouter();
const previousState = () => router.go(-1);

const detalleVenta = ref<IDetalleVenta>(new DetalleVenta());
const ventas = ref<IVenta[]>([]);
const vehiculos = ref<IVehiculo[]>([]);
const isSaving = ref(false);

if (route.params?.detalleVentaId) {
  detalleVentaService().find(route.params.detalleVentaId as any)
    .then(res => { detalleVenta.value = res; })
    .catch(e => alertService.showHttpError(e.response));
}

ventaService().retrieve({ page: 0, size: 200 }).then(r => { ventas.value = r.data; });
vehiculoService().retrieve({ page: 0, size: 200 }).then(r => { vehiculos.value = r.data; });

// Auto-calcular subtotal cuando cambia precio
watch(() => detalleVenta.value.precioUnitario, (precio) => {
  detalleVenta.value.subtotal = Number(precio ?? 0) * (detalleVenta.value.cantidad ?? 1);
});

function calcularSubtotal() {
  detalleVenta.value.subtotal = Number(detalleVenta.value.precioUnitario ?? 0) * (detalleVenta.value.cantidad ?? 1);
}

// Cuando se selecciona vehículo, pre-llenar precio
watch(() => detalleVenta.value.vehiculo, (v) => {
  if (v?.precio && !detalleVenta.value.id) {
    detalleVenta.value.precioUnitario = Number(v.precio);
    detalleVenta.value.cantidad = 1;
    calcularSubtotal();
  }
});

const validations = useValidation();
const validationRules = {
  precioUnitario: { required: validations.required('El precio es obligatorio.'), min: validations.minValue('Debe ser mayor que 0.', 0) },
  cantidad:       { required: validations.required('La cantidad es obligatoria.'), min: validations.minValue('Mínimo 1.', 1), max: validations.maxValue('Máximo 1.', 1) },
  subtotal:       { required: validations.required('El subtotal es obligatorio.'), min: validations.minValue('Debe ser mayor que 0.', 0) },
  venta:    {},
  vehiculo: {},
};
const v$ = useVuelidate(validationRules, detalleVenta as any);
v$.value.$validate();

function save() {
  isSaving.value = true;
  const op = detalleVenta.value.id
    ? detalleVentaService().update(detalleVenta.value)
    : detalleVentaService().create(detalleVenta.value);

  op.then(() => {
    isSaving.value = false;
    previousState();
    alertService.showSuccess(detalleVenta.value.id ? 'Detalle actualizado' : 'Detalle creado correctamente');
  }).catch(e => {
    isSaving.value = false;
    alertService.showHttpError(e.response);
  });
}

function formatFecha(f?: Date | string) {
  return f ? new Date(f).toLocaleDateString('es-AR') : '—';
}
function formatPrecio(p?: number | null) {
  return Number(p ?? 0).toLocaleString('es-AR');
}
</script>
