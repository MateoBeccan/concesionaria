<template>
  <div class="container-fluid px-0 adjudicaciones-page">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <p class="section-kicker mb-1">Planes de ahorro</p>
          <h1 class="h4 mb-1">Adjudicaciones</h1>
          <p class="text-muted mb-0">Seguimiento operativo de adjudicaciones y ventas vinculadas.</p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <div class="metric-chip">
            <small>Total</small>
            <strong>{{ rows.length }}</strong>
          </div>
          <div class="metric-chip">
            <small>Con venta</small>
            <strong>{{ adjudicacionesConVenta }}</strong>
          </div>
          <router-link :to="{ name: 'ContratoPlanAhorro' }" class="btn btn-outline-secondary">Ver contratos</router-link>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-2">
          <div class="col-12 col-md-8">
            <label class="form-label mb-1">Buscar</label>
            <input v-model="searchTerm" type="text" class="form-control" placeholder="Contrato, cliente, plan o estado..." />
          </div>
          <div class="col-12 col-md-4">
            <label class="form-label mb-1">Estado</label>
            <select v-model="estadoFiltro" class="form-select">
              <option value="">Todos</option>
              <option v-for="estado in estadosDisponibles" :key="estado" :value="estado">{{ estado }}</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div v-if="loading" class="card-body text-muted">Cargando adjudicaciones...</div>
      <div v-else-if="filtradas.length === 0" class="card-body text-muted">No hay adjudicaciones para mostrar.</div>
      <div v-else class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th>Contrato</th>
              <th>Cliente</th>
              <th>Plan</th>
              <th>Estado</th>
              <th>Monto reconocido</th>
              <th>Diferencia</th>
              <th>Venta</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filtradas" :key="item.adjudicacion.id">
              <td class="fw-semibold">{{ item.adjudicacion.numeroContrato ?? '-' }}</td>
              <td>{{ item.adjudicacion.cliente?.apellido }} {{ item.adjudicacion.cliente?.nombre }}</td>
              <td>{{ item.adjudicacion.planNombre ?? '-' }}</td>
              <td>
                <span class="badge rounded-pill bg-primary">
                  {{ item.adjudicacion.estado }}
                </span>
              </td>
              <td>{{ formatMoney(item.adjudicacion.montoReconocidoCuotas) }}</td>
              <td>{{ formatMoney(item.adjudicacion.diferenciaAPagar) }}</td>
              <td>
                <router-link
                  v-if="item.adjudicacion.venta?.id"
                  :to="{ name: 'VentaView', params: { ventaId: item.adjudicacion.venta.id } }"
                  class="btn btn-sm btn-link p-0"
                >
                  #{{ item.adjudicacion.venta.id }}
                </router-link>
                <span v-else class="text-muted">-</span>
              </td>
              <td class="text-end">
                <router-link
                  :to="{ name: 'ContratoPlanAhorroView', params: { contratoId: item.adjudicacion.contratoPlanAhorroId } }"
                  class="btn btn-sm btn-outline-primary"
                >
                  Gestionar
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer d-flex justify-content-end" v-if="totalItems > itemsPerPage">
        <b-pagination v-model="page" :total-rows="totalItems" :per-page="itemsPerPage" @update:model-value="retrieve" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./adjudicacion-plan-ahorro.component.ts"></script>

<style scoped>
.adjudicaciones-page {
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

.card {
  border-radius: 14px;
}

.table thead th {
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
</style>
