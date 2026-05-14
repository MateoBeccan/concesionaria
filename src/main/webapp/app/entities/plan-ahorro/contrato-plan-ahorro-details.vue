<template>
  <div class="container-fluid px-0 contrato-details-page">
    <div class="card border-0 shadow-sm mb-3" v-if="contrato">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <p class="section-kicker mb-1">Contrato de plan</p>
          <h1 class="h4 mb-1">Contrato {{ contrato.numeroContrato }}</h1>
          <p class="text-muted mb-0">{{ contrato.cliente?.apellido }} {{ contrato.cliente?.nombre }} · {{ contrato.plan?.nombre }} · {{ contrato.estado }}</p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <div class="metric-chip">
            <small>Pagadas</small>
            <strong>{{ cuotasPagadasCount }}</strong>
          </div>
          <div class="metric-chip">
            <small>Pendientes</small>
            <strong>{{ cuotasPendientesCount }}</strong>
          </div>
          <router-link :to="{ name: 'ContratoPlanAhorro' }" class="btn btn-outline-secondary">Volver</router-link>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-header bg-white d-flex justify-content-between align-items-center flex-wrap gap-2">
        <strong>Cuotas del contrato</strong>
        <div class="cuota-selector">
          <label class="form-label mb-1 small text-muted">Cuota a visualizar</label>
          <select v-model.number="cuotaSeleccionadaId" class="form-select form-select-sm">
            <option v-for="cuota in cuotas" :key="cuota.id" :value="cuota.id">
              Cuota {{ cuota.numeroCuota }} · {{ cuota.estado }} · {{ formatDateShort(cuota.fechaVencimiento) }}
            </option>
          </select>
        </div>
      </div>
      <div class="card-body" v-if="cuotaSeleccionada">
        <div class="row g-3">
          <div class="col-12 col-lg-8">
            <div class="surface-card border rounded-3 p-3 h-100">
              <h6 class="mb-3">Detalle de cuota #{{ cuotaSeleccionada.numeroCuota }}</h6>
              <div class="row g-2">
                <div class="col-6 col-md-4"><small class="text-muted d-block">Vencimiento</small><strong>{{ formatDateShort(cuotaSeleccionada.fechaVencimiento) }}</strong></div>
                <div class="col-6 col-md-4">
                  <small class="text-muted d-block">Importe</small>
                  <strong>{{ Number(cuotaSeleccionada.importe ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</strong>
                </div>
                <div class="col-6 col-md-4">
                  <small class="text-muted d-block">Estado</small>
                  <span class="badge rounded-pill" :class="cuotaSeleccionada.estado === 'PAGADA' ? 'bg-success' : 'bg-secondary'">{{ cuotaSeleccionada.estado }}</span>
                </div>
                <div class="col-6 col-md-4"><small class="text-muted d-block">Pago</small><router-link v-if="cuotaSeleccionada.pagoId" :to="{ name: 'PagoView', params: { pagoId: cuotaSeleccionada.pagoId } }">#{{ cuotaSeleccionada.pagoId }}</router-link><strong v-else>-</strong></div>
                <div class="col-6 col-md-4"><small class="text-muted d-block">Fecha pago</small><strong>{{ cuotaSeleccionada.fechaPago ? formatDateShort(cuotaSeleccionada.fechaPago) : '-' }}</strong></div>
                <div class="col-6 col-md-4">
                  <small class="text-muted d-block">Comprobante</small>
                  <span v-if="cuotaSeleccionada.comprobantePlanAhorroId">{{ cuotaSeleccionada.numeroComprobantePlanAhorro ?? `#${cuotaSeleccionada.comprobantePlanAhorroId}` }}</span>
                  <strong v-else>-</strong>
                </div>
              </div>
            </div>
          </div>
          <div class="col-12 col-lg-4">
            <div class="surface-card border rounded-3 p-3 h-100">
              <h6 class="mb-3">Acciones</h6>
              <div class="d-grid gap-2">
                <button
                  v-if="cuotaSeleccionada.estado === 'PENDIENTE' || cuotaSeleccionada.estado === 'VENCIDA'"
                  class="btn btn-primary"
                  @click="abrirPago(cuotaSeleccionada)"
                >
                  Pagar cuota
                </button>
                <button
                  v-if="cuotaSeleccionada.comprobantePlanAhorroId && cuotaSeleccionada.estadoComprobantePlanAhorro === 'EMITIDO'"
                  class="btn btn-outline-danger"
                  @click="anularComprobante(cuotaSeleccionada)"
                >
                  Anular comprobante
                </button>
                <button v-if="cuotaSeleccionada.comprobantePlanAhorroId" class="btn btn-outline-primary" @click="descargarComprobante(cuotaSeleccionada)">
                  Descargar PDF
                </button>
                <div v-if="cuotaSeleccionada.comprobantePlanAhorroId" class="small">
                  <span class="badge rounded-pill" :class="cuotaSeleccionada.estadoComprobantePlanAhorro === 'EMITIDO' ? 'bg-success' : 'bg-warning text-dark'">
                    {{ cuotaSeleccionada.estadoComprobantePlanAhorro }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="card-body text-muted">No hay cuotas para mostrar.</div>
    </div>

    <div class="card border-0 shadow-sm mt-3" v-if="contrato">
      <div class="card-header d-flex justify-content-between align-items-center">
        <strong>Adjudicacion del contrato</strong>
        <span v-if="adjudicacion" class="badge bg-primary">{{ adjudicacion.estado }}</span>
      </div>
      <div class="card-body">
        <div v-if="!adjudicacion" class="d-flex flex-wrap align-items-end gap-2">
          <div class="flex-grow-1">
            <label class="form-label">Observaciones</label>
            <input v-model="adjudicacionObservaciones" type="text" class="form-control" placeholder="Opcional" />
          </div>
          <button class="btn btn-primary" :disabled="!puedeAdjudicar || loadingAdjudicacion" @click="adjudicarContrato">Adjudicar contrato</button>
        </div>

        <div v-else class="vstack gap-3">
          <div class="alert alert-info py-2 mb-0">Gestiona la adjudicacion del contrato y, cuando corresponda, genera la venta vinculada.</div>
          <div class="row g-3">
            <div class="col-12 col-lg-6">
              <label class="form-label">Inventario compatible disponible</label>
              <select v-model.number="inventarioSeleccionadoId" class="form-select" :disabled="loadingAdjudicacion">
                <option :value="null">Seleccionar unidad</option>
                <option v-for="item in inventariosCompatibles" :key="item.id" :value="item.id">
                  {{ item.codigoInternoStock }} · {{ item.vehiculo?.patente || 'SIN PATENTE' }} · {{ item.vehiculo?.precio || 0 }}
                </option>
              </select>
            </div>
            <div class="col-12 col-lg-6 d-flex align-items-end">
              <button class="btn btn-outline-primary" :disabled="!inventarioSeleccionadoId || loadingAdjudicacion" @click="asignarInventario">
                Asignar inventario
              </button>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-12 col-md-6">
              <div class="surface-card border rounded-3 p-3 h-100">
                <h6 class="mb-2">Resumen economico</h6>
                <p class="mb-1"><strong>Cuotas pagadas:</strong> {{ contrato.cuotasPagadas || 0 }} / {{ contrato.cuotasTotales || 0 }}</p>
                <p class="mb-1"><strong>Monto reconocido:</strong> {{ adjudicacion.montoReconocidoCuotas || 0 }}</p>
                <p class="mb-1"><strong>Precio unidad:</strong> {{ adjudicacion.vehiculo?.precio || 0 }}</p>
                <p class="mb-0"><strong>Diferencia a pagar:</strong> {{ adjudicacion.diferenciaAPagar || 0 }}</p>
              </div>
            </div>
            <div class="col-12 col-md-6">
              <div class="surface-card border rounded-3 p-3 h-100">
                <h6 class="mb-2">Estado operativo</h6>
                <p class="mb-1"><strong>Cliente:</strong> {{ contrato.cliente?.apellido }} {{ contrato.cliente?.nombre }}</p>
                <p class="mb-1"><strong>Contrato:</strong> {{ contrato.numeroContrato }}</p>
                <p class="mb-1"><strong>Inventario:</strong> {{ adjudicacion.inventario?.codigoInternoStock || '-' }}</p>
                <p class="mb-0"><strong>Venta vinculada:</strong> {{ adjudicacion.venta?.id ? `#${adjudicacion.venta.id}` : '-' }}</p>
              </div>
            </div>
          </div>

          <div>
            <button
              class="btn btn-success"
              :disabled="!adjudicacion.inventario?.id || !!adjudicacion.venta?.id || loadingAdjudicacion"
              @click="generarVentaDesdeAdjudicacion"
            >
              Generar venta
            </button>
            <router-link v-if="adjudicacion.venta?.id" :to="{ name: 'VentaView', params: { ventaId: adjudicacion.venta.id } }" class="btn btn-link">
              Ver venta generada
            </router-link>
            <router-link
              v-if="adjudicacion.venta?.id"
              :to="{ name: 'VentaEditorEdit', params: { ventaId: adjudicacion.venta.id } }"
              class="btn btn-link"
            >
              Gestionar venta
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showPay" class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Pagar cuota #{{ cuotaActiva?.numeroCuota }}</h5>
            <button type="button" class="btn-close" @click="showPay = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-2">
              <label class="form-label">Monto</label>
              <input v-model.number="pagoMonto" type="number" class="form-control" min="0.01" step="0.01" />
            </div>
            <div class="mb-2">
              <label class="form-label">Observaciones</label>
              <textarea v-model="pagoObservaciones" class="form-control" rows="2"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-outline-secondary" @click="showPay = false">Cancelar</button>
            <button class="btn btn-primary" @click="confirmarPago">Confirmar pago</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showPay" class="modal-backdrop fade show"></div>
  </div>
</template>

<script lang="ts" src="./contrato-plan-ahorro-details.component.ts"></script>

<style scoped>
.contrato-details-page {
  width: 100%;
  max-width: none;
}

.section-kicker {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #64748b;
  font-weight: 700;
}

.metric-chip {
  display: inline-flex;
  flex-direction: column;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0.35rem 0.65rem;
  min-width: 88px;
  background: #fff;
}

.metric-chip small {
  font-size: 0.7rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.metric-chip strong {
  font-size: 1rem;
  color: #0f172a;
}

.surface-card {
  background: #f8fafc;
}

.card {
  border-radius: 14px;
}

.cuota-selector {
  min-width: 320px;
}

@media (max-width: 768px) {
  .cuota-selector {
    min-width: 100%;
  }
}
</style>
