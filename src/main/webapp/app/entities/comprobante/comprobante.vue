<template>
  <div class="container-fluid px-0 comprobante-page">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <h2 class="h4 mb-1">Comprobantes</h2>
          <p class="text-muted mb-0">Gestión operativa y emisión de comprobantes.</p>
        </div>
        <button class="btn btn-outline-primary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" class="me-2" />
          Actualizar
        </button>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body py-3 d-flex flex-wrap gap-3 align-items-center">
        <div class="metric">
          <span class="metric-label">Total registros</span>
          <strong class="metric-value">{{ queryCount ?? 0 }}</strong>
        </div>
        <div class="metric">
          <span class="metric-label">Emitidos</span>
          <strong class="metric-value text-success">{{ comprobantes.filter(c => c.estado === 'EMITIDO').length }}</strong>
        </div>
        <div class="metric">
          <span class="metric-label">Anulados</span>
          <strong class="metric-value text-secondary">{{ comprobantes.filter(c => c.estado === 'ANULADO').length }}</strong>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-2 align-items-end">
          <div class="col-12 col-md-3">
            <label class="form-label mb-1">Búsqueda</label>
            <input v-model="filters.q" type="text" class="form-control form-control-sm" placeholder="Número, tipo o ID..." />
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label mb-1">Estado</label>
            <select v-model="filters.estado" class="form-select form-select-sm">
              <option value="">Todos</option>
              <option value="EMITIDO">Emitido</option>
              <option value="ANULADO">Anulado</option>
            </select>
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label mb-1">Tipo</label>
            <select v-model="filters.tipo" class="form-select form-select-sm">
              <option value="">Todos</option>
              <option v-for="tipo in tipoOptions" :key="tipo" :value="tipo">{{ tipo }}</option>
            </select>
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label mb-1">Moneda</label>
            <select v-model="filters.moneda" class="form-select form-select-sm">
              <option value="">Todas</option>
              <option v-for="moneda in monedaOptions" :key="moneda" :value="moneda">{{ moneda }}</option>
            </select>
          </div>
          <div class="col-6 col-md-1">
            <label class="form-label mb-1">Venta</label>
            <input v-model="filters.ventaId" type="text" class="form-control form-control-sm" placeholder="#" />
          </div>
          <div class="col-6 col-md-1">
            <label class="form-label mb-1">Desde</label>
            <input v-model="filters.fechaDesde" type="date" class="form-control form-control-sm" />
          </div>
          <div class="col-6 col-md-1">
            <label class="form-label mb-1">Hasta</label>
            <input v-model="filters.fechaHasta" type="date" class="form-control form-control-sm" />
          </div>
          <div class="col-12 col-md-3">
            <label class="form-label mb-1">Usuario emisor</label>
            <input v-model="filters.usuario" type="text" class="form-control form-control-sm" placeholder="usuario" />
          </div>
          <div class="col-12 col-md-2">
            <button class="btn btn-light btn-sm w-100" @click="resetFilters">Limpiar filtros</button>
          </div>
        </div>
      </div>
    </div>

    <div class="alert alert-light border" v-if="!isFetching && filteredComprobantes?.length === 0">No se encontraron comprobantes con los filtros actuales.</div>

    <div class="card border-0 shadow-sm" v-if="filteredComprobantes?.length > 0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0" aria-describedby="comprobantes">
          <thead class="table-light">
            <tr>
              <th>ID</th>
              <th>Número</th>
              <th>Tipo</th>
              <th>Estado</th>
              <th>Fecha emisión</th>
              <th class="text-end">Total</th>
              <th>Moneda</th>
              <th>Venta</th>
              <th>Usuario emisor</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="comprobante in filteredComprobantes" :key="comprobante.id" :class="comprobante.estado === 'ANULADO' ? 'table-secondary' : ''">
              <td>
                <router-link :to="{ name: 'ComprobanteView', params: { comprobanteId: comprobante.id } }">{{ comprobante.id }}</router-link>
              </td>
              <td class="fw-semibold">{{ comprobante.numeroComprobante }}</td>
              <td>{{ comprobante.tipoComprobante?.codigo ?? '-' }}</td>
              <td><span class="badge" :class="estadoClass(comprobante.estado)">{{ estadoLabel(comprobante.estado) }}</span></td>
              <td>{{ formatDateShort(comprobante.fechaEmision) || '-' }}</td>
              <td class="text-end fw-semibold">{{ formatMoney(comprobante.total) }}</td>
              <td>{{ comprobante.moneda?.simbolo ?? '' }} {{ comprobante.moneda?.codigo ?? '-' }}</td>
              <td>
                <router-link v-if="comprobante.venta?.id" :to="{ name: 'VentaView', params: { ventaId: comprobante.venta.id } }">#{{ comprobante.venta.id }}</router-link>
                <span v-else>-</span>
              </td>
              <td>{{ comprobante.usuarioEmision ?? comprobante.createdBy ?? '-' }}</td>
              <td class="text-end">
                <router-link :to="{ name: 'ComprobanteView', params: { comprobanteId: comprobante.id } }" class="btn btn-outline-secondary btn-sm me-1">
                  Ver
                </router-link>
                <button class="btn btn-outline-primary btn-sm me-1" @click="descargarPdf(comprobante.id)">PDF</button>
                <b-button v-if="comprobante.estado !== 'ANULADO'" @click="prepareRemove(comprobante)" variant="outline-danger" class="btn btn-sm" v-b-modal.removeEntity>
                  Anular
                </b-button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <b-modal ref="removeEntity" id="removeEntity">
      <template #title>Anular comprobante</template>
      <div class="modal-body">
        <p class="mb-2">Confirmás la anulación del comprobante <strong>#{{ removeId }}</strong>.</p>
        <label class="form-label">Motivo</label>
        <textarea v-model="motivoAnulacion" class="form-control" rows="3" maxlength="500" />
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary btn-sm" @click="closeDialog()">Cancelar</button>
          <button type="button" class="btn btn-danger btn-sm" :disabled="!motivoAnulacion || !motivoAnulacion.trim()" @click="removeComprobante">
            Confirmar anulación
          </button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./comprobante.component.ts"></script>

<style scoped>
.comprobante-page {
  width: 100%;
  max-width: none;
}

.metric {
  min-width: 140px;
}

.metric-label {
  display: block;
  font-size: 0.74rem;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.metric-value {
  font-size: 1.1rem;
}
</style>
