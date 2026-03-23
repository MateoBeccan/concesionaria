<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2 id="concesionariaApp.venta.home.createOrEditLabel" data-cy="VentaCreateUpdateHeading">Crear o editar Venta</h2>
        <div>
          <div class="mb-3" v-if="venta.id">
            <label for="id">ID</label>
            <input type="text" class="form-control" id="id" name="id" v-model="venta.id" readonly />
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Fecha</label>
            <div class="d-flex">
              <input
                id="venta-fecha"
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
            <label class="form-control-label" for="venta">Cotizacion</label>
            <input
              type="number"
              class="form-control"
              name="cotizacion"
              id="venta-cotizacion"
              data-cy="cotizacion"
              :class="{ valid: !v$.cotizacion.$invalid, invalid: v$.cotizacion.$invalid }"
              v-model.number="v$.cotizacion.$model"
              required
            />
            <div v-if="v$.cotizacion.$anyDirty && v$.cotizacion.$invalid">
              <small class="form-text text-danger" v-for="error of v$.cotizacion.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Importe Neto</label>
            <input
              type="number"
              class="form-control"
              name="importeNeto"
              id="venta-importeNeto"
              data-cy="importeNeto"
              :class="{ valid: !v$.importeNeto.$invalid, invalid: v$.importeNeto.$invalid }"
              v-model.number="v$.importeNeto.$model"
              required
            />
            <div v-if="v$.importeNeto.$anyDirty && v$.importeNeto.$invalid">
              <small class="form-text text-danger" v-for="error of v$.importeNeto.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Impuesto</label>
            <input
              type="number"
              class="form-control"
              name="impuesto"
              id="venta-impuesto"
              data-cy="impuesto"
              :class="{ valid: !v$.impuesto.$invalid, invalid: v$.impuesto.$invalid }"
              v-model.number="v$.impuesto.$model"
              required
            />
            <div v-if="v$.impuesto.$anyDirty && v$.impuesto.$invalid">
              <small class="form-text text-danger" v-for="error of v$.impuesto.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Total</label>
            <input
              type="number"
              class="form-control"
              name="total"
              id="venta-total"
              data-cy="total"
              :class="{ valid: !v$.total.$invalid, invalid: v$.total.$invalid }"
              v-model.number="v$.total.$model"
              required
            />
            <div v-if="v$.total.$anyDirty && v$.total.$invalid">
              <small class="form-text text-danger" v-for="error of v$.total.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Porcentaje Impuesto</label>
            <input
              type="number"
              class="form-control"
              name="porcentajeImpuesto"
              id="venta-porcentajeImpuesto"
              data-cy="porcentajeImpuesto"
              :class="{ valid: !v$.porcentajeImpuesto.$invalid, invalid: v$.porcentajeImpuesto.$invalid }"
              v-model.number="v$.porcentajeImpuesto.$model"
            />
            <div v-if="v$.porcentajeImpuesto.$anyDirty && v$.porcentajeImpuesto.$invalid">
              <small class="form-text text-danger" v-for="error of v$.porcentajeImpuesto.$errors" :key="error.$uid">{{
                error.$message
              }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Total Pagado</label>
            <input
              type="number"
              class="form-control"
              name="totalPagado"
              id="venta-totalPagado"
              data-cy="totalPagado"
              :class="{ valid: !v$.totalPagado.$invalid, invalid: v$.totalPagado.$invalid }"
              v-model.number="v$.totalPagado.$model"
            />
            <div v-if="v$.totalPagado.$anyDirty && v$.totalPagado.$invalid">
              <small class="form-text text-danger" v-for="error of v$.totalPagado.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Saldo</label>
            <input
              type="number"
              class="form-control"
              name="saldo"
              id="venta-saldo"
              data-cy="saldo"
              :class="{ valid: !v$.saldo.$invalid, invalid: v$.saldo.$invalid }"
              v-model.number="v$.saldo.$model"
            />
            <div v-if="v$.saldo.$anyDirty && v$.saldo.$invalid">
              <small class="form-text text-danger" v-for="error of v$.saldo.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Observaciones</label>
            <input
              type="text"
              class="form-control"
              name="observaciones"
              id="venta-observaciones"
              data-cy="observaciones"
              :class="{ valid: !v$.observaciones.$invalid, invalid: v$.observaciones.$invalid }"
              v-model="v$.observaciones.$model"
            />
            <div v-if="v$.observaciones.$anyDirty && v$.observaciones.$invalid">
              <small class="form-text text-danger" v-for="error of v$.observaciones.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Cliente</label>
            <select class="form-control" id="venta-cliente" data-cy="cliente" name="cliente" v-model="venta.cliente">
              <option :value="null"></option>
              <option
                :value="venta.cliente && clienteOption.id === venta.cliente.id ? venta.cliente : clienteOption"
                v-for="clienteOption in clientes"
                :key="clienteOption.id"
              >
                {{ clienteOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Estado Venta</label>
            <select class="form-control" id="venta-estadoVenta" data-cy="estadoVenta" name="estadoVenta" v-model="venta.estadoVenta">
              <option :value="null"></option>
              <option
                :value="venta.estadoVenta && estadoVentaOption.id === venta.estadoVenta.id ? venta.estadoVenta : estadoVentaOption"
                v-for="estadoVentaOption in estadoVentas"
                :key="estadoVentaOption.id"
              >
                {{ estadoVentaOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">Moneda</label>
            <select class="form-control" id="venta-moneda" data-cy="moneda" name="moneda" v-model="venta.moneda">
              <option :value="null"></option>
              <option
                :value="venta.moneda && monedaOption.id === venta.moneda.id ? venta.moneda : monedaOption"
                v-for="monedaOption in monedas"
                :key="monedaOption.id"
              >
                {{ monedaOption.id }}
              </option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="venta">User</label>
            <select class="form-control" id="venta-user" data-cy="user" name="user" v-model="venta.user">
              <option :value="null"></option>
              <option
                :value="venta.user && userOption.id === venta.user.id ? venta.user : userOption"
                v-for="userOption in users"
                :key="userOption.id"
              >
                {{ userOption.login }}
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
<script lang="ts" src="./venta-update.component.ts"></script>
