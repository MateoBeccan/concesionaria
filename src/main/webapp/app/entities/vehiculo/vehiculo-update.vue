<template>
  <div class="container py-4" style="max-width: 860px">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ vehiculo.id ? 'Editar vehiculo' : 'Nuevo vehiculo' }}</h1>
        <p class="page-subtitle">
          {{ vehiculo.id ? `Actualiza la unidad ${vehiculo.patente || 'sin patente asignada'}` : 'Carga una unidad lista para operar en catalogo e inventario.' }}
        </p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="previousState()">Volver</button>
    </div>

    <form @submit.prevent="save()" novalidate>
      <div class="card mb-3">
        <div class="card-header">1. Identificacion comercial</div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Patente <span v-if="patenteRequerida" class="text-danger">*</span></label>
              <input
                v-model="v$.patente.$model"
                type="text"
                class="form-control text-uppercase"
                :class="{ 'is-invalid': (v$.patente.$dirty && v$.patente.$invalid) || !!patenteDuplicada }"
                placeholder="ABC123 o AB123CD"
                @input="v$.patente.$model = (v$.patente.$model ?? '').toUpperCase()"
                @blur="validarPatenteUnica"
              />
              <div class="invalid-feedback">
                <span v-if="patenteDuplicada">{{ patenteDuplicada }}</span>
                <span v-else v-for="error of v$.patente.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
              <small class="text-muted d-block">{{ patenteHint }}</small>
              <small v-if="chequeandoPatente" class="text-muted">Validando patente...</small>
            </div>

            <div class="col-md-3">
              <label class="form-label">Estado fisico <span class="text-danger">*</span></label>
              <select class="form-select" v-model="v$.estado.$model" :class="{ 'is-invalid': v$.estado.$dirty && v$.estado.$invalid }">
                <option :value="null">Seleccionar</option>
                <option v-for="estado in estadoVehiculoValues" :key="estado" :value="estado">
                  {{ estado === 'NUEVO' ? 'Nuevo' : 'Usado' }}
                </option>
              </select>
              <div class="invalid-feedback">
                <span v-for="error of v$.estado.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-3">
              <label class="form-label">Condicion comercial</label>
              <div class="form-control d-flex align-items-center justify-content-between bg-light">
                <span>{{ condicionComercialLabel }}</span>
                <span class="badge" :class="condicionComercialBadge">{{ vehiculo.condicion }}</span>
              </div>
              <small class="text-muted">Se gestiona desde ventas e inventario para evitar inconsistencias.</small>
            </div>

            <div class="col-md-6">
              <label class="form-label">Fecha de fabricacion <span class="text-danger">*</span></label>
              <input
                v-model="v$.fechaFabricacion.$model"
                type="date"
                class="form-control"
                :class="{ 'is-invalid': v$.fechaFabricacion.$dirty && v$.fechaFabricacion.$invalid }"
              />
              <div class="invalid-feedback">
                <span v-for="error of v$.fechaFabricacion.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-3">
              <label class="form-label">Kilometros <span class="text-danger">*</span></label>
              <div class="input-group">
                <input
                  v-model.number="v$.km.$model"
                  type="number"
                  min="0"
                  class="form-control"
                  :class="{ 'is-invalid': v$.km.$dirty && v$.km.$invalid }"
                />
                <span class="input-group-text">km</span>
              </div>
              <div class="invalid-feedback d-block" v-if="v$.km.$dirty && v$.km.$invalid">
                <span v-for="error of v$.km.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-3">
              <label class="form-label">Precio publicado <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input
                  v-model.number="v$.precio.$model"
                  type="number"
                  min="0"
                  step="0.01"
                  class="form-control"
                  :class="{ 'is-invalid': v$.precio.$dirty && v$.precio.$invalid }"
                />
              </div>
              <div class="invalid-feedback d-block" v-if="v$.precio.$dirty && v$.precio.$invalid">
                <span v-for="error of v$.precio.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">2. Catalogo del vehiculo</div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Marca <span class="text-danger">*</span></label>
              <select class="form-select" v-model="selectedMarca" @change="handleMarcaChange">
                <option :value="null">Seleccionar</option>
                <option v-for="marca in marcas" :key="marca.id" :value="marca">
                  {{ marca.nombre }}
                </option>
              </select>
              <small class="text-muted">Primero elegi la marca para acotar el catalogo.</small>
            </div>

            <div class="col-md-6">
              <label class="form-label">Modelo <span class="text-danger">*</span></label>
              <select class="form-select" v-model="selectedModelo" @change="handleModeloChange" :disabled="!selectedMarca">
                <option :value="null">Seleccionar</option>
                <option v-for="modelo in modelosFiltrados" :key="modelo.id" :value="modelo">
                  {{ modelo.nombre }}
                </option>
              </select>
              <small class="text-muted">Se habilita al seleccionar una marca.</small>
            </div>

            <div class="col-md-6">
              <label class="form-label">Version <span class="text-danger">*</span></label>
              <select
                class="form-select"
                v-model="v$.version.$model"
                :class="{ 'is-invalid': v$.version.$dirty && v$.version.$invalid }"
                :disabled="!selectedModelo"
                @change="handleVersionChange"
              >
                <option :value="null">Seleccionar</option>
                <option v-for="version in versionesFiltradas" :key="version.id" :value="version">
                  {{ version.nombre }}
                </option>
              </select>
              <div class="invalid-feedback">
                <span v-for="error of v$.version.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Motor <span class="text-danger">*</span></label>
              <select
                class="form-select"
                v-model="v$.motor.$model"
                :class="{ 'is-invalid': (v$.motor.$dirty && v$.motor.$invalid) || !!motorValidationMessage }"
                :disabled="!vehiculo.version || loadingMotores"
              >
                <option :value="null">
                  {{
                    !vehiculo.version
                      ? 'Seleccioná una versión primero'
                      : loadingMotores
                        ? 'Cargando motores compatibles...'
                        : motoresCompatibles.length === 0
                          ? 'No hay motores configurados'
                          : 'Seleccionar motor compatible'
                  }}
                </option>
                <option v-for="motor in motoresCompatibles" :key="motor.id" :value="motor">
                  {{ motor.nombre }} - {{ motor.potenciaHp ?? '-' }} HP
                </option>
              </select>
              <div class="invalid-feedback">
                <span v-if="motorValidationMessage">{{ motorValidationMessage }}</span>
                <span v-else v-for="error of v$.motor.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
              <small v-if="loadingMotores" class="text-muted">Buscando motores compatibles para la versión seleccionada...</small>
              <small v-else-if="motorHint" class="text-muted">{{ motorHint }}</small>
            </div>

            <div class="col-md-12" v-if="vehiculo.version && !loadingMotores && motoresCompatibles.length === 0">
              <div class="alert alert-warning mb-0 py-2">
                Esta versión no tiene motores configurados. Cargá compatibilidades en Administración antes de guardar la unidad.
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Tipo de vehiculo <span class="text-danger">*</span></label>
              <select
                class="form-select"
                v-model="v$.tipoVehiculo.$model"
                :class="{ 'is-invalid': v$.tipoVehiculo.$dirty && v$.tipoVehiculo.$invalid }"
              >
                <option :value="null">Seleccionar</option>
                <option v-for="tipo in tipoVehiculos" :key="tipo.id" :value="tipo">
                  {{ tipo.nombre }}
                </option>
              </select>
              <div class="invalid-feedback">
                <span v-for="error of v$.tipoVehiculo.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-6 d-flex align-items-end">
              <div class="rounded border bg-light p-3 w-100 small">
                <div class="fw-semibold mb-1">Resumen de catalogo</div>
                <div>{{ selectedMarca?.nombre ?? 'Sin marca' }}</div>
                <div>{{ selectedModelo?.nombre ?? 'Sin modelo' }}</div>
                <div>{{ vehiculo.version?.nombre ?? 'Sin version' }}</div>
                <div>{{ vehiculo.motor?.nombre ?? 'Sin motor' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">3. Inventario asociado</div>
        <div class="card-body">
          <div v-if="inventarioAsociado" class="d-flex flex-column gap-2">
            <div class="d-flex flex-wrap gap-2 align-items-center">
              <span class="badge bg-light text-dark border">Estado: {{ inventarioAsociado.estadoInventario }}</span>
              <span class="badge" :class="inventarioAsociado.disponible ? 'bg-success' : 'bg-secondary'">
                {{ inventarioAsociado.disponible ? 'Disponible' : 'No disponible' }}
              </span>
              <span class="text-muted small">Ubicacion: {{ inventarioAsociado.ubicacion ?? 'Sin definir' }}</span>
            </div>
            <div v-if="inventarioWarning" class="alert alert-warning mb-0 py-2">
              {{ inventarioWarning }}
            </div>
            <router-link
              :to="{ name: 'InventarioEdit', params: { inventarioId: inventarioAsociado.id } }"
              class="btn btn-sm btn-outline-secondary align-self-start"
            >
              Revisar inventario
            </router-link>
          </div>
          <div v-else class="text-muted">
            No encontramos un registro de inventario asociado a esta unidad. La condicion comercial quedara en estado seguro hasta que se
            registre.
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="previousState()">Cancelar</button>
        <button type="submit" class="btn btn-primary" :disabled="v$.$invalid || isSaving || chequeandoPatente || loadingMotores || !canSaveVehiculo">
          <span v-if="isSaving" class="spinner-border spinner-border-sm me-1" />
          {{ vehiculo.id ? 'Guardar cambios' : 'Crear vehiculo' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script lang="ts" src="./vehiculo-update.component.ts"></script>
