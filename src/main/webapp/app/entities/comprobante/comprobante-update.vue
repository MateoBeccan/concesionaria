<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.comprobante.home.createOrEditLabel" data-cy="ComprobanteCreateUpdateHeading">
          Crear o editar Comprobante
        </h2>
        <div>
          <div class="mb-3" v-if="comprobante.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="comprobante.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="comprobante">Numero Comprobante</label>
            <input
              type="text"
              class="form-control"
              name="numeroComprobante"
              id="comprobante-numeroComprobante"
              data-cy="numeroComprobante"
              :class="{ valid: !v$.numeroComprobante.$invalid, invalid: v$.numeroComprobante.$invalid }"
              v-model="v$.numeroComprobante.$model"
              required
            />
            <div v-if="v$.numeroComprobante.$anyDirty && v$.numeroComprobante.$invalid">
              <small class="form-text text-danger" v-for="error of v$.numeroComprobante.$errors" :key="error.$uid">{{
                error.$message
              }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="comprobante">Fecha Emision</label>
            <div class="d-flex">
              <input
                id="comprobante-fechaEmision"
                data-cy="fechaEmision"
                type="datetime-local"
                class="form-control"
                name="fechaEmision"
                :class="{ valid: !v$.fechaEmision.$invalid, invalid: v$.fechaEmision.$invalid }"
                required
                :value="convertDateTimeFromServer(v$.fechaEmision.$model)"
                @change="updateInstantField('fechaEmision', $event)"
              />
            </div>
            <div v-if="v$.fechaEmision.$anyDirty && v$.fechaEmision.$invalid">
              <small class="form-text text-danger" v-for="error of v$.fechaEmision.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="comprobante">Importe Neto</label>
            <input
              type="number"
              class="form-control"
              name="importeNeto"
              id="comprobante-importeNeto"
              data-cy="importeNeto"
              :class="{ valid: !v$.importeNeto.$invalid, invalid: v$.importeNeto.$invalid }"
              v-model.number="v$.importeNeto.$model"
            />
            <div v-if="v$.importeNeto.$anyDirty && v$.importeNeto.$invalid">
              <small class="form-text text-danger" v-for="error of v$.importeNeto.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="comprobante">Impuesto</label>
            <input
              type="number"
              class="form-control"
              name="impuesto"
              id="comprobante-impuesto"
              data-cy="impuesto"
              :class="{ valid: !v$.impuesto.$invalid, invalid: v$.impuesto.$invalid }"
              v-model.number="v$.impuesto.$model"
            />
            <div v-if="v$.impuesto.$anyDirty && v$.impuesto.$invalid">
              <small class="form-text text-danger" v-for="error of v$.impuesto.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="comprobante">Total</label>
            <input
              type="number"
              class="form-control"
              name="total"
              id="comprobante-total"
              data-cy="total"
              :class="{ valid: !v$.total.$invalid, invalid: v$.total.$invalid }"
              v-model.number="v$.total.$model"
            />
            <div v-if="v$.total.$anyDirty && v$.total.$invalid">
              <small class="form-text text-danger" v-for="error of v$.total.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="comprobante">Venta</label>
            <select class="form-control" id="comprobante-venta" data-cy="venta" name="venta" v-model="comprobante.venta">
              <option :value="null"></option>
              <option
                :value="comprobante.venta && ventaOption.id === comprobante.venta.id ? comprobante.venta : ventaOption"
                v-for="ventaOption in ventas"
                :key="ventaOption.id"
              >
                {{ ventaOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="comprobante">Tipo Comprobante</label>
            <select
              class="form-control"
              id="comprobante-tipoComprobante"
              data-cy="tipoComprobante"
              name="tipoComprobante"
              v-model="comprobante.tipoComprobante"
            >
              <option :value="null"></option>
              <option
                :value="
                  comprobante.tipoComprobante && tipoComprobanteOption.id === comprobante.tipoComprobante.id
                    ? comprobante.tipoComprobante
                    : tipoComprobanteOption
                "
                v-for="tipoComprobanteOption in tipoComprobantes"
                :key="tipoComprobanteOption.id"
              >
                {{ tipoComprobanteOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="comprobante">Moneda</label>
            <select class="form-control" id="comprobante-moneda" data-cy="moneda" name="moneda" v-model="comprobante.moneda">
              <option :value="null"></option>
              <option
                :value="comprobante.moneda && monedaOption.id === comprobante.moneda.id ? comprobante.moneda : monedaOption"
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
<script lang="ts" src="./comprobante-update.component.ts"></script>
