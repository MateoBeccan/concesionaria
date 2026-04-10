<template>
  <div class="container py-4" style="max-width:680px">

    <div class="page-header">
      <div>
        <h1 class="page-title">{{ vehiculo.id ? 'Editar vehículo' : 'Nuevo vehículo' }}</h1>
        <p class="page-subtitle" v-if="vehiculo.id">Patente: {{ vehiculo.patente }}</p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="previousState()">← Volver</button>
    </div>

    <form @submit.prevent="save()" novalidate>

      <!-- SECCIÓN: IDENTIFICACIÓN -->
      <div class="card mb-3">
        <div class="card-header">Identificación</div>
        <div class="card-body">
          <div class="row g-3">

            <div class="col-md-6">
              <label class="form-label">Patente <span class="text-danger">*</span></label>
              <input
                type="text"
                class="form-control text-uppercase"
                v-model="v$.patente.$model"
                :class="{ 'is-invalid': v$.patente.$dirty && v$.patente.$invalid }"
                placeholder="ABC123 o AB123CD"
                @input="v$.patente.$model = (v$.patente.$model ?? '').toUpperCase()"
              />
              <div class="invalid-feedback">
                <span v-for="e of v$.patente.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Estado <span class="text-danger">*</span></label>
              <select
                class="form-select"
                v-model="v$.estado.$model"
                :class="{ 'is-invalid': v$.estado.$dirty && v$.estado.$invalid }"
              >
                <option value="">— Seleccionar —</option>
                <option v-for="e in estadoVehiculoValues" :key="e" :value="e">
                  {{ e === 'NUEVO' ? 'Nuevo' : 'Usado' }}
                </option>
              </select>
              <div class="invalid-feedback">
                <span v-for="e of v$.estado.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Año de fabricación <span class="text-danger">*</span></label>
              <input
                type="date"
                class="form-control"
                v-model="v$.fechaFabricacion.$model"
                :class="{ 'is-invalid': v$.fechaFabricacion.$dirty && v$.fechaFabricacion.$invalid }"
              />
              <div class="invalid-feedback">
                <span v-for="e of v$.fechaFabricacion.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Precio <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="v$.precio.$model"
                  :class="{ 'is-invalid': v$.precio.$dirty && v$.precio.$invalid }"
                  min="0"
                  placeholder="0"
                />
              </div>
              <div class="invalid-feedback d-block" v-if="v$.precio.$dirty && v$.precio.$invalid">
                <span v-for="e of v$.precio.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Kilómetros <span class="text-danger">*</span></label>
              <div class="input-group">
                <input
                  type="number"
                  class="form-control"
                  v-model.number="v$.km.$model"
                  :class="{ 'is-invalid': v$.km.$dirty && v$.km.$invalid }"
                  min="0"
                  placeholder="0"
                />
                <span class="input-group-text">km</span>
              </div>
              <div class="invalid-feedback d-block" v-if="v$.km.$dirty && v$.km.$invalid">
                <span v-for="e of v$.km.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

          </div>
        </div>
      </div>

      <!-- SECCIÓN: ESPECIFICACIONES -->
      <div class="card mb-3">
  <div class="card-header">Especificaciones técnicas</div>
  <div class="card-body">
    <div class="row g-3">

      <!-- MARCA -->
      <div class="col-md-6">
        <label class="form-label">Marca</label>
        <select class="form-select" v-model="selectedMarca" @change="onMarcaChange">
          <option :value="null">— Seleccionar —</option>
          <option v-for="m in marcas" :key="m.id" :value="m">
            {{ m.nombre }}
          </option>
        </select>
      </div>

      <!-- MODELO -->
      <div class="col-md-6">
        <label class="form-label">Modelo</label>
        <select class="form-select" v-model="selectedModelo" @change="onModeloChange">
          <option :value="null">— Seleccionar —</option>
          <option v-for="m in modelosFiltrados" :key="m.id" :value="m">
            {{ m.nombre }}
          </option>
        </select>
      </div>

      <!-- VERSION -->
      <div class="col-md-6">
        <label class="form-label">Versión</label>
        <select class="form-select" v-model="vehiculo.version">
          <option :value="null">— Seleccionar —</option>
          <option v-for="v in versionesFiltradas" :key="v.id" :value="v">
            {{ v.nombre }}
          </option>
        </select>
      </div>

      <!-- MOTOR -->
      <div class="col-md-6">
        <label class="form-label">Motor</label>
        <select class="form-select" v-model="vehiculo.motor">
          <option :value="null">— Seleccionar —</option>
          <option v-for="m in motors" :key="m.id" :value="m">
            {{ m.nombre }} — {{ m.potenciaHp }} HP
          </option>
        </select>
      </div>

      <!-- TIPO VEHICULO -->
      <div class="col-md-6">
        <label class="form-label">Tipo de vehículo</label>
        <select class="form-select" v-model="vehiculo.tipoVehiculo">
          <option :value="null">— Seleccionar —</option>
          <option v-for="t in tipoVehiculos" :key="t.id" :value="t">
            {{ t.nombre }}
          </option>
        </select>
      </div>

    </div>
  </div>
</div>

      <!-- ACCIONES -->
      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="previousState()">
          Cancelar
        </button>
        <button type="submit" class="btn btn-primary" :disabled="v$.$invalid || isSaving">
          <span v-if="isSaving" class="spinner-border spinner-border-sm me-1" />
          {{ vehiculo.id ? 'Guardar cambios' : 'Crear vehículo' }}
        </button>
      </div>

    </form>
  </div>
</template>

<script lang="ts" src="./vehiculo-update.component.ts"></script>
