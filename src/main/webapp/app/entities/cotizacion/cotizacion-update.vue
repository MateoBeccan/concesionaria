<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.cotizacion.home.createOrEditLabel" data-cy="CotizacionCreateUpdateHeading">Crear o editar Cotizacion</h2>
        <div>
          <div class="mb-3" v-if="cotizacion.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="cotizacion.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cotizacion">Fecha</label>
            <div class="d-flex">
              <input
                id="cotizacion-fecha"
                data-cy="fecha"
                type="datetime-local"
                class="form-control"
                name="fecha"
                :class="{ valid: !v$.fecha.$invalid, invalid: v$.fecha.$invalid }"
                required
                :value="convertDateTimeFromServer(v$.fecha.$model)"
                @change="updateInstantField('fecha', $event)"
              />
            </div>
            <div v-if="v$.fecha.$anyDirty && v$.fecha.$invalid">
              <small class="form-text text-danger" v-for="error of v$.fecha.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cotizacion">Valor Compra</label>
            <input
              type="number"
              class="form-control"
              name="valorCompra"
              id="cotizacion-valorCompra"
              data-cy="valorCompra"
              :class="{ valid: !v$.valorCompra.$invalid, invalid: v$.valorCompra.$invalid }"
              v-model.number="v$.valorCompra.$model"
              required
            />
            <div v-if="v$.valorCompra.$anyDirty && v$.valorCompra.$invalid">
              <small class="form-text text-danger" v-for="error of v$.valorCompra.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cotizacion">Valor Venta</label>
            <input
              type="number"
              class="form-control"
              name="valorVenta"
              id="cotizacion-valorVenta"
              data-cy="valorVenta"
              :class="{ valid: !v$.valorVenta.$invalid, invalid: v$.valorVenta.$invalid }"
              v-model.number="v$.valorVenta.$model"
              required
            />
            <div v-if="v$.valorVenta.$anyDirty && v$.valorVenta.$invalid">
              <small class="form-text text-danger" v-for="error of v$.valorVenta.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="cotizacion">Activo</label>
            <input
              type="checkbox"
              class="form-check"
              name="activo"
              id="cotizacion-activo"
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
            <label class="form-control-label" for="cotizacion">Moneda</label>
            <select class="form-control" id="cotizacion-moneda" data-cy="moneda" name="moneda" v-model="cotizacion.moneda">
              <option :value="null"></option>
              <option
                :value="cotizacion.moneda && monedaOption.id === cotizacion.moneda.id ? cotizacion.moneda : monedaOption"
                v-for="monedaOption in monedas"
                :key="monedaOption.id"
              >
                {{ monedaOption.id }}
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
<script lang="ts" src="./cotizacion-update.component.ts"></script>
