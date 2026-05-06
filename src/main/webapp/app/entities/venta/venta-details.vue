<template>
  <div class="container py-4" style="max-width: 900px">
    <div v-if="!venta.id" class="py-5 text-center">
      <div class="spinner-border text-primary" />
    </div>

    <template v-else>
      <div class="page-header">
        <div>
          <h1 class="page-title mb-0">Venta #{{ venta.id }}</h1>
          <div class="mt-1 d-flex align-items-center gap-2">
            <span class="text-muted small">{{ formatFecha(venta.fecha) }}</span>
            <span class="badge" :class="badgeEstado(venta.estado)">{{ labelEstado(venta.estado) }}</span>
            <span v-if="ventaMonedaDisplay" class="badge border bg-light text-dark">
              {{ ventaMonedaDisplay.simbolo ?? '' }} {{ ventaMonedaDisplay.codigo }}
            </span>
          </div>
        </div>
        <div class="d-flex flex-wrap gap-2">
          <button class="btn btn-sm btn-outline-secondary" @click="previousState()">Volver</button>
          <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-primary">Editar venta</router-link>
        </div>
      </div>

      <div class="card mb-3 border-0" style="background: var(--color-primary-light)">
        <div class="card-body py-3">
          <div class="row g-3 align-items-center">
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing: 0.05em">Total</div>
              <div class="fw-bold" style="font-size: 1.5rem; color: var(--color-primary)">
                {{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.total) }} {{ ventaMonedaDisplay?.codigo ?? '' }}
              </div>
            </div>
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing: 0.05em">Pagado</div>
              <div class="fw-bold text-success" style="font-size: 1.3rem">
                {{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.totalPagado) }} {{ ventaMonedaDisplay?.codigo ?? '' }}
              </div>
            </div>
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing: 0.05em">Saldo</div>
              <div class="fw-bold" :class="(venta.saldo ?? 0) > 0 ? 'text-danger' : 'text-success'" style="font-size: 1.3rem">
                {{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.saldo) }} {{ ventaMonedaDisplay?.codigo ?? '' }}
              </div>
            </div>
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing: 0.05em">Estado</div>
              <span class="badge fs-6" :class="badgeEstado(venta.estado)">{{ labelEstado(venta.estado) }}</span>
            </div>
          </div>
        </div>
      </div>

      <OperationalTraceCard
        class="mb-3"
        title="Trazabilidad de la venta"
        :status="ventaStatusLabel"
        :created-by="traceCreatedBy"
        :created-at="venta.createdDate ? formatDateLong(venta.createdDate) : 'No disponible'"
        :updated-by="traceUpdatedBy"
        :updated-at="venta.lastModifiedDate ? formatDateLong(venta.lastModifiedDate) : 'No disponible'"
        :last-action="traceLastAction"
        :last-action-at="traceLastActionDate"
      />

      <div class="row g-3 mb-3">
        <div class="col-md-6">
          <div class="card h-100">
            <div class="card-header d-flex align-items-center gap-2">Cliente</div>
            <div class="card-body">
              <div v-if="venta.cliente">
                <p class="mb-2 fw-semibold" style="font-size: 1rem">{{ venta.cliente.nombre }} {{ venta.cliente.apellido }}</p>
                <dl class="detail-list">
                  <dt>Documento</dt>
                  <dd>{{ venta.cliente.nroDocumento ?? '-' }}</dd>
                  <dt>Usuario registro</dt>
                  <dd>{{ venta.user?.login ?? venta.createdBy ?? '-' }}</dd>
                  <dt>Email</dt>
                  <dd>{{ venta.cliente.email ?? '-' }}</dd>
                  <dt>Telefono</dt>
                  <dd>{{ venta.cliente.telefono ?? '-' }}</dd>
                  <dt>Condicion IVA</dt>
                  <dd>{{ venta.cliente.condicionIva?.descripcion ?? venta.cliente.condicionIva?.codigo ?? '-' }}</dd>
                </dl>
              </div>
              <p v-else class="mb-0 text-muted">Sin cliente asignado</p>
            </div>
          </div>
        </div>

        <div class="col-md-6">
          <div class="card h-100">
            <div class="card-header d-flex align-items-center gap-2">Detalle financiero</div>
            <div class="card-body">
              <dl class="detail-list">
                <dt>Precio base vehiculo</dt>
                <dd>
                  {{ venta.monedaVehiculo?.simbolo ?? venta.vehiculo?.moneda?.simbolo ?? '$' }}
                  {{ formatPrecio(venta.precioBaseVehiculo ?? venta.vehiculo?.precio) }}
                  {{ venta.monedaVehiculo?.codigo ?? venta.vehiculo?.moneda?.codigo ?? '' }}
                </dd>
                <dt>Moneda venta</dt>
                <dd>{{ ventaMonedaDisplay?.simbolo ?? '' }} {{ ventaMonedaDisplay?.codigo ?? '-' }}</dd>
                <dt>Cotizacion aplicada</dt>
                <dd>{{ formatCotizacion(venta.cotizacion) }}</dd>
                <dt>Importe convertido</dt>
                <dd>{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.importeConvertido ?? venta.importeNeto) }} {{ ventaMonedaDisplay?.codigo ?? '' }}</dd>
                <dt>% Impuesto</dt>
                <dd>{{ venta.porcentajeImpuesto ? `${venta.porcentajeImpuesto}%` : '-' }}</dd>
                <dt>Total</dt>
                <dd class="fw-semibold" style="color: var(--color-primary)">{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.total) }} {{ ventaMonedaDisplay?.codigo ?? '' }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>Vehiculo de la venta</span>
          <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-outline-primary">Gestionar venta</router-link>
        </div>
        <div v-if="detalles.length === 0" class="card-body py-4 text-center">
          <p class="mb-0 text-muted">No hay vehiculo asociado a esta venta.</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table mb-0">
            <thead>
              <tr>
                <th>Patente</th>
                <th>Vehiculo</th>
                <th>Estado</th>
                <th class="text-end">Precio base</th>
                <th class="text-end">Importe en venta</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="detalle in detalles" :key="detalle.id">
                <td class="fw-semibold">{{ detalle.vehiculo?.patente ?? '-' }}</td>
                <td>
                  {{ detalle.vehiculo?.version?.modelo?.marca?.nombre ?? '' }}
                  {{ detalle.vehiculo?.version?.modelo?.nombre ?? '' }}
                  {{ detalle.vehiculo?.version?.nombre ?? '' }}
                </td>
                <td>
                  <span class="badge" :class="detalle.vehiculo?.estadoInventario === 'DISPONIBLE' ? 'bg-success' : detalle.vehiculo?.estadoInventario === 'RESERVADO' ? 'bg-warning text-dark' : detalle.vehiculo?.estadoInventario === 'VENDIDO' ? 'bg-danger' : 'bg-secondary'">
                    {{ detalle.vehiculo?.estadoInventario ?? detalle.vehiculo?.estado ?? '-' }}
                  </span>
                </td>
                <td class="text-end">
                  {{ venta.monedaVehiculo?.simbolo ?? detalle.vehiculo?.moneda?.simbolo ?? '$' }} {{ formatPrecio(detalle.precioUnitario) }}
                  {{ venta.monedaVehiculo?.codigo ?? detalle.vehiculo?.moneda?.codigo ?? '' }}
                </td>
                <td class="text-end fw-semibold" style="color: var(--color-primary)">
                  {{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(detalle.subtotal) }} {{ ventaMonedaDisplay?.codigo ?? '' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">Pagos</div>
        <div v-if="loadingPagos" class="card-body">
          <div v-for="i in 2" :key="i" class="placeholder-glow mb-2 d-flex gap-3">
            <span class="placeholder col-2 rounded" /><span class="placeholder col-3 rounded" /><span class="placeholder col-2 rounded" />
          </div>
        </div>
        <div v-else-if="pagos.length === 0" class="card-body py-4 text-center">
          <p class="mb-0 text-muted">No hay pagos registrados.</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table mb-0">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Metodo</th>
                <th>Moneda</th>
                <th>Referencia</th>
                <th class="text-end">Monto</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="pago in pagos" :key="pago.id">
                <td>{{ formatFecha(pago.fecha) }}</td>
                <td>{{ pago.metodoPago?.descripcion ?? pago.metodoPago?.codigo ?? '-' }}</td>
                <td>{{ pago.moneda?.simbolo ?? '' }} {{ pago.moneda?.codigo ?? '-' }}</td>
                <td class="text-muted small">{{ pago.referencia ?? '-' }}</td>
                <td class="text-end fw-semibold text-success">
                  {{ pago.moneda?.simbolo ?? ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(pago.monto) }} {{ pago.moneda?.codigo ?? ventaMonedaDisplay?.codigo ?? '' }}
                </td>
              </tr>
            </tbody>
            <tfoot class="table-light">
              <tr>
                <td colspan="4" class="fw-semibold text-end">Total pagado:</td>
                <td class="text-end fw-bold text-success">
                  {{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(totalPagado) }} {{ ventaMonedaDisplay?.codigo ?? '' }}
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <div v-if="venta.observaciones" class="card">
        <div class="card-header">Observaciones</div>
        <div class="card-body">
          <p class="mb-0" style="white-space: pre-wrap">{{ venta.observaciones }}</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script lang="ts" src="./venta-details.component.ts"></script>

<style scoped>
.detail-list {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.4rem 1rem;
  margin: 0;
}

.detail-list dt {
  align-self: center;
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.detail-list dd {
  align-self: center;
  margin: 0;
  font-size: 0.88rem;
  color: var(--color-text);
}
</style>
