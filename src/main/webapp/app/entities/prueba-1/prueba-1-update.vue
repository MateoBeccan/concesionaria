<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.prueba1.home.createOrEditLabel" data-cy="Prueba1CreateUpdateHeading">Crear o editar Prueba 1</h2>
        <div>
          <div class="mb-3" v-if="prueba1.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="prueba1.id" readonly />
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
<script lang="ts" src="./prueba-1-update.component.ts"></script>
