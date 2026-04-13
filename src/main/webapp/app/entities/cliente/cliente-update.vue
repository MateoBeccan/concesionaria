<template>
  <div class="d-flex justify-content-center">
    <div class="col-10 col-lg-8">
      <form @submit.prevent="save()">
        <div class="card shadow-sm p-4">
          <h3 class="mb-4">
            {{ cliente.id ? 'Editar Cliente' : 'Nuevo Cliente' }}
          </h3>

          <h5 class="border-bottom pb-2 mb-3">Datos personales</h5>

          <div class="row mb-3">
            <div class="col-md-6">
              <label>Nombre *</label>
              <input
                class="form-control"
                v-model.trim="v$.nombre.$model"
                :class="{ 'is-invalid': v$.nombre.$dirty && v$.nombre.$invalid }"
              />
              <div v-if="v$.nombre.$dirty && v$.nombre.$invalid" class="invalid-feedback">
                <span v-for="error of v$.nombre.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label>Apellido *</label>
              <input
                class="form-control"
                v-model.trim="v$.apellido.$model"
                :class="{ 'is-invalid': v$.apellido.$dirty && v$.apellido.$invalid }"
              />
              <div v-if="v$.apellido.$dirty && v$.apellido.$invalid" class="invalid-feedback">
                <span v-for="error of v$.apellido.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label>Documento *</label>
              <input
                class="form-control"
                inputmode="numeric"
                v-model.trim="v$.nroDocumento.$model"
                :class="{ 'is-invalid': v$.nroDocumento.$dirty && v$.nroDocumento.$invalid }"
              />
              <div v-if="v$.nroDocumento.$dirty && v$.nroDocumento.$invalid" class="invalid-feedback">
                <span v-for="error of v$.nroDocumento.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label>Condición IVA</label>
              <select class="form-control" v-model="v$.condicionIva.$model">
                <option :value="null">Seleccione</option>
                <option v-for="opt in condicionIvas" :key="opt.id" :value="opt">
                  {{ opt.descripcion }}
                </option>
              </select>
            </div>
          </div>

          <h5 class="border-bottom pb-2 mb-3">Contacto</h5>

          <div class="row mb-3">
            <div class="col-md-6">
              <label>Teléfono</label>
              <input
                class="form-control"
                v-model.trim="v$.telefono.$model"
                :class="{ 'is-invalid': v$.telefono.$dirty && v$.telefono.$invalid }"
              />
              <div v-if="v$.telefono.$dirty && v$.telefono.$invalid" class="invalid-feedback">
                <span v-for="error of v$.telefono.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label>Email *</label>
              <input
                class="form-control"
                type="email"
                v-model.trim="v$.email.$model"
                :class="{ 'is-invalid': v$.email.$dirty && v$.email.$invalid }"
              />
              <div v-if="v$.email.$dirty && v$.email.$invalid" class="invalid-feedback">
                <span v-for="error of v$.email.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>
          </div>

          <div class="mb-3">
            <label>Dirección</label>
            <input class="form-control" v-model.trim="v$.direccion.$model" />
          </div>

          <div class="row mb-3">
            <div class="col-md-4">
              <input class="form-control" placeholder="Ciudad" v-model.trim="v$.ciudad.$model" />
            </div>
            <div class="col-md-4">
              <input class="form-control" placeholder="Provincia" v-model.trim="v$.provincia.$model" />
            </div>
            <div class="col-md-4">
              <input class="form-control" placeholder="País" v-model.trim="v$.pais.$model" />
            </div>
          </div>

          <div class="form-check form-switch mb-3">
            <input type="checkbox" v-model="v$.activo.$model" class="form-check-input" />
            <label>Activo</label>
          </div>

          <div class="row mb-3">
            <div class="col-md-4">
              <label>Fecha Alta</label>
              <input
                type="datetime-local"
                class="form-control"
                :class="{ 'is-invalid': v$.fechaAlta.$dirty && v$.fechaAlta.$invalid }"
                :value="convertDateTimeFromServer(v$.fechaAlta.$model)"
                @change="updateInstantField('fechaAlta', $event)"
              />
              <div v-if="v$.fechaAlta.$dirty && v$.fechaAlta.$invalid" class="invalid-feedback">
                <span v-for="error of v$.fechaAlta.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-4">
              <label>Creado</label>
              <input
                type="datetime-local"
                class="form-control"
                :value="convertDateTimeFromServer(v$.createdDate.$model)"
                @change="updateInstantField('createdDate', $event)"
              />
            </div>

            <div class="col-md-4">
              <label>Modificado</label>
              <input
                type="datetime-local"
                class="form-control"
                :value="convertDateTimeFromServer(v$.lastModifiedDate.$model)"
                @change="updateInstantField('lastModifiedDate', $event)"
              />
            </div>
          </div>

          <div class="mb-3">
            <label>Tipo Documento *</label>
            <select
              class="form-control"
              v-model="v$.tipoDocumento.$model"
              :class="{ 'is-invalid': v$.tipoDocumento.$dirty && v$.tipoDocumento.$invalid }"
            >
              <option :value="null">Seleccione</option>
              <option v-for="opt in tipoDocumentos" :key="opt.id" :value="opt">
                {{ opt.descripcion }}
              </option>
            </select>
            <div v-if="v$.tipoDocumento.$dirty && v$.tipoDocumento.$invalid" class="invalid-feedback">
              <span v-for="error of v$.tipoDocumento.$errors" :key="error.$uid">{{ error.$message }}</span>
            </div>
          </div>

          <div class="d-flex justify-content-end gap-2 mt-4">
            <button type="button" class="btn btn-outline-secondary" @click="previousState()">Cancelar</button>

            <button type="submit" class="btn btn-primary" :disabled="v$.$invalid || isSaving">Guardar</button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./cliente-update.component.ts"></script>
