<template>
  <div class="container py-4" style="max-width:640px">

    <div class="page-header">
      <div>
        <h1 class="page-title">{{ pago.id ? 'Editar pago' : 'Registrar pago' }}</h1>
        <p class="page-subtitle" v-if="pago.venta">
          Venta #{{ pago.venta.id }} — {{ pago.venta.cliente?.nombre }} {{ pago.venta.cliente?.apellido }}
        </p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="previousState()">← Volver</button>
    </div>

    <form @submit.prevent="save()" novalidate>
      <div class="card mb-3">
        <div class="card-header">Datos del pago</div>
        <div class="card-body">
          <div class="row g-3">

            <div class="col-12">
              <label class="form-label">Venta <span class="text-danger">*</span></label>
              <select class="form-select" v-model="pago.venta">
                <option :value="null">— Seleccionar venta —</option>
                <option
                  v-for="v in ventas"
                  :key="v.id"
                  :value="pago.venta?.id === v.id ? pago.venta : v"
                >
                  Venta #{{ v.id }} — {{ v.cliente?.nombre }} {{ v.cliente?.apellido }}
                  — Total: $ {{ formatPrecio(v.total) }}
                  — Saldo: $ {{ formatPrecio(v.saldo) }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Monto <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="v$.monto.$model"
                  :class="{ 'is-invalid': v$.monto.$dirty && v$.monto.$invalid }"
                  min="0"
                  step="0.01"
                />
              </div>
              <div class="invalid-feedback d-block" v-if="v$.monto.$dirty && v$.monto.$invalid">
                <span v-for="e of v$.monto.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
              <!-- Sugerencia de saldo -->
              <small v-if="pago.venta?.saldo" class="text-muted">
                Saldo pendiente: $ {{ formatPrecio(pago.venta.saldo) }}
                <button type="button" class="btn btn-link btn-sm p-0 ms-1" @click="usarSaldo">
                  Usar saldo completo
                </button>
              </small>
            </div>

            <div class="col-md-6">
              <label class="form-label">Fecha <span class="text-danger">*</span></label>
              <input
                type="datetime-local"
                class="form-control"
                :class="{ 'is-invalid': v$.fecha.$dirty && v$.fecha.$invalid }"
                :value="convertDateTimeFromServer(v$.fecha.$model)"
                @change="updateInstantField('fecha', $event)"
              />
              <div class="invalid-feedback">
                <span v-for="e of v$.fecha.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Método de pago <span class="text-danger">*</span></label>
              <select class="form-select" v-model="pago.metodoPago">
                <option :value="null">— Seleccionar método —</option>
                <option
                  v-for="m in metodoPagos"
                  :key="m.id"
                  :value="pago.metodoPago?.id === m.id ? pago.metodoPago : m"
                >
                  {{ m.descripcion ?? m.codigo }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Moneda</label>
              <select class="form-select" v-model="pago.moneda">
                <option :value="null">— Seleccionar moneda —</option>
                <option
                  v-for="m in monedas"
                  :key="m.id"
                  :value="pago.moneda?.id === m.id ? pago.moneda : m"
                >
                  {{ m.simbolo ?? '' }} {{ m.codigo }} — {{ m.descripcion }}
                </option>
              </select>
            </div>

            <div class="col-12" v-if="pago.metodoPago?.requiereReferencia">
              <label class="form-label">Referencia / Nro. comprobante</label>
              <input
                type="text"
                class="form-control"
                v-model="v$.referencia.$model"
                :class="{ 'is-invalid': v$.referencia.$dirty && v$.referencia.$invalid }"
                placeholder="Ej: 0001-00012345"
              />
              <div class="invalid-feedback">
                <span v-for="e of v$.referencia.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

          </div>
        </div>
      </div>

      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="previousState()">Cancelar</button>
        <button type="submit" class="btn btn-primary" :disabled="v$.$invalid || isSaving">
          <span v-if="isSaving" class="spinner-border spinner-border-sm me-1" />
          {{ pago.id ? 'Guardar cambios' : 'Registrar pago' }}
        </button>
      </div>

    </form>
  </div>
</template>

<script lang="ts" src="./pago-update.component.ts"></script>
