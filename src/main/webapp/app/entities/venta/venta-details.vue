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
            <span class="badge" :class="badgeEstado(venta.estadoVenta ?? venta.estado)">
              {{ labelEstado(venta.estadoVenta ?? venta.estado) }}
            </span>
            <span v-if="venta.moneda" class="badge border bg-light text-dark">
              {{ venta.moneda.simbolo ?? '' }} {{ venta.moneda.codigo }}
            </span>
          </div>
        </div>
        <div class="d-flex flex-wrap gap-2">
          <button class="btn btn-sm btn-outline-secondary" @click="previousState()">Volver</button>
          <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-primary"
            >Editar venta</router-link
          >
        </div>
      </div>

      <div class="card mb-3 border-0" style="background: var(--color-primary-light)">
        <div class="card-body py-3">
          <div class="row g-3 align-items-center">
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing: 0.05em">Total</div>
              <div class="fw-bold" style="font-size: 1.5rem; color: var(--color-primary)">$ {{ formatPrecio(venta.total) }}</div>
            </div>
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing: 0.05em">Pagado</div>
              <div class="fw-bold text-success" style="font-size: 1.3rem">$ {{ formatPrecio(venta.totalPagado) }}</div>
            </div>
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing: 0.05em">Saldo</div>
              <div class="fw-bold" :class="(venta.saldo ?? 0) > 0 ? 'text-danger' : 'text-success'" style="font-size: 1.3rem">
                $ {{ formatPrecio(venta.saldo) }}
              </div>
            </div>
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing: 0.05em">Estado</div>
              <span class="badge fs-6" :class="badgeEstado(venta.estadoVenta ?? venta.estado)">
                {{ labelEstado(venta.estadoVenta ?? venta.estado) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="row g-3 mb-3">
        <div class="col-md-6">
          <div class="card h-100">
            <div class="card-header d-flex align-items-center gap-2">Cliente</div>
            <div class="card-body">
              <div v-if="venta.cliente">
                <p class="mb-2 fw-semibold" style="font-size: 1rem">{{ venta.cliente.nombre }} {{ venta.cliente.apellido }}</p>
                <dl class="detail-list">
                  <dt>Documento</dt>
                  <dd>{{ venta.cliente.nroDocumento ?? '—' }}</dd>
                  <dt>Email</dt>
                  <dd>{{ venta.cliente.email ?? '—' }}</dd>
                  <dt>Telefono</dt>
                  <dd>{{ venta.cliente.telefono ?? '—' }}</dd>
                  <dt>Condicion IVA</dt>
                  <dd>{{ venta.cliente.condicionIva?.descripcion ?? venta.cliente.condicionIva?.codigo ?? '—' }}</dd>
                </dl>
                <router-link
                  :to="{ name: 'ClienteView', params: { clienteId: venta.cliente.id } }"
                  class="btn btn-sm btn-outline-secondary mt-3"
                >
                  Ver cliente
                </router-link>
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
                <dt>Importe neto</dt>
                <dd>$ {{ formatPrecio(venta.importeNeto) }}</dd>
                <dt>% Impuesto</dt>
                <dd>{{ venta.porcentajeImpuesto ? `${venta.porcentajeImpuesto}%` : '—' }}</dd>
                <dt>Impuesto</dt>
                <dd>$ {{ formatPrecio(venta.impuesto) }}</dd>
                <dt>Total</dt>
                <dd class="fw-semibold" style="color: var(--color-primary)">$ {{ formatPrecio(venta.total) }}</dd>
                <dt>Cotizacion</dt>
                <dd>{{ venta.cotizacion ?? '—' }}</dd>
                <dt>Moneda</dt>
                <dd>{{ venta.moneda?.simbolo ?? '' }} {{ venta.moneda?.codigo ?? '—' }}</dd>
                <dt>Registrado por</dt>
                <dd>{{ venta.user?.login ?? '—' }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>Vehiculos de la venta</span>
          <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-outline-primary">
            Gestionar venta
          </router-link>
        </div>

        <div v-if="loadingDetalles" class="card-body">
          <div v-for="i in 2" :key="i" class="placeholder-glow mb-2 d-flex gap-3">
            <span class="placeholder col-2 rounded" /><span class="placeholder col-4 rounded" /><span class="placeholder col-2 rounded" />
          </div>
        </div>

        <div v-else-if="detalles.length === 0" class="card-body py-4 text-center">
          <p class="mb-2 text-muted">No hay vehiculos asociados a esta venta</p>
          <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-primary"
            >Editar venta</router-link
          >
        </div>

        <div v-else class="table-responsive">
          <table class="table mb-0">
            <thead>
              <tr>
                <th>Patente</th>
                <th>Vehiculo</th>
                <th>Estado</th>
                <th class="text-end">Precio unitario</th>
                <th class="text-end">Subtotal</th>
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="detalle in detalles" :key="detalle.id">
                <td class="fw-semibold">{{ detalle.vehiculo?.patente ?? '—' }}</td>
                <td>
                  {{ detalle.vehiculo?.version?.modelo?.marca?.nombre ?? '' }}
                  {{ detalle.vehiculo?.version?.modelo?.nombre ?? '' }}
                  {{ detalle.vehiculo?.version?.nombre ?? '' }}
                </td>
                <td>
                  <span class="badge" :class="detalle.vehiculo?.estado === 'NUEVO' ? 'bg-success' : 'bg-secondary'">
                    {{ detalle.vehiculo?.estado ?? '—' }}
                  </span>
                </td>
                <td class="text-end">$ {{ formatPrecio(detalle.precioUnitario) }}</td>
                <td class="text-end fw-semibold" style="color: var(--color-primary)">$ {{ formatPrecio(detalle.subtotal) }}</td>
                <td class="text-end">
                  <router-link
                    :to="{ name: 'VehiculoView', params: { vehiculoId: detalle.vehiculo?.id } }"
                    class="btn btn-sm btn-outline-secondary"
                  >
                    Ver
                  </router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>Pagos</span>
          <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-success"
            >Gestionar pagos</router-link
          >
        </div>

        <div v-if="loadingPagos" class="card-body">
          <div v-for="i in 2" :key="i" class="placeholder-glow mb-2 d-flex gap-3">
            <span class="placeholder col-2 rounded" /><span class="placeholder col-3 rounded" /><span class="placeholder col-2 rounded" />
          </div>
        </div>

        <div v-else-if="pagos.length === 0" class="card-body py-4 text-center">
          <p class="mb-2 text-muted">No hay pagos registrados</p>
          <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-success"
            >Editar venta</router-link
          >
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
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="pago in pagos" :key="pago.id">
                <td>{{ formatFecha(pago.fecha) }}</td>
                <td>{{ pago.metodoPago?.descripcion ?? pago.metodoPago?.codigo ?? '—' }}</td>
                <td>{{ pago.moneda?.simbolo ?? '' }} {{ pago.moneda?.codigo ?? '—' }}</td>
                <td class="text-muted small">{{ pago.referencia ?? '—' }}</td>
                <td class="text-end fw-semibold text-success">$ {{ formatPrecio(pago.monto) }}</td>
                <td class="text-end">
                  <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-outline-secondary">
                    Editar venta
                  </router-link>
                </td>
              </tr>
            </tbody>
            <tfoot class="table-light">
              <tr>
                <td colspan="4" class="fw-semibold text-end">Total pagado:</td>
                <td class="text-end fw-bold text-success">$ {{ formatPrecio(totalPagado) }}</td>
                <td />
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>Comprobantes</span>
          <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-outline-primary">
            Editar venta
          </router-link>
        </div>

        <div v-if="loadingComprobantes" class="card-body">
          <div class="placeholder-glow d-flex gap-3">
            <span class="placeholder col-2 rounded" /><span class="placeholder col-3 rounded" />
          </div>
        </div>

        <div v-else-if="comprobantes.length === 0" class="card-body py-3 text-center">
          <p class="mb-0 small text-muted">Sin comprobantes emitidos</p>
        </div>

        <div v-else class="table-responsive">
          <table class="table mb-0">
            <thead>
              <tr>
                <th>Numero</th>
                <th>Tipo</th>
                <th>Fecha emision</th>
                <th>Moneda</th>
                <th class="text-end">Total</th>
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="comprobante in comprobantes" :key="comprobante.id">
                <td class="fw-semibold">{{ comprobante.numeroComprobante }}</td>
                <td>{{ comprobante.tipoComprobante?.codigo ?? '—' }} - {{ comprobante.tipoComprobante?.descripcion ?? '' }}</td>
                <td>{{ formatFecha(comprobante.fechaEmision) }}</td>
                <td>{{ comprobante.moneda?.simbolo ?? '' }} {{ comprobante.moneda?.codigo ?? '—' }}</td>
                <td class="text-end fw-semibold">$ {{ formatPrecio(comprobante.total) }}</td>
                <td class="text-end">
                  <router-link
                    :to="{ name: 'ComprobanteView', params: { comprobanteId: comprobante.id } }"
                    class="btn btn-sm btn-outline-secondary"
                  >
                    Ver
                  </router-link>
                </td>
              </tr>
            </tbody>
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
