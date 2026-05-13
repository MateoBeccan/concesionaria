<template>
  <div class="container-fluid px-0" v-if="pago">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-start gap-3">
        <div>
          <h1 class="h4 mb-1">Pago #{{ pago.id }}</h1>
          <p class="text-muted mb-0">
            {{ formatDateLong(pago.fecha) || '-' }}
            <span class="mx-2">•</span>
            Usuario: {{ pago.usuarioRegistro || '-' }}
          </p>
        </div>
        <span class="badge rounded-pill px-3 py-2" :class="estadoClass(pago.estado)">{{ estadoLabel(pago.estado) }}</span>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-12 col-lg-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Datos del pago</h2>
            <dl class="row mb-0">
              <dt class="col-5">Método</dt>
              <dd class="col-7">{{ pago.metodoPago?.descripcion ?? pago.metodoPago?.codigo ?? '-' }}</dd>
              <dt class="col-5">Moneda</dt>
              <dd class="col-7">{{ pago.moneda?.simbolo ?? '' }} {{ pago.moneda?.codigo ?? '-' }}</dd>
              <dt class="col-5">Referencia</dt>
              <dd class="col-7">{{ pago.referencia || '-' }}</dd>
              <dt class="col-5">N° operación</dt>
              <dd class="col-7">{{ pago.numeroOperacion || '-' }}</dd>
              <dt class="col-5">Entidad/Banco</dt>
              <dd class="col-7">{{ pago.bancoEntidad || '-' }}</dd>
              <dt class="col-5">Comprobante externo</dt>
              <dd class="col-7">{{ pago.comprobanteExterno || '-' }}</dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Estado financiero</h2>
            <dl class="row mb-0">
              <dt class="col-5">Monto original</dt>
              <dd class="col-7 fw-semibold">{{ formatMoney(pago.monto, pago.moneda?.codigo ?? 'ARS') }}</dd>
              <dt class="col-5">Cotización usada</dt>
              <dd class="col-7">{{ pago.cotizacionUsada ?? '-' }}</dd>
              <dt class="col-5">Monto aplicado ARS</dt>
              <dd class="col-7 fw-semibold">{{ formatMoney(pago.montoAplicadoVenta, 'ARS') }}</dd>
              <dt class="col-5">Venta asociada</dt>
              <dd class="col-7">
                <router-link v-if="pago.venta?.id" :to="{ name: 'VentaView', params: { ventaId: pago.venta.id } }">#{{ pago.venta.id }}</router-link>
                <span v-else>-</span>
              </dd>
              <dt class="col-5">Reserva asociada</dt>
              <dd class="col-7">
                <router-link v-if="pago.reserva?.id" :to="{ name: 'ReservaView', params: { reservaId: pago.reserva.id } }">#{{ pago.reserva.id }}</router-link>
                <span v-else>-</span>
              </dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="col-12">
        <div class="card border-0 shadow-sm">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Trazabilidad</h2>
            <dl class="row mb-0">
              <dt class="col-12 col-md-3">Creado</dt>
              <dd class="col-12 col-md-9">{{ pago.createdDate ? formatDateLong(pago.createdDate) : '-' }}</dd>
              <dt class="col-12 col-md-3">Última modificación</dt>
              <dd class="col-12 col-md-9">{{ pago.lastModifiedDate ? formatDateLong(pago.lastModifiedDate) : '-' }}</dd>
              <dt class="col-12 col-md-3">Motivo anulación</dt>
              <dd class="col-12 col-md-9">{{ pago.motivoAnulacion || '-' }}</dd>
              <dt class="col-12 col-md-3">Usuario anulación</dt>
              <dd class="col-12 col-md-9">{{ pago.usuarioAnulacion || '-' }}</dd>
              <dt class="col-12 col-md-3">Fecha anulación</dt>
              <dd class="col-12 col-md-9">{{ pago.fechaAnulacion ? formatDateLong(pago.fechaAnulacion) : '-' }}</dd>
            </dl>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-3">
      <button type="button" @click.prevent="previousState()" class="btn btn-light">
        <font-awesome-icon icon="arrow-left" class="me-2" />
        Volver
      </button>
    </div>
  </div>
</template>

<script lang="ts" src="./pago-details.component.ts"></script>
