<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.metodoPago.home.createOrEditLabel" data-cy="MetodoPagoCreateUpdateHeading">Crear o editar Metodo Pago</h2>
        <div>
          <div class="mb-3" v-if="metodoPago.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="metodoPago.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="metodo-pago">Codigo</label>
            <input
              type="text"
              class="form-control"
              name="codigo"
              id="metodo-pago-codigo"
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
            <label class="form-control-label" for="metodo-pago">Descripcion</label>
            <input
              type="text"
              class="form-control"
              name="descripcion"
              id="metodo-pago-descripcion"
              data-cy="descripcion"
              :class="{ valid: !v$.descripcion.$invalid, invalid: v$.descripcion.$invalid }"
              v-model="v$.descripcion.$model"
            />
            <div v-if="v$.descripcion.$anyDirty && v$.descripcion.$invalid">
              <small class="form-text text-danger" v-for="error of v$.descripcion.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="metodo-pago">Activo</label>
            <input
              type="checkbox"
              class="form-check"
              name="activo"
              id="metodo-pago-activo"
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
            <label class="form-control-label" for="metodo-pago">Requiere Referencia</label>
            <input
              type="checkbox"
              class="form-check"
              name="requiereReferencia"
              id="metodo-pago-requiereReferencia"
              data-cy="requiereReferencia"
              :class="{ valid: !v$.requiereReferencia.$invalid, invalid: v$.requiereReferencia.$invalid }"
              v-model="v$.requiereReferencia.$model"
              required
            />
            <div v-if="v$.requiereReferencia.$anyDirty && v$.requiereReferencia.$invalid">
              <small class="form-text text-danger" v-for="error of v$.requiereReferencia.$errors" :key="error.$uid">{{
                error.$message
              }}</small>
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
<script lang="ts" src="./metodo-pago-update.component.ts"></script>
