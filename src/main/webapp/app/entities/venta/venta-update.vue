<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">

        <h2>Crear o editar Venta</h2>

        <div>
          <div class="mb-3" v-if="venta.id">
            <label>ID</label>
            <input type="text" class="form-control" v-model="venta.id" readonly />
          </div>

          <!-- FECHA -->
          <div class="mb-3">
            <label>Fecha</label>
            <input
              type="datetime-local"
              class="form-control"
              :value="convertDateTimeFromServer(v$.fecha.$model)"
              @change="updateInstantField('fecha', $event)"
            />
          </div>

          <!-- IMPORTES -->
          <div class="mb-3">
            <label>Cotización</label>
            <input type="number" class="form-control" v-model.number="v$.cotizacion.$model" />
          </div>

          <div class="mb-3">
            <label>Importe Neto</label>
            <input type="number" class="form-control" v-model.number="v$.importeNeto.$model" />
          </div>

          <div class="mb-3">
            <label>Impuesto</label>
            <input type="number" class="form-control" v-model.number="v$.impuesto.$model" />
          </div>

          <div class="mb-3">
            <label>Total</label>
            <input type="number" class="form-control" v-model.number="v$.total.$model" />
          </div>

          <div class="mb-3">
            <label>% Impuesto</label>
            <input type="number" class="form-control" v-model.number="v$.porcentajeImpuesto.$model" />
          </div>

          <div class="mb-3">
            <label>Total Pagado</label>
            <input type="number" class="form-control" v-model.number="v$.totalPagado.$model" />
          </div>

          <div class="mb-3">
            <label>Saldo</label>
            <input type="number" class="form-control" v-model.number="v$.saldo.$model" />
          </div>

          <div class="mb-3">
            <label>Observaciones</label>
            <input type="text" class="form-control" v-model="v$.observaciones.$model" />
          </div>

          <!-- CLIENTE -->
          <div class="mb-3">
            <label>Cliente</label>
            <select class="form-control" v-model="venta.cliente">
              <option :value="null"></option>
              <option v-for="c in clientes" :key="c.id" :value="c">
                {{ c.nombre }} {{ c.apellido }}
              </option>
            </select>
          </div>

          <!-- ESTADO -->
          <div class="mb-3">
            <label>Estado</label>
            <select class="form-control" v-model="venta.estadoVenta">
              <option :value="null"></option>
              <option v-for="e in estadoVentas" :key="e" :value="e">
                {{ e }}
              </option>
            </select>
          </div>

          <!-- MONEDA -->
          <div class="mb-3">
            <label>Moneda</label>
            <select class="form-control" v-model="venta.moneda">
              <option :value="null"></option>
              <option v-for="m in monedas" :key="m.id" :value="m">
                {{ m.codigo }}
              </option>
            </select>
          </div>

          <!-- USER -->
          <div class="mb-3">
            <label>Usuario</label>
            <select class="form-control" v-model="venta.user">
              <option :value="null"></option>
              <option v-for="u in users" :key="u.id" :value="u">
                {{ u.login }}
              </option>
            </select>
          </div>

        </div>

        <div class="mt-3">
          <button type="button" class="btn btn-secondary" @click="previousState()">
            Cancelar
          </button>

          <button type="submit" :disabled="v$.$invalid || isSaving" class="btn btn-primary ms-2">
            Guardar
          </button>
        </div>

      </form>
    </div>
  </div>
</template>

<script lang="ts" src="./venta-update.component.ts"></script>
