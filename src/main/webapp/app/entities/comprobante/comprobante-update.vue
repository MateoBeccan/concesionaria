<template>
  <div class="container py-4" style="max-width:640px">

    <div class="page-header">
      <div>
        <h1 class="page-title">{{ comprobante.id ? 'Editar comprobante' : 'Nuevo comprobante' }}</h1>
        <p class="page-subtitle">Facturación asociada a una venta</p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="previousState()">← Volver</button>
    </div>

    <form @submit.prevent="save()" novalidate>

      <div class="card mb-3">
        <div class="card-header">Datos del comprobante</div>
        <div class="card-body">
          <div class="row g-3">

            <div class="col-md-6">
              <label class="form-label">Número de comprobante <span class="text-danger">*</span></label>
              <input
                type="text"
                class="form-control"
                v-model="v$.numeroComprobante.$model"
                :class="{ 'is-invalid': v$.numeroComprobante.$dirty && v$.numeroComprobante.$invalid }"
                placeholder="Ej: 0001-00000001"
              />
              <div class="invalid-feedback">
                <span v-for="e of v$.numeroComprobante.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="form-label">Fecha de emisión <span class="text-danger">*</span></label>
              <input
                type="datetime-local"
                class="form-control"
                :class="{ 'is-invalid': v$.fechaEmision.$dirty && v$.fechaEmision.$invalid }"
                :value="convertDateTimeFromServer(v$.fechaEmision.$model)"
                @change="updateInstantField('fechaEmision', $event)"
              />
              <div class="invalid-feedback">
                <span v-for="e of v$.fechaEmision.$errors" :key="e.$uid">{{ e.$message }}</span>
              </div>
            </div>

            <div class="col-12">
              <label class="form-label">Venta asociada</label>
              <select class="form-select" v-model="comprobante.venta">
                <option :value="null">— Seleccionar venta —</option>
                <option
                  v-for="v in ventas"
                  :key="v.id"
                  :value="comprobante.venta?.id === v.id ? comprobante.venta : v"
                >
                  Venta #{{ v.id }} — {{ v.cliente?.nombre }} {{ v.cliente?.apellido }}
                  ({{ formatFecha(v.fecha) }}) — $ {{ formatPrecio(v.total) }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Tipo de comprobante</label>
              <select class="form-select" v-model="comprobante.tipoComprobante">
                <option :value="null">— Seleccionar tipo —</option>
                <option
                  v-for="t in tipoComprobantes"
                  :key="t.id"
                  :value="comprobante.tipoComprobante?.id === t.id ? comprobante.tipoComprobante : t"
                >
                  {{ t.codigo }} — {{ t.descripcion }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Moneda</label>
              <select class="form-select" v-model="comprobante.moneda">
                <option :value="null">— Seleccionar moneda —</option>
                <option
                  v-for="m in monedas"
                  :key="m.id"
                  :value="comprobante.moneda?.id === m.id ? comprobante.moneda : m"
                >
                  {{ m.simbolo ?? '' }} {{ m.codigo }} — {{ m.descripcion }}
                </option>
              </select>
            </div>

          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">Importes</div>
        <div class="card-body">
          <div class="row g-3">

            <div class="col-md-4">
              <label class="form-label">Importe neto <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input type="number" class="form-control" v-model.number="v$.importeNeto.$model"
                  :class="{ 'is-invalid': v$.importeNeto.$dirty && v$.importeNeto.$invalid }" min="0" />
              </div>
            </div>

            <div class="col-md-4">
              <label class="form-label">Impuesto <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input type="number" class="form-control" v-model.number="v$.impuesto.$model"
                  :class="{ 'is-invalid': v$.impuesto.$dirty && v$.impuesto.$invalid }" min="0" />
              </div>
            </div>

            <div class="col-md-4">
              <label class="form-label">Total <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text">$</span>
                <input type="number" class="form-control" v-model.number="v$.total.$model"
                  :class="{ 'is-invalid': v$.total.$dirty && v$.total.$invalid }" min="0" />
              </div>
            </div>

          </div>
        </div>
      </div>

      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="previousState()">Cancelar</button>
        <button type="submit" class="btn btn-primary" :disabled="v$.$invalid || isSaving">
          <span v-if="isSaving" class="spinner-border spinner-border-sm me-1" />
          {{ comprobante.id ? 'Guardar cambios' : 'Crear comprobante' }}
        </button>
      </div>

    </form>
  </div>
</template>

<script lang="ts" src="./comprobante-update.component.ts"></script>
