<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.pago.home.createOrEditLabel" data-cy="PagoCreateUpdateHeading">Crear o editar Pago</h2>
        <div>
          <div class="mb-3" v-if="pago.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="pago.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="pago">Monto</label>
            <input
              type="number"
              class="form-control"
              name="monto"
              id="pago-monto"
              data-cy="monto"
              :class="{ valid: !v$.monto.$invalid, invalid: v$.monto.$invalid }"
              v-model.number="v$.monto.$model"
              required
            />
            <div v-if="v$.monto.$anyDirty && v$.monto.$invalid">
              <small class="form-text text-danger" v-for="error of v$.monto.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="pago">Fecha</label>
            <div class="d-flex">
              <input
                id="pago-fecha"
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
            <label class="form-control-label" for="pago">Referencia</label>
            <input
              type="text"
              class="form-control"
              name="referencia"
              id="pago-referencia"
              data-cy="referencia"
              :class="{ valid: !v$.referencia.$invalid, invalid: v$.referencia.$invalid }"
              v-model="v$.referencia.$model"
            />
            <div v-if="v$.referencia.$anyDirty && v$.referencia.$invalid">
              <small class="form-text text-danger" v-for="error of v$.referencia.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="pago">Created Date</label>
            <div class="d-flex">
              <input
                id="pago-createdDate"
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
            <label class="form-control-label" for="pago">Venta</label>
            <select class="form-control" id="pago-venta" data-cy="venta" name="venta" v-model="pago.venta">
              <option :value="null"></option>
              <option
                :value="pago.venta && ventaOption.id === pago.venta.id ? pago.venta : ventaOption"
                v-for="ventaOption in ventas"
                :key="ventaOption.id"
              >
                {{ ventaOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="pago">Metodo Pago</label>
            <select class="form-control" id="pago-metodoPago" data-cy="metodoPago" name="metodoPago" v-model="pago.metodoPago">
              <option :value="null"></option>
              <option
                :value="pago.metodoPago && metodoPagoOption.id === pago.metodoPago.id ? pago.metodoPago : metodoPagoOption"
                v-for="metodoPagoOption in metodoPagos"
                :key="metodoPagoOption.id"
              >
                {{ metodoPagoOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="pago">Moneda</label>
            <select class="form-control" id="pago-moneda" data-cy="moneda" name="moneda" v-model="pago.moneda">
              <option :value="null"></option>
              <option
                :value="pago.moneda && monedaOption.id === pago.moneda.id ? pago.moneda : monedaOption"
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
<script lang="ts" src="./pago-update.component.ts"></script>
