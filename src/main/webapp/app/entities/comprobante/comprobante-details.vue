<template>
  <div class="container py-4 comprobante-details" v-if="comprobante">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
          <div class="text-uppercase small text-muted fw-semibold">Detalle financiero</div>
          <h1 class="h4 mb-1">Comprobante #{{ comprobante.id }}</h1>
          <div class="d-flex gap-2 align-items-center">
            <span class="badge" :class="estadoClass(comprobante.estado)">{{ estadoLabel(comprobante.estado) }}</span>
            <span class="text-muted small">{{ comprobante.fechaEmision ? formatDateLong(comprobante.fechaEmision) : '-' }}</span>
          </div>
        </div>
        <div class="d-flex gap-2">
          <button type="button" @click="descargarPdf" class="btn btn-primary btn-sm">Descargar PDF</button>
          <button type="button" @click.prevent="previousState()" class="btn btn-outline-secondary btn-sm">Volver</button>
        </div>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-12 col-lg-7">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white fw-semibold">Datos del comprobante</div>
          <div class="card-body">
            <dl class="row mb-0">
              <dt class="col-sm-4">Número</dt>
              <dd class="col-sm-8 fw-semibold">{{ comprobante.numeroComprobante ?? '-' }}</dd>
              <dt class="col-sm-4">Tipo</dt>
              <dd class="col-sm-8">{{ comprobante.tipoComprobante?.codigo ?? '-' }}</dd>
              <dt class="col-sm-4">Venta asociada</dt>
              <dd class="col-sm-8">
                <router-link v-if="comprobante.venta?.id" :to="{ name: 'VentaView', params: { ventaId: comprobante.venta.id } }">#{{ comprobante.venta.id }}</router-link>
                <span v-else>-</span>
              </dd>
              <dt class="col-sm-4">Moneda</dt>
              <dd class="col-sm-8">{{ comprobante.moneda?.simbolo ?? '' }} {{ comprobante.moneda?.codigo ?? '-' }}</dd>
              <dt class="col-sm-4">Usuario emisor</dt>
              <dd class="col-sm-8">{{ comprobante.usuarioEmision ?? comprobante.createdBy ?? '-' }}</dd>
              <dt class="col-sm-4">Fecha emisión</dt>
              <dd class="col-sm-8">{{ comprobante.fechaEmision ? formatDateLong(comprobante.fechaEmision) : '-' }}</dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-5">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white fw-semibold">Resumen económico</div>
          <div class="card-body">
            <div class="summary-row">
              <span>Importe neto</span>
              <strong>{{ formatMoney(comprobante.importeNeto) }}</strong>
            </div>
            <div class="summary-row">
              <span>Impuesto</span>
              <strong>{{ formatMoney(comprobante.impuesto) }}</strong>
            </div>
            <div class="summary-row total">
              <span>Total</span>
              <strong>{{ formatMoney(comprobante.total) }}</strong>
            </div>
            <hr />
            <div class="small text-muted">
              <div><strong>Motivo anulación:</strong> {{ comprobante.motivoAnulacion ?? '-' }}</div>
              <div><strong>Usuario anulación:</strong> {{ comprobante.usuarioAnulacion ?? '-' }}</div>
              <div><strong>Fecha anulación:</strong> {{ comprobante.fechaAnulacion ? formatDateLong(comprobante.fechaAnulacion) : '-' }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./comprobante-details.component.ts"></script>

<style scoped>
.comprobante-details {
  max-width: 1100px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.summary-row.total {
  font-size: 1.05rem;
  color: var(--color-primary);
}
</style>
