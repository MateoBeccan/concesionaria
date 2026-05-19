<template>
  <div class="venta-view container-fluid py-3 py-md-4">
    <div v-if="!venta.id" class="py-5 text-center">
      <div class="spinner-border text-primary" />
    </div>

    <template v-else>
      <div class="executive-header card border-0 shadow-sm mb-3">
        <div class="card-body d-flex flex-column flex-lg-row justify-content-between gap-3">
          <div>
            <div class="text-uppercase small text-muted fw-semibold mb-1">Detalle operativo</div>
            <h1 class="h3 mb-2">Venta #{{ venta.id }}</h1>
            <div class="d-flex flex-wrap gap-2 align-items-center">
              <span class="badge" :class="badgeEstado(venta.estado)">{{ labelEstado(venta.estado) }}</span>
              <span class="badge bg-light text-dark border">{{ formatFecha(venta.fecha) }}</span>
              <span class="badge bg-light text-dark border">Vendedor: {{ venta.user?.login ?? venta.createdBy ?? '-' }}</span>
              <span class="badge bg-light text-dark border">Cliente: {{ venta.cliente?.nombre }} {{ venta.cliente?.apellido }}</span>
            </div>
          </div>
          <div class="d-flex flex-wrap gap-2 align-self-start">
            <button class="btn btn-outline-secondary btn-sm" @click="previousState">Volver</button>
            <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-primary btn-sm">Gestionar venta</router-link>
          </div>
        </div>
      </div>

      <div class="card border-0 shadow-sm mb-3">
        <div class="card-body">
          <div class="row g-3 text-center">
            <div class="col-6 col-lg-3">
              <div class="metric-label">Subtotal</div>
              <div class="metric-value">{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.importeNeto) }}</div>
            </div>
            <div class="col-6 col-lg-3">
              <div class="metric-label">Total</div>
              <div class="metric-value text-primary">{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.total) }}</div>
            </div>
            <div class="col-6 col-lg-3">
              <div class="metric-label">Pagado</div>
              <div class="metric-value text-success">{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(totalPagado) }}</div>
            </div>
            <div class="col-6 col-lg-3">
              <div class="metric-label">Saldo</div>
              <div class="metric-value" :class="(venta.saldo ?? 0) > 0 ? 'text-danger' : 'text-success'">
                {{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.saldo) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="actions-strip card border-0 shadow-sm mb-3">
        <div class="card-body d-flex flex-wrap gap-2">
          <router-link v-if="puedeRegistrarPago" :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-outline-primary btn-sm">
            Registrar pago
          </router-link>
          <router-link
            v-if="venta.estado === 'PENDIENTE' || venta.estado === 'RESERVADA'"
            :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }"
            class="btn btn-outline-success btn-sm"
          >
            Confirmar venta
          </router-link>
          <router-link v-if="puedeEmitirComprobante" :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-outline-dark btn-sm">
            Emitir comprobante
          </router-link>
          <router-link v-if="!puedeRegistrarPago && !puedeEmitirComprobante" :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-outline-secondary btn-sm">
            Ver acciones
          </router-link>
        </div>
      </div>

      <div v-if="creditoPlanAhorro > 0" class="card border-0 shadow-sm mb-3">
        <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
          <span>Credito aplicado desde plan de ahorro</span>
          <span class="badge bg-primary">PLAN_AHORRO</span>
        </div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-4"><strong>Monto reconocido:</strong> {{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(creditoPlanAhorro) }}</div>
            <div class="col-md-4"><strong>Diferencia restante:</strong> {{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(diferenciaRestantePlan) }}</div>
            <div class="col-md-4"><strong>Contrato asociado:</strong> {{ contratoPlanAsociado ? `#${contratoPlanAsociado}` : '-' }}</div>
          </div>
        </div>
      </div>

      <div class="row g-3 mb-3">
        <div class="col-12 col-xl-6">
          <div class="card h-100 border-0 shadow-sm">
            <div class="card-header bg-white fw-semibold">Cliente</div>
            <div class="card-body">
              <dl class="detail-list mb-0">
                <dt>Nombre</dt>
                <dd>{{ venta.cliente?.nombre }} {{ venta.cliente?.apellido }}</dd>
                <dt>Documento</dt>
                <dd>{{ venta.cliente?.nroDocumento ?? '-' }}</dd>
                <dt>Telefono</dt>
                <dd>{{ venta.cliente?.telefono ?? '-' }}</dd>
                <dt>Email</dt>
                <dd>{{ venta.cliente?.email ?? '-' }}</dd>
                <dt>Condicion IVA</dt>
                <dd>{{ venta.cliente?.condicionIva?.descripcion ?? venta.cliente?.condicionIva?.codigo ?? '-' }}</dd>
              </dl>
            </div>
          </div>
        </div>

        <div class="col-12 col-xl-6">
          <div class="card h-100 border-0 shadow-sm">
            <div class="card-header bg-white fw-semibold">Estado financiero</div>
            <div class="card-body">
              <dl class="detail-list mb-0">
                <dt>Subtotal</dt>
                <dd>{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.importeNeto) }}</dd>
                <dt>IVA</dt>
                <dd>{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.impuesto) }}</dd>
                <dt>Total</dt>
                <dd class="fw-semibold">{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.total) }}</dd>
                <dt>Pagado</dt>
                <dd class="text-success fw-semibold">{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(totalPagado) }}</dd>
                <dt>Saldo</dt>
                <dd :class="(venta.saldo ?? 0) > 0 ? 'text-danger fw-semibold' : 'text-success fw-semibold'">
                  {{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(venta.saldo) }}
                </dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="card border-0 shadow-sm mb-3">
        <div class="card-header bg-white fw-semibold">Vehiculo</div>
        <div v-if="detalles.length === 0" class="card-body text-muted">No hay vehiculo asociado.</div>
        <div v-else class="table-responsive">
          <table class="table table-sm align-middle mb-0">
            <thead>
              <tr>
                <th>Patente</th>
                <th>Unidad</th>
                <th>Estado</th>
                <th class="text-end">Precio final</th>
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
                  <span class="badge bg-light text-dark border">{{ detalle.vehiculo?.estadoInventario ?? detalle.vehiculo?.estado ?? '-' }}</span>
                </td>
                <td class="text-end fw-semibold">{{ ventaMonedaDisplay?.simbolo ?? '$' }} {{ formatPrecio(detalle.subtotal) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card border-0 shadow-sm mb-3">
        <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
          <span>Pagos</span>
          <router-link v-if="puedeRegistrarPago" :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-outline-primary btn-sm">
            Registrar pago
          </router-link>
        </div>
        <div v-if="loadingPagos" class="card-body text-muted">Cargando pagos...</div>
        <div v-else-if="pagos.length === 0" class="card-body text-muted">No hay pagos registrados.</div>
        <div v-else class="table-responsive">
          <table class="table table-sm align-middle mb-0">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Metodo</th>
                <th>Moneda</th>
                <th class="text-end">Monto original</th>
                <th class="text-end">Aplicado ARS</th>
                <th class="text-end">Cotizacion</th>
                <th>Entidad</th>
                <th>Referencia</th>
                <th>Usuario</th>
                <th>Estado</th>
                <th class="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="pago in pagos" :key="pago.id" :class="pago.estado === 'ANULADO' ? 'table-secondary' : ''">
                <td>{{ formatFecha(pago.fecha) }}</td>
                <td>{{ pago.metodoPago?.descripcion ?? pago.metodoPago?.codigo ?? '-' }}</td>
                <td>{{ pago.moneda?.codigo ?? '-' }}</td>
                <td class="text-end">{{ formatPrecio(pago.monto) }}</td>
                <td class="text-end fw-semibold">{{ formatPrecio(pago.montoAplicadoVenta ?? pago.monto) }}</td>
                <td class="text-end">{{ formatCotizacion(pago.cotizacionUsada) }}</td>
                <td>{{ pago.entidadFinanciera?.nombre ?? pago.bancoEntidad ?? '-' }}</td>
                <td>{{ pago.referencia ?? pago.numeroOperacion ?? '-' }}</td>
                <td>{{ pago.usuarioRegistro ?? '-' }}</td>
                <td>
                  <span class="badge me-1" :class="pago.estado === 'ANULADO' ? 'bg-secondary' : 'bg-success'">
                    {{ pago.estado === 'ANULADO' ? 'ANULADO' : 'REGISTRADO' }}
                  </span>
                  <span v-if="esPagoPlanAhorro(pago)" class="badge bg-primary">PLAN_AHORRO</span>
                </td>
                <td class="text-end">
                  <button v-if="pago.estado !== 'ANULADO' && pago.id" class="btn btn-outline-danger btn-sm" @click="abrirModalAnulacionPago(pago.id)">Anular</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card border-0 shadow-sm mb-3">
        <div class="card-header bg-white fw-semibold">Comprobantes</div>
        <div v-if="loadingComprobantes" class="card-body text-muted">Cargando comprobantes...</div>
        <div v-else-if="comprobantes.length === 0" class="card-body text-muted">No hay comprobantes emitidos.</div>
        <div v-else class="table-responsive">
          <table class="table table-sm align-middle mb-0">
            <thead>
              <tr>
                <th>Numero</th>
                <th>Tipo</th>
                <th>Estado</th>
                <th>Fecha emision</th>
                <th class="text-end">Total</th>
                <th>Usuario emision</th>
                <th class="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="comprobante in comprobantes" :key="comprobante.id" :class="comprobante.estado === 'ANULADO' ? 'table-secondary' : ''">
                <td class="fw-semibold">{{ comprobante.numeroComprobante ?? '-' }}</td>
                <td>{{ comprobante.tipoComprobante?.codigo ?? '-' }}</td>
                <td>
                  <span class="badge" :class="comprobante.estado === 'ANULADO' ? 'bg-secondary' : 'bg-success'">
                    {{ labelEstadoComprobante(comprobante.estado) }}
                  </span>
                </td>
                <td>{{ formatFecha(comprobante.fechaEmision) }}</td>
                <td class="text-end">{{ formatPrecio(comprobante.total) }}</td>
                <td>{{ comprobante.usuarioEmision ?? comprobante.createdBy ?? '-' }}</td>
                <td class="text-end">
                  <button class="btn btn-outline-primary btn-sm me-2" @click="descargarPdfComprobante(comprobante.id)">Descargar PDF</button>
                  <button
                    v-if="comprobante.estado !== 'ANULADO' && comprobante.id"
                    class="btn btn-outline-danger btn-sm"
                    @click="abrirModalAnulacionComprobante(comprobante.id)"
                  >
                    Anular
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="tasacionUsada" class="card border-0 shadow-sm mb-3">
        <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
          <span>Entrega de usado</span>
          <router-link v-if="tasacionUsadaId" :to="{ name: 'TasacionUsadoView', params: { tasacionUsadoId: tasacionUsadaId } }" class="btn btn-outline-primary btn-sm">
            Ver tasacion
          </router-link>
        </div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-4"><strong>Patente:</strong> {{ tasacionUsada.patenteUsado ?? '-' }}</div>
            <div class="col-md-4"><strong>Modelo:</strong> {{ tasacionUsada.marcaModeloUsado ?? '-' }}</div>
            <div class="col-md-4"><strong>Monto:</strong> {{ tasacionUsada.moneda?.simbolo ?? '$' }} {{ formatPrecio(tasacionUsada.montoTasacion) }}</div>
          </div>
        </div>
      </div>

      <div class="card border-0 shadow-sm mb-3">
        <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
          <span>Entrega de unidad</span>
          <span v-if="entregaUnidad" class="badge bg-light text-dark border">{{ entregaUnidad.estado }}</span>
        </div>
        <div v-if="loadingEntrega" class="card-body text-muted">Cargando entrega...</div>
        <div v-else-if="!entregaUnidad" class="card-body d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <div class="fw-semibold">Entrega aun no programada</div>
            <div class="text-muted small">Programa la fecha de entrega para continuar con el ciclo operativo de la unidad.</div>
          </div>
          <button class="btn btn-outline-primary btn-sm" @click="showProgramarEntrega = true">Programar entrega</button>
        </div>
        <div v-else class="card-body">
          <div class="row g-3 mb-3">
            <div class="col-md-4"><strong>Fecha programada:</strong> {{ formatFecha(entregaUnidad.fechaProgramada) }}</div>
            <div class="col-md-4"><strong>Fecha entrega:</strong> {{ formatFecha(entregaUnidad.fechaEntrega) }}</div>
            <div class="col-md-4"><strong>Usuario:</strong> {{ entregaUnidad.usuarioProgramacion ?? '-' }}</div>
          </div>
          <div class="delivery-progress card border-0 bg-light mb-3">
            <div class="card-body py-2">
              <div class="d-flex justify-content-between align-items-center small mb-1">
                <strong>Checklist completado</strong>
                <span>{{ checklistStats.completos }}/{{ checklistStats.total }} items ({{ checklistStats.progreso }}%)</span>
              </div>
              <div class="progress" role="progressbar" aria-label="Progreso checklist" aria-valuemin="0" aria-valuemax="100" :aria-valuenow="checklistStats.progreso">
                <div class="progress-bar" :class="checklistObligatorioCompleto ? 'bg-success' : 'bg-warning'" :style="{ width: `${checklistStats.progreso}%` }" />
              </div>
              <div v-if="checklistStats.obligatoriosPendientes > 0" class="text-danger small mt-2">
                Faltan {{ checklistStats.obligatoriosPendientes }} item(s) obligatorios para confirmar la entrega.
              </div>
            </div>
          </div>
          <div class="checklist-grid mb-3">
            <div
              v-for="item in entregaUnidad.checklist"
              :key="item.id"
              class="checklist-item card border"
              :class="{ obligatorio: item.obligatorio, cumplido: item.completado }"
            >
              <div class="card-body py-2">
                <div class="d-flex justify-content-between align-items-center gap-2">
                  <div class="fw-semibold">{{ item.descripcion }}</div>
                  <span class="badge" :class="item.obligatorio ? 'bg-danger-subtle text-danger-emphasis border border-danger-subtle' : 'bg-light text-dark border'">
                    {{ item.obligatorio ? 'Obligatorio' : 'Opcional' }}
                  </span>
                </div>
                <div class="d-flex align-items-center gap-2 mt-2">
                  <input
                    v-model="item.completado"
                    type="checkbox"
                    class="form-check-input mt-0"
                    :disabled="!puedeEditarEntrega"
                  />
                  <label class="small text-muted mb-0">Marcar como completado</label>
                </div>
                <input
                  v-model="item.observaciones"
                  class="form-control form-control-sm mt-2"
                  :disabled="!puedeEditarEntrega"
                  placeholder="Observacion del item (opcional)"
                />
              </div>
            </div>
          </div>
          <div class="d-flex gap-2 flex-wrap">
            <button class="btn btn-outline-primary btn-sm" :disabled="!puedeEditarEntrega" @click="guardarChecklist">Guardar checklist</button>
            <button class="btn btn-success btn-sm" :disabled="!checklistObligatorioCompleto || !puedeEditarEntrega" @click="showConfirmarEntrega = true">
              Confirmar entrega
            </button>
            <button class="btn btn-outline-danger btn-sm" :disabled="!puedeEditarEntrega" @click="cancelarEntrega">
              Cancelar entrega
            </button>
          </div>
        </div>
      </div>

      <div class="card border-0 shadow-sm mb-3">
        <div class="card-header bg-white fw-semibold">Historial de operacion</div>
        <div v-if="timeline.length === 0" class="card-body text-muted">No hay eventos para mostrar.</div>
        <div v-else class="card-body">
          <div class="timeline">
            <div v-for="event in timeline" :key="event.key" class="timeline-item">
              <div class="timeline-dot" :class="timelineToneClass(event.tono)" />
              <div class="timeline-content">
                <div class="d-flex flex-wrap justify-content-between gap-2">
                  <div class="fw-semibold">{{ event.titulo }}</div>
                  <div class="text-muted small">{{ formatFecha(event.fecha) }}</div>
                </div>
                <div v-if="event.detalle" class="text-muted small">{{ event.detalle }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <OperationalTraceCard
        class="mb-3"
        title="Trazabilidad tecnica"
        :status="labelEstado(venta.estado)"
        :created-by="venta.createdBy ?? venta.user?.login ?? 'No disponible'"
        :created-at="venta.createdDate ? formatFecha(venta.createdDate) : 'No disponible'"
        :updated-by="venta.lastModifiedBy ?? venta.user?.login ?? 'No disponible'"
        :updated-at="venta.lastModifiedDate ? formatFecha(venta.lastModifiedDate) : 'No disponible'"
        :last-action="timeline[0]?.titulo ?? 'Sin actividad reciente'"
        :last-action-at="timeline[0]?.fecha ? formatFecha(timeline[0].fecha) : 'No disponible'"
      />

      <b-modal v-model="mostrarModalAnulacionPago" title="Anular pago">
        <div class="mb-2">Ingresa motivo de anulacion</div>
        <textarea v-model="motivoAnulacionPago" class="form-control" rows="3" maxlength="500" />
        <template #footer>
          <button type="button" class="btn btn-secondary btn-sm" @click="cerrarModalAnulacionPago">Cancelar</button>
          <button type="button" class="btn btn-danger btn-sm" :disabled="!motivoAnulacionPago?.trim()" @click="confirmarAnulacionPago">
            Confirmar anulacion
          </button>
        </template>
      </b-modal>

      <b-modal v-model="mostrarModalAnulacionComprobante" title="Anular comprobante">
        <div class="mb-2">Ingresa motivo de anulacion</div>
        <textarea v-model="motivoAnulacionComprobante" class="form-control" rows="3" maxlength="500" />
        <template #footer>
          <button type="button" class="btn btn-secondary btn-sm" @click="cerrarModalAnulacionComprobante">Cancelar</button>
          <button type="button" class="btn btn-danger btn-sm" :disabled="!motivoAnulacionComprobante?.trim()" @click="confirmarAnulacionComprobante">
            Confirmar anulacion
          </button>
        </template>
      </b-modal>

      <b-modal v-model="showProgramarEntrega" title="Programar entrega">
        <div class="mb-2">
          <label class="form-label">Fecha programada</label>
          <input v-model="fechaProgramada" type="date" class="form-control" />
        </div>
        <div class="mb-2">
          <label class="form-label">Observaciones</label>
          <textarea v-model="observacionesEntrega" class="form-control" rows="2" />
        </div>
        <template #footer>
          <button class="btn btn-outline-secondary btn-sm" @click="showProgramarEntrega = false">Cancelar</button>
          <button class="btn btn-primary btn-sm" :disabled="!fechaProgramada" @click="programarEntrega">Programar</button>
        </template>
      </b-modal>

      <b-modal v-model="showConfirmarEntrega" title="Confirmar entrega">
        <div class="alert alert-light border small mb-3">
          Antes de confirmar: verifica checklist obligatorio, kilometraje real y nivel de combustible al momento de la entrega.
        </div>
        <div class="mb-2">
          <label class="form-label">Kilometraje de entrega</label>
          <input v-model.number="kilometrajeEntrega" class="form-control" type="number" min="0" step="1" placeholder="Ej: 12" />
        </div>
        <div class="mb-2">
          <label class="form-label">Nivel de combustible</label>
          <select v-model="nivelCombustible" class="form-select">
            <option value="">Seleccionar nivel</option>
            <option v-for="nivel in nivelesCombustible" :key="nivel.value" :value="nivel.value">{{ nivel.label }}</option>
          </select>
        </div>
        <div class="mb-2">
          <label class="form-label">Observaciones</label>
          <textarea v-model="observacionesConfirmacion" class="form-control" rows="3" maxlength="500" placeholder="Detalles de entrega, accesorios, estado final, etc." />
        </div>
        <template #footer>
          <button class="btn btn-outline-secondary btn-sm" @click="showConfirmarEntrega = false">Cancelar</button>
          <button class="btn btn-success btn-sm" :disabled="!checklistObligatorioCompleto || submittingConfirmacionEntrega" @click="confirmarEntrega">
            {{ submittingConfirmacionEntrega ? 'Confirmando...' : 'Confirmar entrega' }}
          </button>
        </template>
      </b-modal>
    </template>
  </div>
</template>

<script lang="ts" src="./venta-details.component.ts"></script>

<style scoped>
.venta-view {
  max-width: 1320px;
}

.executive-header {
  background: linear-gradient(130deg, #f8fbff 0%, #eef4ff 100%);
}

.metric-label {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-muted);
}

.metric-value {
  font-size: 1.2rem;
  font-weight: 700;
}

.detail-list {
  display: grid;
  grid-template-columns: minmax(120px, auto) 1fr;
  gap: 0.45rem 0.9rem;
}

.detail-list dt {
  margin: 0;
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-text-muted);
}

.detail-list dd {
  margin: 0;
  font-size: 0.93rem;
}

.timeline {
  position: relative;
  padding-left: 1rem;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  bottom: 2px;
  width: 2px;
  background: #d8e0ea;
}

.timeline-item {
  position: relative;
  padding-left: 1.2rem;
  margin-bottom: 1rem;
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: 0;
  top: 0.25rem;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: currentColor;
}

.delivery-progress .progress {
  height: 8px;
}

.checklist-grid {
  display: grid;
  gap: 0.75rem;
}

.checklist-item {
  border-color: #dce3eb !important;
}

.checklist-item.obligatorio {
  border-left: 4px solid #dc3545 !important;
}

.checklist-item.cumplido {
  background: #f6fff9;
}

@media (max-width: 767px) {
  .detail-list {
    grid-template-columns: 1fr;
    gap: 0.2rem;
  }
}
</style>
