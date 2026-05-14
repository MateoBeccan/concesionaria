<template>
  <div class="container-fluid px-0 contratos-page">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <p class="section-kicker mb-1">Planes de ahorro</p>
          <h1 class="h4 mb-1">Contratos de plan</h1>
          <p class="text-muted mb-0">Alta y seguimiento operativo de contratos vigentes.</p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <div class="metric-chip">
            <small>Activos</small>
            <strong>{{ contratosActivos }}</strong>
          </div>
          <div class="metric-chip">
            <small>Total</small>
            <strong>{{ contratos.length }}</strong>
          </div>
          <button class="btn btn-primary" @click="openCreate">Nuevo contrato</button>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-2">
          <div class="col-12 col-md-8">
            <label class="form-label mb-1">Buscar</label>
            <input v-model="searchTerm" type="text" class="form-control" placeholder="Numero, cliente, plan o estado..." />
          </div>
          <div class="col-12 col-md-4">
            <label class="form-label mb-1">Estado</label>
            <select v-model="estadoFiltro" class="form-select">
              <option value="">Todos</option>
              <option value="ACTIVO">ACTIVO</option>
              <option value="EN_MORA">EN_MORA</option>
              <option value="ADJUDICADO">ADJUDICADO</option>
              <option value="ENTREGADO">ENTREGADO</option>
              <option value="CANCELADO">CANCELADO</option>
              <option value="FINALIZADO">FINALIZADO</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div class="table-responsive card border-0 shadow-sm">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Numero</th>
            <th>Cliente</th>
            <th>Plan</th>
            <th>Estado</th>
            <th>Pagadas</th>
            <th>Saldo pendiente</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in contratosFiltrados" :key="c.id">
            <td class="fw-semibold">{{ c.numeroContrato }}</td>
            <td>{{ c.cliente?.apellido }} {{ c.cliente?.nombre }}</td>
            <td>{{ c.plan?.nombre }}</td>
            <td><span class="badge rounded-pill bg-primary">{{ c.estado }}</span></td>
            <td>{{ c.cuotasPagadas }}/{{ c.cuotasTotales }}</td>
            <td>{{ Number(c.saldoPendiente ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</td>
            <td class="text-end">
              <router-link :to="{ name: 'ContratoPlanAhorroView', params: { contratoId: c.id } }" class="btn btn-sm btn-outline-primary">
                Ver detalle
              </router-link>
            </td>
          </tr>
          <tr v-if="contratosFiltrados.length === 0">
            <td colspan="7" class="text-center text-muted py-4">No hay contratos que coincidan con los filtros.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showCreate" class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Crear contrato</h5>
            <button type="button" class="btn-close" @click="showCreate = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-2">
              <label class="form-label">Cliente</label>
              <select v-model.number="draft.clienteId" class="form-select">
                <option :value="null">Seleccionar</option>
                <option v-for="cli in clientes" :key="cli.id" :value="cli.id">{{ cli.apellido }} {{ cli.nombre }}</option>
              </select>
            </div>
            <div class="mb-2">
              <label class="form-label">Plan</label>
              <select v-model.number="draft.planId" class="form-select">
                <option :value="null">Seleccionar</option>
                <option v-for="plan in planes" :key="plan.id" :value="plan.id">{{ plan.nombre }}</option>
              </select>
            </div>
            <div class="mb-2">
              <label class="form-label">Observaciones</label>
              <textarea v-model="draft.observaciones" class="form-control" rows="2"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-outline-secondary" @click="showCreate = false">Cancelar</button>
            <button class="btn btn-primary" @click="save">Guardar</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showCreate" class="modal-backdrop fade show"></div>
  </div>
</template>

<script lang="ts" src="./contrato-plan-ahorro.component.ts"></script>

<style scoped>
.contratos-page {
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
