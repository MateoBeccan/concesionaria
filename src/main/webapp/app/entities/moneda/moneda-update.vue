<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.moneda.home.createOrEditLabel" data-cy="MonedaCreateUpdateHeading">Crear o editar Moneda</h2>
        <div>
          <div class="mb-3" v-if="moneda.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="moneda.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="moneda">Codigo</label>
            <input
              type="text"
              class="form-control"
              name="codigo"
              id="moneda-codigo"
              data-cy="codigo"
              :class="{ valid: !v$.codigo.$invalid, invalid: v$.codigo.$invalid }"
              v-model="v$.codigo.$model"
              required
            />
            <div v-if="v$.codigo.$anyDirty && v$.codigo.$invalid">
              <small class="form-text text-danger" v-for="error of v$.codigo.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="moneda">Descripcion</label>
            <input
              type="text"
              class="form-control"
              name="descripcion"
              id="moneda-descripcion"
              data-cy="descripcion"
              :class="{ valid: !v$.descripcion.$invalid, invalid: v$.descripcion.$invalid }"
              v-model="v$.descripcion.$model"
            />
            <div v-if="v$.descripcion.$anyDirty && v$.descripcion.$invalid">
              <small class="form-text text-danger" v-for="error of v$.descripcion.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="moneda">Simbolo</label>
            <input
              type="text"
              class="form-control"
              name="simbolo"
              id="moneda-simbolo"
              data-cy="simbolo"
              :class="{ valid: !v$.simbolo.$invalid, invalid: v$.simbolo.$invalid }"
              v-model="v$.simbolo.$model"
            />
            <div v-if="v$.simbolo.$anyDirty && v$.simbolo.$invalid">
              <small class="form-text text-danger" v-for="error of v$.simbolo.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="moneda">Activo</label>
            <input
              type="checkbox"
              class="form-check"
              name="activo"
              id="moneda-activo"
              data-cy="activo"
              :class="{ valid: !v$.activo.$invalid, invalid: v$.activo.$invalid }"
              v-model="v$.activo.$model"
              required
            />
            <div v-if="v$.activo.$anyDirty && v$.activo.$invalid">
              <small class="form-text text-danger" v-for="error of v$.activo.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
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
<script lang="ts" src="./moneda-update.component.ts"></script>
