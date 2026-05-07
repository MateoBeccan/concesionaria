<template>
  <div class="container py-4" style="max-width: 760px">
    <div class="page-header mb-3">
      <div>
        <h1 class="page-title mb-1">{{ comprobante.id ? 'Comprobante emitido' : 'Emitir comprobante' }}</h1>
        <p class="page-subtitle mb-0">Genera el comprobante en un paso, sin carga manual de datos técnicos.</p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="previousState()">Volver</button>
    </div>

    <div v-if="comprobante.id" class="card">
      <div class="card-body">
        <div class="fw-semibold mb-2">Este comprobante ya fue generado</div>
        <div>Número: <strong>{{ comprobante.numeroComprobante }}</strong></div>
        <div>Estado: <strong>{{ comprobante.estado }}</strong></div>
        <div class="mt-3">
          <button class="btn btn-outline-secondary" @click="previousState()">Volver al listado</button>
        </div>
      </div>
    </div>

    <form v-else @submit.prevent="save()" novalidate>
      <div class="card mb-3">
        <div class="card-header">Venta</div>
        <div class="card-body">
          <label class="form-label">Selecciona una venta</label>
          <select
            class="form-select"
            v-model="v$.venta.$model"
            :class="{ 'is-invalid': v$.venta.$dirty && v$.venta.$invalid }"
          >
            <option :value="null">Seleccionar venta</option>
            <option v-for="v in ventas" :key="v.id" :value="comprobante.venta?.id === v.id ? comprobante.venta : v">
              {{ ventaLabel(v) }} - {{ formatFecha(v.fecha) }} - ${{ formatMoneda(v.total) }}
            </option>
          </select>
          <div class="invalid-feedback">
            <span v-for="e of v$.venta.$errors" :key="e.$uid">{{ e.$message }}</span>
          </div>
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-header">Tipo de comprobante</div>
        <div class="card-body">
          <label class="form-label">Selecciona tipo</label>
          <select
            class="form-select"
            v-model="v$.tipoComprobante.$model"
            :class="{ 'is-invalid': v$.tipoComprobante.$dirty && v$.tipoComprobante.$invalid }"
          >
            <option :value="null">Seleccionar tipo</option>
            <option
              v-for="t in tipoComprobantes"
              :key="t.id"
              :value="comprobante.tipoComprobante?.id === t.id ? comprobante.tipoComprobante : t"
            >
              {{ t.codigo }} - {{ t.descripcion }}
            </option>
          </select>
          <div class="invalid-feedback">
            <span v-for="e of v$.tipoComprobante.$errors" :key="e.$uid">{{ e.$message }}</span>
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="previousState()">Cancelar</button>
        <button type="submit" class="btn btn-primary" :disabled="isSaving">
          <span v-if="isSaving" class="spinner-border spinner-border-sm me-1" />
          Emitir comprobante
        </button>
      </div>
    </form>
  </div>
</template>

<script lang="ts" src="./comprobante-update.component.ts"></script>
