<template>
  <div class="container-fluid px-0" data-cy="PagoHeading">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <h2 class="h4 mb-1">Pagos</h2>
          <p class="text-muted mb-0">Gestión operativa y trazabilidad de cobros.</p>
        </div>
        <button class="btn btn-outline-primary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" class="me-2" />
          Actualizar
        </button>
      </div>
    </div>

    <div class="row g-3 mb-3">
      <div class="col-12 col-md-4">
        <div class="card border-0 shadow-sm">
          <div class="card-body">
            <p class="text-muted mb-1 small">Total pagos</p>
            <h3 class="h5 mb-0">{{ pagos.length }}</h3>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="card border-0 shadow-sm">
          <div class="card-body">
            <p class="text-muted mb-1 small">Registrados</p>
            <h3 class="h5 mb-0">{{ pagos.filter(p => p.estado !== 'ANULADO').length }}</h3>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="card border-0 shadow-sm">
          <div class="card-body">
            <p class="text-muted mb-1 small">Anulados</p>
            <h3 class="h5 mb-0">{{ pagos.filter(p => p.estado === 'ANULADO').length }}</h3>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-2 align-items-end">
          <div class="col-12 col-md-3">
            <label class="form-label mb-1">Búsqueda</label>
            <input v-model="filters.q" type="text" class="form-control form-control-sm" placeholder="ID, referencia, operación..." />
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label mb-1">Estado</label>
            <select v-model="filters.estado" class="form-select form-select-sm">
              <option value="">Todos</option>
              <option value="REGISTRADO">Registrado</option>
              <option value="ANULADO">Anulado</option>
            </select>
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label mb-1">Método</label>
            <select v-model="filters.metodo" class="form-select form-select-sm">
              <option value="">Todos</option>
              <option v-for="metodo in metodoOptions" :key="metodo" :value="metodo">{{ metodo }}</option>
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
            <label class="form-label mb-1">Usuario</label>
            <input v-model="filters.usuario" type="text" class="form-control form-control-sm" placeholder="usuario registro" />
          </div>
          <div class="col-12 col-md-2">
            <button class="btn btn-light btn-sm w-100" @click="resetFilters">Limpiar filtros</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="alert alert-light m-3 border" v-if="!isFetching && filteredPagos?.length === 0">No se encontraron pagos con los filtros actuales.</div>

        <div class="table-responsive" v-if="filteredPagos?.length > 0">
          <table class="table table-hover align-middle mb-0" aria-describedby="pagos">
            <thead class="table-light">
              <tr>
                <th scope="col" @click="changeOrder('id')" class="sortable">ID</th>
                <th scope="col" @click="changeOrder('fecha')" class="sortable">Fecha</th>
                <th scope="col" @click="changeOrder('metodoPago.id')" class="sortable">Método</th>
                <th scope="col" @click="changeOrder('monto')" class="sortable text-end">Monto original</th>
                <th scope="col" @click="changeOrder('moneda.id')" class="sortable">Moneda</th>
                <th scope="col" @click="changeOrder('cotizacionUsada')" class="sortable text-end">Cotización</th>
                <th scope="col" @click="changeOrder('montoAplicadoVenta')" class="sortable text-end">Aplicado ARS</th>
                <th scope="col" @click="changeOrder('usuarioRegistro')" class="sortable">Usuario</th>
                <th scope="col" @click="changeOrder('estado')" class="sortable">Estado</th>
                <th scope="col" class="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="pago in filteredPagos" :key="pago.id" data-cy="entityTable" :class="{ 'table-secondary': pago.estado === 'ANULADO' }">
                <td>
                  <router-link :to="{ name: 'PagoView', params: { pagoId: pago.id } }" class="fw-semibold text-decoration-none">#{{ pago.id }}</router-link>
                </td>
                <td>{{ formatDateShort(pago.fecha) || '-' }}</td>
                <td>{{ pago.metodoPago?.descripcion ?? pago.metodoPago?.codigo ?? '-' }}</td>
                <td class="text-end">{{ formatMoney(pago.monto, pago.moneda?.codigo ?? 'ARS') }}</td>
                <td>{{ pago.moneda?.simbolo ?? '' }} {{ pago.moneda?.codigo ?? '-' }}</td>
                <td class="text-end">{{ pago.cotizacionUsada ?? '-' }}</td>
                <td class="text-end">{{ formatMoney(pago.montoAplicadoVenta, 'ARS') }}</td>
                <td>{{ pago.usuarioRegistro ?? '-' }}</td>
                <td>
                  <span class="badge rounded-pill" :class="estadoClass(pago.estado)">{{ estadoLabel(pago.estado) }}</span>
                </td>
                <td class="text-end">
                  <div class="btn-group">
                    <router-link :to="{ name: 'PagoView', params: { pagoId: pago.id } }" custom v-slot="{ navigate }">
                      <button @click="navigate" class="btn btn-outline-primary btn-sm">
                        <font-awesome-icon icon="eye" class="me-1" />
                        Ver
                      </button>
                    </router-link>
                    <b-button
                      v-if="pago.estado !== 'ANULADO'"
                      @click="prepareRemove(pago)"
                      variant="outline-danger"
                      class="btn btn-sm"
                      v-b-modal.removeEntity
                    >
                      <font-awesome-icon icon="ban" class="me-1" />
                      Anular
                    </b-button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <b-modal ref="removeEntity" id="removeEntity" centered>
      <template #title>Anular pago</template>
      <div class="modal-body">
        <p class="mb-2">Vas a anular el pago <strong>#{{ removeId }}</strong>. Esta acción preserva la trazabilidad y revierte su impacto financiero.</p>
        <label class="form-label">Motivo de anulación</label>
        <textarea v-model="motivoAnulacion" class="form-control" rows="3" maxlength="500" />
      </div>
      <template #footer>
        <div class="d-flex gap-2">
          <button type="button" class="btn btn-light" @click="closeDialog()">Cancelar</button>
          <button type="button" class="btn btn-danger" :disabled="!motivoAnulacion || !motivoAnulacion.trim()" @click="removePago">
            Confirmar anulación
          </button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./pago.component.ts"></script>

<style scoped>
.sortable {
  cursor: pointer;
  user-select: none;
}
</style>
