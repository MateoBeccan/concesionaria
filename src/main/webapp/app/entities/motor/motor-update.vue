<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.motor.home.createOrEditLabel" data-cy="MotorCreateUpdateHeading">Crear o editar Motor</h2>
        <div>
          <div class="mb-3" v-if="motor.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="motor.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="motor">Nombre</label>
            <input
              type="text"
              class="form-control"
              name="nombre"
              id="motor-nombre"
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
            <label class="form-control-label" for="motor">Cilindrada Cc</label>
            <input
              type="number"
              class="form-control"
              name="cilindradaCc"
              id="motor-cilindradaCc"
              data-cy="cilindradaCc"
              :class="{ valid: !v$.cilindradaCc.$invalid, invalid: v$.cilindradaCc.$invalid }"
              v-model.number="v$.cilindradaCc.$model"
            />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="motor">Cilindro Cant</label>
            <input
              type="number"
              class="form-control"
              name="cilindroCant"
              id="motor-cilindroCant"
              data-cy="cilindroCant"
              :class="{ valid: !v$.cilindroCant.$invalid, invalid: v$.cilindroCant.$invalid }"
              v-model.number="v$.cilindroCant.$model"
            />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="motor">Potencia Hp</label>
            <input
              type="number"
              class="form-control"
              name="potenciaHp"
              id="motor-potenciaHp"
              data-cy="potenciaHp"
              :class="{ valid: !v$.potenciaHp.$invalid, invalid: v$.potenciaHp.$invalid }"
              v-model.number="v$.potenciaHp.$model"
            />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="motor">Turbo</label>
            <input
              type="checkbox"
              class="form-check"
              name="turbo"
              id="motor-turbo"
              data-cy="turbo"
              :class="{ valid: !v$.turbo.$invalid, invalid: v$.turbo.$invalid }"
              v-model="v$.turbo.$model"
            />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="motor">Combustible</label>
            <select class="form-control" id="motor-combustible" data-cy="combustible" name="combustible" v-model="motor.combustible">
              <option :value="null"></option>
              <option
                :value="motor.combustible && combustibleOption.id === motor.combustible.id ? motor.combustible : combustibleOption"
                v-for="combustibleOption in combustibles"
                :key="combustibleOption.id"
              >
                {{ combustibleOption.id }}
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
<script lang="ts" src="./motor-update.component.ts"></script>
