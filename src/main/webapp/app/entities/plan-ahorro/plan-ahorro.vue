<template>
  <div class="container-fluid px-0 planes-page">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <p class="section-kicker mb-1">Planes de ahorro</p>
          <h1 class="h4 mb-1">Catalogo de planes</h1>
          <p class="text-muted mb-0">Gestion comercial de planes vigentes y configuracion base.</p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <div class="metric-chip">
            <small>Activos</small>
            <strong>{{ totalActivos }}</strong>
          </div>
          <div class="metric-chip">
            <small>Total</small>
            <strong>{{ planes.length }}</strong>
          </div>
          <button class="btn btn-primary" @click="openCreate">Nuevo plan</button>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-2">
          <div class="col-12 col-md-8">
            <label class="form-label mb-1">Buscar</label>
            <input v-model="searchTerm" class="form-control" placeholder="Nombre, descripcion, version o moneda..." />
          </div>
          <div class="col-12 col-md-4">
            <label class="form-label mb-1">Estado</label>
            <select v-model="estadoFiltro" class="form-select">
              <option value="">Todos</option>
              <option value="ACTIVO">ACTIVO</option>
              <option value="INACTIVO">INACTIVO</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div class="table-responsive card border-0 shadow-sm">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Nombre</th>
            <th>Version objetivo</th>
            <th>Cuotas</th>
            <th>Valor movil</th>
            <th>Moneda</th>
            <th>Regla adjudicación</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="plan in planesFiltrados" :key="plan.id">
            <td>{{ plan.nombre }}</td>
            <td>{{ plan.versionObjetivo?.nombre ?? '-' }}</td>
            <td>{{ plan.cantidadCuotas }}</td>
            <td>{{ Number(plan.valorMovil ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</td>
            <td>{{ plan.moneda?.codigo }}</td>
            <td>{{ plan.reglaAdjudicacion?.nombre ?? 'SIN_REGLA_DEFAULT' }}</td>
            <td>
              <span class="badge rounded-pill" :class="plan.estado === 'ACTIVO' ? 'bg-success' : 'bg-secondary'">{{ plan.estado }}</span>
            </td>
          </tr>
          <tr v-if="planesFiltrados.length === 0">
            <td colspan="7" class="text-center text-muted py-4">No hay planes que coincidan con los filtros.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showCreate" class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Crear plan</h5>
            <button type="button" class="btn-close" @click="showCreate = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-2">
              <label class="form-label">Nombre</label>
              <input v-model="draft.nombre" class="form-control" />
            </div>
            <div class="mb-2">
              <label class="form-label">Descripcion</label>
              <textarea v-model="draft.descripcion" class="form-control" rows="2"></textarea>
            </div>
            <div class="row g-2">
              <div class="col-6">
                <label class="form-label">Cuotas</label>
                <input v-model.number="draft.cantidadCuotas" type="number" min="1" class="form-control" />
              </div>
              <div class="col-6">
                <label class="form-label">Valor movil</label>
                <input v-model.number="draft.valorMovil" type="number" min="0.01" step="0.01" class="form-control" />
              </div>
            </div>
            <div class="mt-2">
              <label class="form-label">Moneda</label>
              <select v-model.number="draft.monedaId" class="form-select">
                <option :value="null">Seleccionar</option>
                <option v-for="mon in monedas" :key="mon.id" :value="mon.id">{{ mon.codigo }} - {{ mon.descripcion }}</option>
              </select>
            </div>
            <div class="mt-2">
              <label class="form-label">Regla de adjudicación</label>
              <select v-model.number="draft.reglaAdjudicacionId" class="form-select">
                <option :value="null">Usar regla por defecto</option>
                <option v-for="regla in reglas" :key="regla.id" :value="regla.id">{{ regla.nombre }} · {{ regla.tipoRegla }}</option>
              </select>
            </div>
            <div class="alert alert-light border mt-2 mb-0" v-if="draft.reglaAdjudicacionId">
              <small class="d-block text-muted mb-1">Resumen de regla</small>
              <strong>{{ reglas.find(r => r.id === draft.reglaAdjudicacionId)?.tipoRegla }}</strong>
              <div class="small">
                Mín cuotas: {{ reglas.find(r => r.id === draft.reglaAdjudicacionId)?.minimoCuotas ?? '-' }} · Mín %:
                {{ reglas.find(r => r.id === draft.reglaAdjudicacionId)?.minimoPorcentaje ?? '-' }} · Mora:
                {{ reglas.find(r => r.id === draft.reglaAdjudicacionId)?.permiteMora ? 'Sí' : 'No' }}
              </div>
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

<script lang="ts" src="./plan-ahorro.component.ts"></script>

<style scoped>
.planes-page {
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
