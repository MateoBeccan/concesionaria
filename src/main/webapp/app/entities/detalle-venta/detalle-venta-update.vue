<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.detalleVenta.home.createOrEditLabel" data-cy="DetalleVentaCreateUpdateHeading">
          Crear o editar Detalle Venta
        </h2>
        <div>
          <div class="mb-3" v-if="detalleVenta.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="detalleVenta.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="detalle-venta">Precio Unitario</label>
            <input
              type="number"
              class="form-control"
              name="precioUnitario"
              id="detalle-venta-precioUnitario"
              data-cy="precioUnitario"
              :class="{ valid: !v$.precioUnitario.$invalid, invalid: v$.precioUnitario.$invalid }"
              v-model.number="v$.precioUnitario.$model"
              required
            />
            <div v-if="v$.precioUnitario.$anyDirty && v$.precioUnitario.$invalid">
              <small class="form-text text-danger" v-for="error of v$.precioUnitario.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="detalle-venta">Cantidad</label>
            <input
              type="number"
              class="form-control"
              name="cantidad"
              id="detalle-venta-cantidad"
              data-cy="cantidad"
              :class="{ valid: !v$.cantidad.$invalid, invalid: v$.cantidad.$invalid }"
              v-model.number="v$.cantidad.$model"
              required
            />
            <div v-if="v$.cantidad.$anyDirty && v$.cantidad.$invalid">
              <small class="form-text text-danger" v-for="error of v$.cantidad.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="detalle-venta">Subtotal</label>
            <input
              type="number"
              class="form-control"
              name="subtotal"
              id="detalle-venta-subtotal"
              data-cy="subtotal"
              :class="{ valid: !v$.subtotal.$invalid, invalid: v$.subtotal.$invalid }"
              v-model.number="v$.subtotal.$model"
              required
            />
            <div v-if="v$.subtotal.$anyDirty && v$.subtotal.$invalid">
              <small class="form-text text-danger" v-for="error of v$.subtotal.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="detalle-venta">Venta</label>
            <select class="form-control" id="detalle-venta-venta" data-cy="venta" name="venta" v-model="detalleVenta.venta">
              <option :value="null"></option>
              <option
                :value="detalleVenta.venta && ventaOption.id === detalleVenta.venta.id ? detalleVenta.venta : ventaOption"
                v-for="ventaOption in ventas"
                :key="ventaOption.id"
              >
                {{ ventaOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="detalle-venta">Auto</label>
            <select class="form-control" id="detalle-venta-auto" data-cy="auto" name="auto" v-model="detalleVenta.auto">
              <option :value="null"></option>
              <option
                :value="detalleVenta.auto && autoOption.id === detalleVenta.auto.id ? detalleVenta.auto : autoOption"
                v-for="autoOption in autos"
                :key="autoOption.id"
              >
                {{ autoOption.id }}
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
<script lang="ts" src="./detalle-venta-update.component.ts"></script>
