<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.vehiculo.home.createOrEditLabel" data-cy="VehiculoCreateUpdateHeading">Crear o editar Vehiculo</h2>
        <div>
          <div class="mb-3" v-if="vehiculo.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="vehiculo.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Estado</label>
            <select
              class="form-control"
              name="estado"
              :class="{ valid: !v$.estado.$invalid, invalid: v$.estado.$invalid }"
              v-model="v$.estado.$model"
              id="vehiculo-estado"
              data-cy="estado"
              required
            >
              <option v-for="estadoVehiculo in estadoVehiculoValues" :key="estadoVehiculo" :value="estadoVehiculo">
                {{ estadoVehiculo }}
              </option>
            </select>
            <div v-if="v$.estado.$anyDirty && v$.estado.$invalid">
              <small class="form-text text-danger" v-for="error of v$.estado.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Fecha Fabricacion</label>
            <b-input-group class="mb-3">
              <b-input-group-prepend>
                <b-form-datepicker
                  aria-controls="vehiculo-fechaFabricacion"
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
                id="vehiculo-fechaFabricacion"
                data-cy="fechaFabricacion"
                type="text"
                class="form-control"
                name="fechaFabricacion"
                :class="{ valid: !v$.fechaFabricacion.$invalid, invalid: v$.fechaFabricacion.$invalid }"
                v-model="v$.fechaFabricacion.$model"
                required
              />
            </b-input-group>
            <div v-if="v$.fechaFabricacion.$anyDirty && v$.fechaFabricacion.$invalid">
              <small class="form-text text-danger" v-for="error of v$.fechaFabricacion.$errors" :key="error.$uid">{{
                error.$message
              }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Km</label>
            <input
              type="number"
              class="form-control"
              name="km"
              id="vehiculo-km"
              data-cy="km"
              :class="{ valid: !v$.km.$invalid, invalid: v$.km.$invalid }"
              v-model.number="v$.km.$model"
              required
            />
            <div v-if="v$.km.$anyDirty && v$.km.$invalid">
              <small class="form-text text-danger" v-for="error of v$.km.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Patente</label>
            <input
              type="text"
              class="form-control"
              name="patente"
              id="vehiculo-patente"
              data-cy="patente"
              :class="{ valid: !v$.patente.$invalid, invalid: v$.patente.$invalid }"
              v-model="v$.patente.$model"
              required
            />
            <div v-if="v$.patente.$anyDirty && v$.patente.$invalid">
              <small class="form-text text-danger" v-for="error of v$.patente.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Precio</label>
            <input
              type="number"
              class="form-control"
              name="precio"
              id="vehiculo-precio"
              data-cy="precio"
              :class="{ valid: !v$.precio.$invalid, invalid: v$.precio.$invalid }"
              v-model.number="v$.precio.$model"
              required
            />
            <div v-if="v$.precio.$anyDirty && v$.precio.$invalid">
              <small class="form-text text-danger" v-for="error of v$.precio.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Created Date</label>
            <div class="d-flex">
              <input
                id="vehiculo-createdDate"
                data-cy="createdDate"
                type="datetime-local"
                class="form-control"
                name="createdDate"
                :class="{ valid: !v$.createdDate.$invalid, invalid: v$.createdDate.$invalid }"
                :value="convertDateTimeFromServer(v$.createdDate.$model)"
                @change="updateInstantField('createdDate', $event)"
              />
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Last Modified Date</label>
            <div class="d-flex">
              <input
                id="vehiculo-lastModifiedDate"
                data-cy="lastModifiedDate"
                type="datetime-local"
                class="form-control"
                name="lastModifiedDate"
                :class="{ valid: !v$.lastModifiedDate.$invalid, invalid: v$.lastModifiedDate.$invalid }"
                :value="convertDateTimeFromServer(v$.lastModifiedDate.$model)"
                @change="updateInstantField('lastModifiedDate', $event)"
              />
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Version</label>
            <select class="form-control" id="vehiculo-version" data-cy="version" name="version" v-model="vehiculo.version">
              <option :value="null"></option>
              <option
                :value="vehiculo.version && versionOption.id === vehiculo.version.id ? vehiculo.version : versionOption"
                v-for="versionOption in versions"
                :key="versionOption.id"
              >
                {{ versionOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Motor</label>
            <select class="form-control" id="vehiculo-motor" data-cy="motor" name="motor" v-model="vehiculo.motor">
              <option :value="null"></option>
              <option
                :value="vehiculo.motor && motorOption.id === vehiculo.motor.id ? vehiculo.motor : motorOption"
                v-for="motorOption in motors"
                :key="motorOption.id"
              >
                {{ motorOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="vehiculo">Tipo Vehiculo</label>
            <select
              class="form-control"
              id="vehiculo-tipoVehiculo"
              data-cy="tipoVehiculo"
              name="tipoVehiculo"
              v-model="vehiculo.tipoVehiculo"
            >
              <option :value="null"></option>
              <option
                :value="
                  vehiculo.tipoVehiculo && tipoVehiculoOption.id === vehiculo.tipoVehiculo.id ? vehiculo.tipoVehiculo : tipoVehiculoOption
                "
                v-for="tipoVehiculoOption in tipoVehiculos"
                :key="tipoVehiculoOption.id"
              >
                {{ tipoVehiculoOption.id }}
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
<script lang="ts" src="./vehiculo-update.component.ts"></script>
