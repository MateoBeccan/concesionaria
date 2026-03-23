<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.cliente.home.createOrEditLabel" data-cy="ClienteCreateUpdateHeading">Crear o editar Cliente</h2>
        <div>
          <div class="mb-3" v-if="cliente.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="cliente.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Nombre</label>
            <input
              type="text"
              class="form-control"
              name="nombre"
              id="cliente-nombre"
              data-cy="nombre"
              :class="{ valid: !v$.nombre.$invalid, invalid: v$.nombre.$invalid }"
              v-model="v$.nombre.$model"
              required
            />
            <div v-if="v$.nombre.$anyDirty && v$.nombre.$invalid">
              <small class="form-text text-danger" v-for="error of v$.nombre.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Apellido</label>
            <input
              type="text"
              class="form-control"
              name="apellido"
              id="cliente-apellido"
              data-cy="apellido"
              :class="{ valid: !v$.apellido.$invalid, invalid: v$.apellido.$invalid }"
              v-model="v$.apellido.$model"
              required
            />
            <div v-if="v$.apellido.$anyDirty && v$.apellido.$invalid">
              <small class="form-text text-danger" v-for="error of v$.apellido.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Nro Documento</label>
            <input
              type="text"
              class="form-control"
              name="nroDocumento"
              id="cliente-nroDocumento"
              data-cy="nroDocumento"
              :class="{ valid: !v$.nroDocumento.$invalid, invalid: v$.nroDocumento.$invalid }"
              v-model="v$.nroDocumento.$model"
              required
            />
            <div v-if="v$.nroDocumento.$anyDirty && v$.nroDocumento.$invalid">
              <small class="form-text text-danger" v-for="error of v$.nroDocumento.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Telefono</label>
            <input
              type="text"
              class="form-control"
              name="telefono"
              id="cliente-telefono"
              data-cy="telefono"
              :class="{ valid: !v$.telefono.$invalid, invalid: v$.telefono.$invalid }"
              v-model="v$.telefono.$model"
            />
            <div v-if="v$.telefono.$anyDirty && v$.telefono.$invalid">
              <small class="form-text text-danger" v-for="error of v$.telefono.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Email</label>
            <input
              type="text"
              class="form-control"
              name="email"
              id="cliente-email"
              data-cy="email"
              :class="{ valid: !v$.email.$invalid, invalid: v$.email.$invalid }"
              v-model="v$.email.$model"
            />
            <div v-if="v$.email.$anyDirty && v$.email.$invalid">
              <small class="form-text text-danger" v-for="error of v$.email.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Direccion</label>
            <input
              type="text"
              class="form-control"
              name="direccion"
              id="cliente-direccion"
              data-cy="direccion"
              :class="{ valid: !v$.direccion.$invalid, invalid: v$.direccion.$invalid }"
              v-model="v$.direccion.$model"
            />
            <div v-if="v$.direccion.$anyDirty && v$.direccion.$invalid">
              <small class="form-text text-danger" v-for="error of v$.direccion.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Ciudad</label>
            <input
              type="text"
              class="form-control"
              name="ciudad"
              id="cliente-ciudad"
              data-cy="ciudad"
              :class="{ valid: !v$.ciudad.$invalid, invalid: v$.ciudad.$invalid }"
              v-model="v$.ciudad.$model"
            />
            <div v-if="v$.ciudad.$anyDirty && v$.ciudad.$invalid">
              <small class="form-text text-danger" v-for="error of v$.ciudad.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Provincia</label>
            <input
              type="text"
              class="form-control"
              name="provincia"
              id="cliente-provincia"
              data-cy="provincia"
              :class="{ valid: !v$.provincia.$invalid, invalid: v$.provincia.$invalid }"
              v-model="v$.provincia.$model"
            />
            <div v-if="v$.provincia.$anyDirty && v$.provincia.$invalid">
              <small class="form-text text-danger" v-for="error of v$.provincia.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Pais</label>
            <input
              type="text"
              class="form-control"
              name="pais"
              id="cliente-pais"
              data-cy="pais"
              :class="{ valid: !v$.pais.$invalid, invalid: v$.pais.$invalid }"
              v-model="v$.pais.$model"
            />
            <div v-if="v$.pais.$anyDirty && v$.pais.$invalid">
              <small class="form-text text-danger" v-for="error of v$.pais.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Activo</label>
            <input
              type="checkbox"
              class="form-check"
              name="activo"
              id="cliente-activo"
              data-cy="activo"
              :class="{ valid: !v$.activo.$invalid, invalid: v$.activo.$invalid }"
              v-model="v$.activo.$model"
              required
            />
            <div v-if="v$.activo.$anyDirty && v$.activo.$invalid">
              <small class="form-text text-danger" v-for="error of v$.activo.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Fecha Alta</label>
            <div class="d-flex">
              <input
                id="cliente-fechaAlta"
                data-cy="fechaAlta"
                type="datetime-local"
                class="form-control"
                name="fechaAlta"
                :class="{ valid: !v$.fechaAlta.$invalid, invalid: v$.fechaAlta.$invalid }"
                required
                :value="convertDateTimeFromServer(v$.fechaAlta.$model)"
                @change="updateInstantField('fechaAlta', $event)"
              />
            </div>
            <div v-if="v$.fechaAlta.$anyDirty && v$.fechaAlta.$invalid">
              <small class="form-text text-danger" v-for="error of v$.fechaAlta.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cliente">Condicion Iva</label>
            <select
              class="form-control"
              id="cliente-condicionIva"
              data-cy="condicionIva"
              name="condicionIva"
              v-model="cliente.condicionIva"
            >
              <option :value="null"></option>
              <option
                :value="
                  cliente.condicionIva && condicionIvaOption.id === cliente.condicionIva.id ? cliente.condicionIva : condicionIvaOption
                "
                v-for="condicionIvaOption in condicionIvas"
                :key="condicionIvaOption.id"
              >
                {{ condicionIvaOption.id }}
              </option>
            </select>
          </div>
        </div>
        <div>
          <button type="button" id="cancel-save" data-cy="entityCreateCancelButton" class="btn btn-secondary" @click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span>Cancelar</span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="v$.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span>Guardar</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./cliente-update.component.ts"></script>
