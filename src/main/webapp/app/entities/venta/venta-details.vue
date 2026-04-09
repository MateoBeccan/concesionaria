<template>
  <div class="container py-4" style="max-width:900px">

    <div v-if="!venta.id" class="text-center py-5">
      <div class="spinner-border text-primary" />
    </div>

    <template v-else>

      <!-- PAGE HEADER -->
      <div class="page-header">
        <div>
          <h1 class="page-title mb-0">Venta #{{ venta.id }}</h1>
          <div class="d-flex align-items-center gap-2 mt-1">
            <span class="text-muted small">{{ formatFecha(venta.fecha) }}</span>
            <span class="badge" :class="badgeEstado(venta.estadoVenta ?? venta.estado)">
              {{ labelEstado(venta.estadoVenta ?? venta.estado) }}
            </span>
            <span v-if="venta.moneda" class="badge bg-light text-dark border">
              {{ venta.moneda.simbolo ?? '' }} {{ venta.moneda.codigo }}
            </span>
          </div>
        </div>
        <div class="d-flex gap-2 flex-wrap">
          <button class="btn btn-sm btn-outline-secondary" @click="previousState()">← Volver</button>
          <router-link :to="{ name: 'PagoCreate', query: { ventaId: venta.id } }" class="btn btn-sm btn-success">
            + Registrar pago
          </router-link>
          <router-link :to="{ name: 'ComprobanteCreate', query: { ventaId: venta.id } }" class="btn btn-sm btn-outline-primary">
            + Comprobante
          </router-link>
          <router-link :to="{ name: 'VentaEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-primary">
            ✏️ Editar
          </router-link>
        </div>
      </div>

      <!-- RESUMEN FINANCIERO -->
      <div class="card mb-3 border-0" style="background:var(--color-primary-light)">
        <div class="card-body py-3">
          <div class="row g-3 align-items-center">
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing:.05em">Total</div>
              <div class="fw-bold" style="font-size:1.5rem;color:var(--color-primary)">$ {{ formatPrecio(venta.total) }}</div>
            </div>
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing:.05em">Pagado</div>
              <div class="fw-bold text-success" style="font-size:1.3rem">$ {{ formatPrecio(venta.totalPagado) }}</div>
            </div>
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing:.05em">Saldo</div>
              <div class="fw-bold" :class="(venta.saldo ?? 0) > 0 ? 'text-danger' : 'text-success'" style="font-size:1.3rem">
                $ {{ formatPrecio(venta.saldo) }}
              </div>
            </div>
            <div class="col-sm-3 text-center">
              <div class="text-muted small fw-semibold text-uppercase" style="letter-spacing:.05em">Estado</div>
              <span class="badge fs-6" :class="badgeEstado(venta.estadoVenta ?? venta.estado)">
                {{ labelEstado(venta.estadoVenta ?? venta.estado) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="row g-3 mb-3">

        <!-- CLIENTE -->
        <div class="col-md-6">
          <div class="card h-100">
            <div class="card-header d-flex align-items-center gap-2">👤 Cliente</div>
            <div class="card-body">
              <div v-if="venta.cliente">
                <p class="fw-semibold mb-2" style="font-size:1rem">
                  {{ venta.cliente.nombre }} {{ venta.cliente.apellido }}
                </p>
                <dl class="detail-list">
                  <dt>Documento</dt>
                  <dd>{{ venta.cliente.nroDocumento ?? '—' }}</dd>
                  <dt>Email</dt>
                  <dd>{{ venta.cliente.email ?? '—' }}</dd>
                  <dt>Teléfono</dt>
                  <dd>{{ venta.cliente.telefono ?? '—' }}</dd>
                  <dt>Condición IVA</dt>
                  <dd>{{ venta.cliente.condicionIva?.descripcion ?? venta.cliente.condicionIva?.codigo ?? '—' }}</dd>
                </dl>
                <router-link :to="{ name: 'ClienteView', params: { clienteId: venta.cliente.id } }" class="btn btn-sm btn-outline-secondary mt-3">
                  Ver cliente
                </router-link>
              </div>
              <p v-else class="text-muted mb-0">Sin cliente asignado</p>
            </div>
          </div>
        </div>

        <!-- DETALLE FINANCIERO -->
        <div class="col-md-6">
          <div class="card h-100">
            <div class="card-header d-flex align-items-center gap-2">💰 Detalle financiero</div>
            <div class="card-body">
              <dl class="detail-list">
                <dt>Importe neto</dt>
                <dd>$ {{ formatPrecio(venta.importeNeto) }}</dd>
                <dt>% Impuesto</dt>
                <dd>{{ venta.porcentajeImpuesto ? `${venta.porcentajeImpuesto}%` : '—' }}</dd>
                <dt>Impuesto</dt>
                <dd>$ {{ formatPrecio(venta.impuesto) }}</dd>
                <dt>Total</dt>
                <dd class="fw-semibold" style="color:var(--color-primary)">$ {{ formatPrecio(venta.total) }}</dd>
                <dt>Cotización</dt>
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

      <!-- VEHÍCULOS (DetalleVenta) -->
      <div class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>🚗 Vehículos de la venta</span>
          <router-link :to="{ name: 'DetalleVentaCreate' }" class="btn btn-sm btn-outline-primary">
            + Agregar vehículo
          </router-link>
        </div>

        <div v-if="loadingDetalles" class="card-body">
          <div v-for="i in 2" :key="i" class="placeholder-glow d-flex gap-3 mb-2">
            <span class="placeholder col-2 rounded" /><span class="placeholder col-4 rounded" /><span class="placeholder col-2 rounded" />
          </div>
        </div>

        <div v-else-if="detalles.length === 0" class="card-body text-center py-4">
          <p class="text-muted mb-2">No hay vehículos asociados a esta venta</p>
          <router-link :to="{ name: 'DetalleVentaCreate' }" class="btn btn-sm btn-primary">
            + Agregar vehículo
          </router-link>
        </div>

        <div v-else class="table-responsive">
          <table class="table mb-0">
            <thead>
              <tr>
                <th>Patente</th>
                <th>Vehículo</th>
                <th>Estado</th>
                <th class="text-end">Precio unitario</th>
                <th class="text-end">Subtotal</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="d in detalles" :key="d.id">
                <td class="fw-semibold">{{ d.vehiculo?.patente ?? '—' }}</td>
                <td>
                  {{ d.vehiculo?.version?.modelo?.marca?.nombre ?? '' }}
                  {{ d.vehiculo?.version?.modelo?.nombre ?? '' }}
                  {{ d.vehiculo?.version?.nombre ?? '' }}
                </td>
                <td>
                  <span class="badge" :class="d.vehiculo?.estado === 'NUEVO' ? 'bg-success' : 'bg-secondary'">
                    {{ d.vehiculo?.estado ?? '—' }}
                  </span>
                </td>
                <td class="text-end">$ {{ formatPrecio(d.precioUnitario) }}</td>
                <td class="text-end fw-semibold" style="color:var(--color-primary)">$ {{ formatPrecio(d.subtotal) }}</td>
                <td class="text-end">
                  <router-link :to="{ name: 'VehiculoView', params: { vehiculoId: d.vehiculo?.id } }" class="btn btn-sm btn-outline-secondary">
                    Ver
                  </router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- PAGOS -->
      <div class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>💳 Pagos</span>
          <router-link :to="{ name: 'PagoCreate', query: { ventaId: venta.id } }" class="btn btn-sm btn-success">
            + Registrar pago
          </router-link>
        </div>

        <div v-if="loadingPagos" class="card-body">
          <div v-for="i in 2" :key="i" class="placeholder-glow d-flex gap-3 mb-2">
            <span class="placeholder col-2 rounded" /><span class="placeholder col-3 rounded" /><span class="placeholder col-2 rounded" />
          </div>
        </div>

        <div v-else-if="pagos.length === 0" class="card-body text-center py-4">
          <p class="text-muted mb-2">No hay pagos registrados</p>
          <router-link :to="{ name: 'PagoCreate', query: { ventaId: venta.id } }" class="btn btn-sm btn-success">
            + Registrar primer pago
          </router-link>
        </div>

        <div v-else class="table-responsive">
          <table class="table mb-0">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Método</th>
                <th>Moneda</th>
                <th>Referencia</th>
                <th class="text-end">Monto</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in pagos" :key="p.id">
                <td>{{ formatFecha(p.fecha) }}</td>
                <td>{{ p.metodoPago?.descripcion ?? p.metodoPago?.codigo ?? '—' }}</td>
                <td>{{ p.moneda?.simbolo ?? '' }} {{ p.moneda?.codigo ?? '—' }}</td>
                <td class="text-muted small">{{ p.referencia ?? '—' }}</td>
                <td class="text-end fw-semibold text-success">$ {{ formatPrecio(p.monto) }}</td>
                <td class="text-end">
                  <router-link :to="{ name: 'PagoEdit', params: { pagoId: p.id } }" class="btn btn-sm btn-outline-secondary">
                    Editar
                  </router-link>
                </td>
              </tr>
            </tbody>
            <tfoot class="table-light">
              <tr>
                <td colspan="4" class="fw-semibold text-end">Total pagado:</td>
                <td class="text-end fw-bold text-success">$ {{ formatPrecio(totalPagado) }}</td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <!-- COMPROBANTES -->
      <div class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>🧾 Comprobantes</span>
          <router-link :to="{ name: 'ComprobanteCreate', query: { ventaId: venta.id } }" class="btn btn-sm btn-outline-primary">
            + Nuevo comprobante
          </router-link>
        </div>

        <div v-if="loadingComprobantes" class="card-body">
          <div class="placeholder-glow d-flex gap-3">
            <span class="placeholder col-2 rounded" /><span class="placeholder col-3 rounded" />
          </div>
        </div>

        <div v-else-if="comprobantes.length === 0" class="card-body text-center py-3">
          <p class="text-muted mb-0 small">Sin comprobantes emitidos</p>
        </div>

        <div v-else class="table-responsive">
          <table class="table mb-0">
            <thead>
              <tr>
                <th>Número</th>
                <th>Tipo</th>
                <th>Fecha emisión</th>
                <th>Moneda</th>
                <th class="text-end">Total</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in comprobantes" :key="c.id">
                <td class="fw-semibold">{{ c.numeroComprobante }}</td>
                <td>{{ c.tipoComprobante?.codigo ?? '—' }} — {{ c.tipoComprobante?.descripcion ?? '' }}</td>
                <td>{{ formatFecha(c.fechaEmision) }}</td>
                <td>{{ c.moneda?.simbolo ?? '' }} {{ c.moneda?.codigo ?? '—' }}</td>
                <td class="text-end fw-semibold">$ {{ formatPrecio(c.total) }}</td>
                <td class="text-end">
                  <router-link :to="{ name: 'ComprobanteView', params: { comprobanteId: c.id } }" class="btn btn-sm btn-outline-secondary">
                    Ver
                  </router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- OBSERVACIONES -->
      <div class="card" v-if="venta.observaciones">
        <div class="card-header">Observaciones</div>
        <div class="card-body">
          <p class="mb-0" style="white-space:pre-wrap">{{ venta.observaciones }}</p>
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
  gap: .4rem 1rem;
  margin: 0;
}
.detail-list dt {
  font-size: .78rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: .04em;
  align-self: center;
}
.detail-list dd {
  font-size: .88rem;
  color: var(--color-text);
  margin: 0;
  align-self: center;
}
</style>
