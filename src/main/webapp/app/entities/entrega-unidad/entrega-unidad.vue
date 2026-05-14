<template>
  <div class="container-fluid px-0">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <p class="section-kicker mb-1">Comercial</p>
          <h1 class="h4 mb-1">Entregas de unidad</h1>
          <p class="text-muted mb-0">Gestion operativa de entregas programadas y confirmadas.</p>
        </div>
        <div class="d-flex gap-2">
          <div class="metric-chip"><small>Programadas</small><strong>{{ totals.programadas }}</strong></div>
          <div class="metric-chip"><small>Entregadas</small><strong>{{ totals.entregadas }}</strong></div>
          <div class="metric-chip"><small>Canceladas</small><strong>{{ totals.canceladas }}</strong></div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-2">
          <div class="col-12 col-md-3">
            <label class="form-label">Estado</label>
            <select v-model="filtroEstado" class="form-select">
              <option value="">Todos</option>
              <option v-for="estado in estados" :key="estado" :value="estado">{{ estado }}</option>
            </select>
          </div>
          <div class="col-12 col-md-3">
            <label class="form-label">Cliente</label>
            <input v-model="filtroCliente" class="form-control" placeholder="Apellido o nombre" />
          </div>
          <div class="col-12 col-md-2">
            <label class="form-label">Venta #</label>
            <input v-model.number="filtroVentaId" type="number" class="form-control" min="1" />
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label">Desde</label>
            <input v-model="filtroDesde" type="date" class="form-control" />
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label">Hasta</label>
            <input v-model="filtroHasta" type="date" class="form-control" />
          </div>
        </div>
        <div class="d-flex justify-content-end gap-2 mt-3">
          <button class="btn btn-outline-secondary" @click="clearFilters">Limpiar</button>
          <button class="btn btn-primary" @click="retrieve">Filtrar</button>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div v-if="loading" class="card-body text-muted">Cargando entregas...</div>
      <div v-else-if="entregas.length === 0" class="card-body text-muted">No hay entregas para mostrar.</div>
      <div v-else class="table-responsive">
        <table class="table table-sm align-middle mb-0">
          <thead>
            <tr>
              <th>Venta</th>
              <th>Cliente</th>
              <th>Unidad</th>
              <th>Estado</th>
              <th>Fecha programada</th>
              <th>Fecha entrega</th>
              <th>Usuario</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="entrega in entregas" :key="entrega.id">
              <td>#{{ entrega.venta?.id }}</td>
              <td>{{ entrega.cliente?.apellido }} {{ entrega.cliente?.nombre }}</td>
              <td>{{ entrega.vehiculo?.patente ?? '-' }}</td>
              <td><span class="badge bg-light text-dark border">{{ entrega.estado }}</span></td>
              <td>{{ formatDate(entrega.fechaProgramada) }}</td>
              <td>{{ formatDate(entrega.fechaEntrega) }}</td>
              <td>{{ entrega.usuarioProgramacion ?? '-' }}</td>
              <td class="text-end">
                <router-link :to="{ name: 'VentaView', params: { ventaId: entrega.venta?.id } }" class="btn btn-sm btn-outline-primary">Ver venta</router-link>
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

<script lang="ts" src="./entrega-unidad.component.ts"></script>

<style scoped>
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
}
.metric-chip strong {
  font-size: 1rem;
  color: #0f172a;
}
</style>

