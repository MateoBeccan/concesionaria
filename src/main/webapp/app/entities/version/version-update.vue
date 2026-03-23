<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.version.home.createOrEditLabel" data-cy="VersionCreateUpdateHeading">Crear o editar Version</h2>
        <div>
          <div class="mb-3" v-if="version.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="version.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="version">Nombre</label>
            <input
              type="text"
              class="form-control"
              name="nombre"
              id="version-nombre"
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
            <label class="form-control-label" for="version">Descripcion</label>
            <input
              type="text"
              class="form-control"
              name="descripcion"
              id="version-descripcion"
              data-cy="descripcion"
              :class="{ valid: !v$.descripcion.$invalid, invalid: v$.descripcion.$invalid }"
              v-model="v$.descripcion.$model"
            />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="version">Anio Inicio</label>
            <input
              type="number"
              class="form-control"
              name="anioInicio"
              id="version-anioInicio"
              data-cy="anioInicio"
              :class="{ valid: !v$.anioInicio.$invalid, invalid: v$.anioInicio.$invalid }"
              v-model.number="v$.anioInicio.$model"
            />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="version">Anio Fin</label>
            <input
              type="number"
              class="form-control"
              name="anioFin"
              id="version-anioFin"
              data-cy="anioFin"
              :class="{ valid: !v$.anioFin.$invalid, invalid: v$.anioFin.$invalid }"
              v-model.number="v$.anioFin.$model"
            />
          </div>
          <div class="mb-3">
            <label for="version">Modelos</label>
            <select
              class="form-control"
              id="version-modeloses"
              data-cy="modelos"
              multiple
              name="modelos"
              v-if="version.modeloses !== undefined"
              v-model="version.modeloses"
            >
              <option :value="getSelected(version.modeloses, modeloOption, 'id')" v-for="modeloOption in modelos" :key="modeloOption.id">
                {{ modeloOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label for="version">Motores</label>
            <select
              class="form-control"
              id="version-motoreses"
              data-cy="motores"
              multiple
              name="motores"
              v-if="version.motoreses !== undefined"
              v-model="version.motoreses"
            >
              <option :value="getSelected(version.motoreses, motorOption, 'id')" v-for="motorOption in motors" :key="motorOption.id">
                {{ motorOption.id }}
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
<script lang="ts" src="./version-update.component.ts"></script>
