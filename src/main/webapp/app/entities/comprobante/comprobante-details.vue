<template>
  <div class="container py-4" style="max-width: 900px" v-if="comprobante">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="mb-0">Comprobante #{{ comprobante.id }}</h2>
      <div class="d-flex gap-2">
        <button type="button" @click="descargarPdf" class="btn btn-primary btn-sm">Descargar PDF</button>
        <button type="button" @click.prevent="previousState()" class="btn btn-outline-secondary btn-sm">Volver</button>
      </div>
    </div>
    <div class="card">
      <div class="card-body">
        <dl class="row mb-0">
          <dt class="col-sm-4">Número</dt>
          <dd class="col-sm-8">{{ comprobante.numeroComprobante }}</dd>
          <dt class="col-sm-4">Tipo</dt>
          <dd class="col-sm-8">{{ comprobante.tipoComprobante?.codigo ?? '-' }}</dd>
          <dt class="col-sm-4">Estado</dt>
          <dd class="col-sm-8">{{ comprobante.estado ?? '-' }}</dd>
          <dt class="col-sm-4">Fecha emisión</dt>
          <dd class="col-sm-8">{{ comprobante.fechaEmision ? formatDateLong(comprobante.fechaEmision) : '-' }}</dd>
          <dt class="col-sm-4">Importe neto</dt>
          <dd class="col-sm-8">{{ comprobante.importeNeto }}</dd>
          <dt class="col-sm-4">Impuesto</dt>
          <dd class="col-sm-8">{{ comprobante.impuesto }}</dd>
          <dt class="col-sm-4">Total</dt>
          <dd class="col-sm-8">{{ comprobante.total }}</dd>
          <dt class="col-sm-4">Moneda</dt>
          <dd class="col-sm-8">{{ comprobante.moneda?.simbolo ?? '' }} {{ comprobante.moneda?.codigo ?? '-' }}</dd>
          <dt class="col-sm-4">Venta asociada</dt>
          <dd class="col-sm-8">
            <router-link v-if="comprobante.venta?.id" :to="{ name: 'VentaView', params: { ventaId: comprobante.venta.id } }">
              {{ comprobante.venta.id }}
            </router-link>
          </dd>
          <dt class="col-sm-4">Usuario emisor</dt>
          <dd class="col-sm-8">{{ comprobante.usuarioEmision ?? comprobante.createdBy ?? '-' }}</dd>
          <dt class="col-sm-4">Motivo anulación</dt>
          <dd class="col-sm-8">{{ comprobante.motivoAnulacion ?? '-' }}</dd>
          <dt class="col-sm-4">Usuario anulación</dt>
          <dd class="col-sm-8">{{ comprobante.usuarioAnulacion ?? '-' }}</dd>
        </dl>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./comprobante-details.component.ts"></script>
