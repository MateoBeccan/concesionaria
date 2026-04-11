<template>
  <div class="container py-4" style="max-width: 720px">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ venta.id ? `Editar venta #${venta.id}` : 'Nueva venta' }}</h1>
        <p class="page-subtitle">Completá los datos de la operación</p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="previousState()">← Volver</button>
    </div>

    <form @submit.prevent="save()" novalidate>
      <!-- PARTES -->
      <div class="card mb-3">
        <div class="card-header">Partes de la operación</div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-12">
              <label class="form-label">Cliente</label>
              <select class="form-select" v-model="venta.cliente">
                <option :value="null">— Seleccionar cliente —</option>
                <option v-for="c in clientes" :key="c.id" :value="c">{{ c.nombre }} {{ c.apellido }} — {{ c.nroDocumento }}</option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Estado</label>
              <select class="form-select" v-model="venta.estado">
                <option :value="null">— Seleccionar estado —</option>
                <option v-for="e in estadoVentas" :key="e" :value="e">
                  {{ { PENDIENTE: 'Pendiente', PAGADA: 'Pagada', CANCELADA: 'Cancelada' }[e] ?? e }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Moneda</label>
              <select class="form-select" v-model="venta.moneda">
                <option :value="null">— Seleccionar moneda —</option>
                <option v-for="m in monedas" :key="m.id" :value="m">
                  {{ m.codigo ?? m.nombre ?? `Moneda ${m.id}` }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Usuario responsable</label>
              <select class="form-select" v-model="venta.user">
                <option :value="null">— Sin asignar —</option>
                <option v-for="u in users" :key="u.id" :value="u">{{ u.login }}</option>
              </select>
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
          </div>
        </div>
      </div>

      <!-- IMPORTES -->
      <div class="card mb-3">
        <div class="card-header">Importes</div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Cotización <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="v$.cotizacion.$model"
                  :class="{ 'is-invalid': v$.cotizacion.$dirty && v$.cotizacion.$invalid }"
                  min="0"
                />
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">% Impuesto</label>
              <div class="input-group">
                <input type="number" class="form-control" v-model.number="v$.porcentajeImpuesto.$model" min="0" max="100" />
                <span class="input-group-text">%</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Importe neto <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="v$.importeNeto.$model"
                  :class="{ 'is-invalid': v$.importeNeto.$dirty && v$.importeNeto.$invalid }"
                  min="0"
                />
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Impuesto <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="v$.impuesto.$model"
                  :class="{ 'is-invalid': v$.impuesto.$dirty && v$.impuesto.$invalid }"
                  min="0"
                />
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Total <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="v$.total.$model"
                  :class="{ 'is-invalid': v$.total.$dirty && v$.total.$invalid }"
                  min="0"
                />
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Total pagado</label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input type="number" class="form-control" v-model.number="v$.totalPagado.$model" min="0" />
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Saldo</label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input type="number" class="form-control" v-model.number="v$.saldo.$model" min="0" />
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">Observaciones</label>
              <textarea class="form-control" v-model="v$.observaciones.$model" rows="3" placeholder="Notas adicionales..." />
              <div class="invalid-feedback d-block" v-if="v$.observaciones.$dirty && v$.observaciones.$invalid">
                <span v-for="e of v$.observaciones.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ACCIONES -->
      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="previousState()">Cancelar</button>
        <button type="submit" class="btn btn-primary" :disabled="v$.$invalid || isSaving">
          <span v-if="isSaving" class="spinner-border spinner-border-sm me-1" />
          {{ venta.id ? 'Guardar cambios' : 'Crear venta' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script lang="ts" src="./venta-update.component.ts"></script>
