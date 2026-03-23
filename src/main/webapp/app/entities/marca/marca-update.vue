<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.marca.home.createOrEditLabel" data-cy="MarcaCreateUpdateHeading">Crear o editar Marca</h2>
        <div>
          <div class="mb-3" v-if="marca.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="marca.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="marca">Nombre</label>
            <input
              type="text"
              class="form-control"
              name="nombre"
              id="marca-nombre"
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
            <label class="form-control-label" for="marca">Pais Origen</label>
            <input
              type="text"
              class="form-control"
              name="paisOrigen"
              id="marca-paisOrigen"
              data-cy="paisOrigen"
              :class="{ valid: !v$.paisOrigen.$invalid, invalid: v$.paisOrigen.$invalid }"
              v-model="v$.paisOrigen.$model"
            />
            <div v-if="v$.paisOrigen.$anyDirty && v$.paisOrigen.$invalid">
              <small class="form-text text-danger" v-for="error of v$.paisOrigen.$errors" :key="error.$uid">{{ error.$message }}</small>
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
<script lang="ts" src="./marca-update.component.ts"></script>
