<template>
  <div class="d-flex justify-content-center">
    <div class="col-10 col-lg-6">
      <form @submit.prevent="save()">

        <div class="card shadow-sm p-4">

          <!-- HEADER -->
          <div class="mb-4">
            <h3 class="mb-0">
              {{ cotizacion.id ? 'Editar Cotización' : 'Nueva Cotización' }}
            </h3>
            <small class="text-muted" v-if="cotizacion.id">
              ID: {{ cotizacion.id }}
            </small>
          </div>

          <!-- FECHA -->
          <div class="mb-3">
            <label class="form-label">Fecha *</label>
            <input
              type="datetime-local"
              class="form-control"
              v-model="v$.fecha.$model"
            />
          </div>

          <!-- VALORES -->
          <h5 class="border-bottom pb-2 mb-3">Valores</h5>

          <div class="row mb-3">

            <div class="col-md-6">
              <label class="form-label">Valor Compra *</label>
              <input
                type="number"
                class="form-control"
                v-model.number="v$.valorCompra.$model"
              />
            </div>

            <div class="col-md-6">
              <label class="form-label">Valor Venta *</label>
              <input
                type="number"
                class="form-control fw-bold text-success"
                v-model.number="v$.valorVenta.$model"
              />
            </div>

          </div>

          <!-- MONEDA -->
          <div class="mb-3">
            <label class="form-label">Moneda</label>
            <select class="form-control" v-model="v$.moneda.$model">
              <option :value="null">Seleccione...</option>
              <option v-for="m in monedas" :key="m.id" :value="m">
                {{ m.codigo || m.id }}
              </option>
            </select>
          </div>

          <!-- ESTADO -->
          <div class="form-check form-switch mb-4">
            <input
              type="checkbox"
              class="form-check-input"
              v-model="v$.activo.$model"
              id="activoSwitch"
            />
            <label class="form-check-label" for="activoSwitch">
              Cotización activa
            </label>
          </div>

          <!-- BOTONES -->
          <div class="d-flex justify-content-end gap-2">

            <button
              type="button"
              class="btn btn-outline-secondary"
              @click="previousState()"
            >
              Cancelar
            </button>

            <button
              type="submit"
              class="btn btn-primary"
              :disabled="v$.$invalid || isSaving"
            >
              Guardar
            </button>

          </div>

        </div>

      </form>
    </div>
  </div>
</template>

<script lang="ts" src="./cotizacion-update.component.ts"></script>
