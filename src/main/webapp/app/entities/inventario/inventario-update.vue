<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.inventario.home.createOrEditLabel" data-cy="InventarioCreateUpdateHeading">Crear o editar Inventario</h2>
        <div>
          <div class="mb-3" v-if="inventario.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="inventario.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="inventario">Fecha Ingreso</label>
            <div class="d-flex">
              <input
                id="inventario-fechaIngreso"
                data-cy="fechaIngreso"
                type="datetime-local"
                class="form-control"
                name="fechaIngreso"
                :class="{ valid: !v$.fechaIngreso.$invalid, invalid: v$.fechaIngreso.$invalid }"
                required
                :value="convertDateTimeFromServer(v$.fechaIngreso.$model)"
                @change="updateInstantField('fechaIngreso', $event)"
              />
            </div>
            <div v-if="v$.fechaIngreso.$anyDirty && v$.fechaIngreso.$invalid">
              <small class="form-text text-danger" v-for="error of v$.fechaIngreso.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="inventario">Ubicacion</label>
            <input
              type="text"
              class="form-control"
              name="ubicacion"
              id="inventario-ubicacion"
              data-cy="ubicacion"
              :class="{ valid: !v$.ubicacion.$invalid, invalid: v$.ubicacion.$invalid }"
              v-model="v$.ubicacion.$model"
            />
            <div v-if="v$.ubicacion.$anyDirty && v$.ubicacion.$invalid">
              <small class="form-text text-danger" v-for="error of v$.ubicacion.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="inventario">Estado Inventario</label>
            <select
              class="form-control"
              name="estadoInventario"
              :class="{ valid: !v$.estadoInventario.$invalid, invalid: v$.estadoInventario.$invalid }"
              v-model="v$.estadoInventario.$model"
              id="inventario-estadoInventario"
              data-cy="estadoInventario"
              required
            >
              <option v-for="estadoInventario in estadoInventarioValues" :key="estadoInventario" :value="estadoInventario">
                {{ estadoInventario }}
              </option>
            </select>
            <div v-if="v$.estadoInventario.$anyDirty && v$.estadoInventario.$invalid">
              <small class="form-text text-danger" v-for="error of v$.estadoInventario.$errors" :key="error.$uid">{{
                error.$message
              }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="inventario">Observaciones</label>
            <input
              type="text"
              class="form-control"
              name="observaciones"
              id="inventario-observaciones"
              data-cy="observaciones"
              :class="{ valid: !v$.observaciones.$invalid, invalid: v$.observaciones.$invalid }"
              v-model="v$.observaciones.$model"
            />
            <div v-if="v$.observaciones.$anyDirty && v$.observaciones.$invalid">
              <small class="form-text text-danger" v-for="error of v$.observaciones.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="inventario">Disponible</label>
            <input
              type="checkbox"
              class="form-check"
              name="disponible"
              id="inventario-disponible"
              data-cy="disponible"
              :class="{ valid: !v$.disponible.$invalid, invalid: v$.disponible.$invalid }"
              v-model="v$.disponible.$model"
              required
            />
            <div v-if="v$.disponible.$anyDirty && v$.disponible.$invalid">
              <small class="form-text text-danger" v-for="error of v$.disponible.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="inventario">Created Date</label>
            <div class="d-flex">
              <input
                id="inventario-createdDate"
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
            <label class="form-control-label" for="inventario">Last Modified Date</label>
            <div class="d-flex">
              <input
                id="inventario-lastModifiedDate"
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
            <label class="form-control-label" for="inventario">Fecha Reserva</label>
            <div class="d-flex">
              <input
                id="inventario-fechaReserva"
                data-cy="fechaReserva"
                type="datetime-local"
                class="form-control"
                name="fechaReserva"
                :class="{ valid: !v$.fechaReserva.$invalid, invalid: v$.fechaReserva.$invalid }"
                :value="convertDateTimeFromServer(v$.fechaReserva.$model)"
                @change="updateInstantField('fechaReserva', $event)"
              />
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="inventario">Fecha Vencimiento Reserva</label>
            <div class="d-flex">
              <input
                id="inventario-fechaVencimientoReserva"
                data-cy="fechaVencimientoReserva"
                type="datetime-local"
                class="form-control"
                name="fechaVencimientoReserva"
                :class="{ valid: !v$.fechaVencimientoReserva.$invalid, invalid: v$.fechaVencimientoReserva.$invalid }"
                :value="convertDateTimeFromServer(v$.fechaVencimientoReserva.$model)"
                @change="updateInstantField('fechaVencimientoReserva', $event)"
              />
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="inventario">Vehiculo</label>
            <select class="form-control" id="inventario-vehiculo" data-cy="vehiculo" name="vehiculo" v-model="inventario.vehiculo">
              <option :value="null"></option>
              <option
                :value="inventario.vehiculo && vehiculoOption.id === inventario.vehiculo.id ? inventario.vehiculo : vehiculoOption"
                v-for="vehiculoOption in vehiculos"
                :key="vehiculoOption.id"
              >
                {{ vehiculoOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="inventario">Cliente Reserva</label>
            <select
              class="form-control"
              id="inventario-clienteReserva"
              data-cy="clienteReserva"
              name="clienteReserva"
              v-model="inventario.clienteReserva"
            >
              <option :value="null"></option>
              <option
                :value="
                  inventario.clienteReserva && clienteOption.id === inventario.clienteReserva.id ? inventario.clienteReserva : clienteOption
                "
                v-for="clienteOption in clientes"
                :key="clienteOption.id"
              >
                {{ clienteOption.id }}
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
<script lang="ts" src="./inventario-update.component.ts"></script>
