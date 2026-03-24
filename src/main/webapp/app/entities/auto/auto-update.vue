<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.auto.home.createOrEditLabel" data-cy="AutoCreateUpdateHeading">Crear o editar Auto</h2>
        <div>
          <div class="mb-3" v-if="auto.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="auto.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="auto">Estado</label>
            <select
              class="form-control"
              name="estado"
              :class="{ valid: !v$.estado.$invalid, invalid: v$.estado.$invalid }"
              v-model="v$.estado.$model"
              id="auto-estado"
              data-cy="estado"
              required
            >
              <option v-for="estadoAuto in estadoAutoValues" :key="estadoAuto" :value="estadoAuto">{{ estadoAuto }}</option>
            </select>
            <div v-if="v$.estado.$anyDirty && v$.estado.$invalid">
              <small class="form-text text-danger" v-for="error of v$.estado.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="auto">Condicion</label>
            <select
              class="form-control"
              name="condicion"
              :class="{ valid: !v$.condicion.$invalid, invalid: v$.condicion.$invalid }"
              v-model="v$.condicion.$model"
              id="auto-condicion"
              data-cy="condicion"
              required
            >
              <option v-for="condicionAuto in condicionAutoValues" :key="condicionAuto" :value="condicionAuto">{{ condicionAuto }}</option>
            </select>
            <div v-if="v$.condicion.$anyDirty && v$.condicion.$invalid">
              <small class="form-text text-danger" v-for="error of v$.condicion.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="auto">Fecha Fabricacion</label>
            <b-input-group class="mb-3">
              <b-input-group-prepend>
                <b-form-datepicker
                  aria-controls="auto-fechaFabricacion"
                  v-model="v$.fechaFabricacion.$model"
                  name="fechaFabricacion"
                  class="form-control"
                  :locale="currentLanguage"
                  button-only
                  today-button
                  reset-button
                  close-button
                >
                </b-form-datepicker>
              </b-input-group-prepend>
              <b-form-input
                id="auto-fechaFabricacion"
                data-cy="fechaFabricacion"
                type="text"
                class="form-control"
                name="fechaFabricacion"
                :class="{ valid: !v$.fechaFabricacion.$invalid, invalid: v$.fechaFabricacion.$invalid }"
                v-model="v$.fechaFabricacion.$model"
              />
            </b-input-group>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="auto">Km</label>
            <input
              type="number"
              class="form-control"
              name="km"
              id="auto-km"
              data-cy="km"
              :class="{ valid: !v$.km.$invalid, invalid: v$.km.$invalid }"
              v-model.number="v$.km.$model"
            />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="auto">Patente</label>
            <input
              type="text"
              class="form-control"
              name="patente"
              id="auto-patente"
              data-cy="patente"
              :class="{ valid: !v$.patente.$invalid, invalid: v$.patente.$invalid }"
              v-model="v$.patente.$model"
            />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="auto">Precio</label>
            <input
              type="number"
              class="form-control"
              name="precio"
              id="auto-precio"
              data-cy="precio"
              :class="{ valid: !v$.precio.$invalid, invalid: v$.precio.$invalid }"
              v-model.number="v$.precio.$model"
            />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="auto">Configuracion</label>
            <select class="form-control" id="auto-configuracion" data-cy="configuracion" name="configuracion" v-model="auto.configuracion">
              <option :value="null"></option>
              <option
                :value="
                  auto.configuracion && configuracionAutoOption.id === auto.configuracion.id ? auto.configuracion : configuracionAutoOption
                "
                v-for="configuracionAutoOption in configuracionAutos"
                :key="configuracionAutoOption.id"
              >
                {{ configuracionAutoOption.id }}
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
<script lang="ts" src="./auto-update.component.ts"></script>
