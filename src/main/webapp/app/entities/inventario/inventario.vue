<template>
  <div class="inventory-page">
    <section class="page-head card-shell">
      <div>
        <p class="page-eyebrow">Stock operativo</p>
        <h2 class="page-title">Inventario</h2>
        <p class="page-copy">Controla disponibilidad, reservas, vencimientos y acceso directo a la operacion de cada unidad.</p>
      </div>

      <div class="page-actions">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" />
        </button>

        <router-link :to="{ name: 'InventarioCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="plus" />
            Nuevo inventario
          </button>
        </router-link>
      </div>
    </section>

    <section class="summary-grid">
      <article class="summary-card tone-available">
        <span class="summary-label">Disponibles</span>
        <strong class="summary-value">{{ inventorySummary.disponible }}</strong>
        <small class="summary-copy">Listos para vender</small>
      </article>
      <article class="summary-card tone-reserved">
        <span class="summary-label">Reservados</span>
        <strong class="summary-value">{{ inventorySummary.reservado }}</strong>
        <small class="summary-copy">Con seguimiento activo</small>
      </article>
      <article class="summary-card tone-sold">
        <span class="summary-label">Vendidos</span>
        <strong class="summary-value">{{ inventorySummary.vendido }}</strong>
        <small class="summary-copy">Ya cerrados</small>
      </article>
      <article class="summary-card tone-expired">
        <span class="summary-label">Vencidas</span>
        <strong class="summary-value">{{ inventorySummary.vencidas }}</strong>
        <small class="summary-copy">Reservas a revisar</small>
      </article>
    </section>

    <section class="card-shell filter-card">
      <div class="row g-3">
        <div class="col-md-4">
          <input v-model="search" class="form-control" placeholder="Buscar por patente, marca, modelo, cliente o ubicacion" />
        </div>

        <div class="col-md-2">
          <select v-model="filtroEstado" class="form-select">
            <option value="">Todos los estados</option>
            <option value="DISPONIBLE">Disponible</option>
            <option value="RESERVADO">Reservado</option>
            <option value="VENDIDO">Vendido</option>
          </select>
        </div>

        <div class="col-md-2">
          <select v-model="filtroReserva" class="form-select">
            <option value="">Todas las reservas</option>
            <option value="activas">Reservas activas</option>
            <option value="vencidas">Reservas vencidas</option>
            <option value="sin-reserva">Sin reserva</option>
          </select>
        </div>

        <div class="col-md-2 d-grid">
          <button class="btn btn-outline-secondary" @click="resetFiltros">Limpiar</button>
        </div>
      </div>
    </section>

    <div class="alert alert-warning" v-if="!isFetching && filteredInventarios.length === 0">No hay inventarios que coincidan con los filtros aplicados.</div>

    <section class="card-shell table-card" v-if="filteredInventarios.length > 0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 inventory-table">
          <thead>
            <tr>
              <th @click="changeOrder('id')">#</th>
              <th>Unidad</th>
              <th @click="changeOrder('estadoInventario')">Estado</th>
              <th>Disponibilidad</th>
              <th @click="changeOrder('ubicacion')">Ubicacion</th>
              <th @click="changeOrder('fechaIngreso')">Ingreso</th>
              <th>Reserva</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="inventario in filteredInventarios" :key="inventario.id">
              <td>{{ inventario.id }}</td>

              <td>
                <div class="cell-main">
                  <router-link :to="{ name: 'InventarioView', params: { inventarioId: inventario.id } }" class="cell-title">
                    {{ vehiculoLabel(inventario) }}
                  </router-link>
                  <small class="cell-copy">
                    {{ inventario.vehiculo?.version?.modelo?.marca?.nombre || '-' }}
                    {{ inventario.vehiculo?.version?.modelo?.nombre || '' }}
                  </small>
                </div>
              </td>

              <td>
                <span class="badge" :class="badgeEstado(inventario.estadoInventario)">
                  {{ inventario.estadoInventario || 'Sin estado' }}
                </span>
              </td>

              <td>
                <span class="badge" :class="badgeDisponibilidad(inventario.estadoInventario)">
                  {{ inventario.estadoInventario === 'DISPONIBLE' ? 'Disponible' : 'No disponible' }}
                </span>
              </td>

              <td>{{ inventario.ubicacion || 'Sin definir' }}</td>
              <td>{{ formatDateShort(inventario.fechaIngreso) || '-' }}</td>

              <td>
                <div v-if="inventario.estadoInventario === 'RESERVADO'" class="cell-main">
                  <span class="cell-title text-body">{{ clienteLabel(inventario) }}</span>
                  <small :class="isReservaVencida(inventario) ? 'text-danger' : 'cell-copy'">
                    {{ isReservaVencida(inventario) ? 'Reserva vencida' : `Vence ${formatDateShort(inventario.fechaVencimientoReserva) || '-'}` }}
                  </small>
                </div>
                <span v-else class="text-muted">Sin reserva</span>
              </td>

              <td class="text-end">
                <div class="action-stack">
                  <router-link :to="{ name: 'InventarioView', params: { inventarioId: inventario.id } }" class="btn btn-sm btn-outline-info">Ver</router-link>
                  <router-link :to="{ name: 'InventarioEdit', params: { inventarioId: inventario.id } }" class="btn btn-sm btn-outline-primary">Editar</router-link>
                  <router-link
                    v-if="inventario.vehiculo?.id"
                    :to="{ name: 'VehiculoView', params: { vehiculoId: inventario.vehiculo.id } }"
                    class="btn btn-sm btn-outline-secondary"
                  >
                    Unidad
                  </router-link>
                  <router-link
                    v-if="inventario.vehiculo?.id && inventario.estadoInventario !== 'VENDIDO'"
                    :to="{ name: 'VentaEditor', query: { vehiculoId: inventario.vehiculo.id } }"
                    class="btn btn-sm btn-success"
                  >
                    Vender
                  </router-link>
                  <button @click="prepareRemove(inventario)" class="btn btn-sm btn-outline-danger">Eliminar</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <b-modal ref="removeEntity" id="removeEntity">
      <template #title>Confirmar eliminacion</template>
      <div class="modal-body">¿Eliminar inventario {{ removeId }}?</div>
      <template #footer>
        <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
        <button type="button" class="btn btn-danger" @click="removeInventario">Eliminar</button>
      </template>
    </b-modal>

    <div v-show="inventarios?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./inventario.component.ts"></script>

<style scoped>
.inventory-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.card-shell {
  padding: 1.1rem 1.2rem;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
}

.page-eyebrow {
  margin: 0 0 0.2rem;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #0284c7;
}

.page-title {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  color: #0f172a;
}

.page-copy {
  margin: 0.35rem 0 0;
  color: #64748b;
}

.page-actions {
  display: flex;
  gap: 0.6rem;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.85rem;
}

.summary-card {
  padding: 1rem 1.1rem;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: #fff;
}

.summary-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #64748b;
}

.summary-value {
  display: block;
  margin-top: 0.35rem;
  font-size: 1.6rem;
  color: #0f172a;
}

.summary-copy {
  color: #64748b;
}

.tone-available {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.tone-reserved {
  border-color: #fde68a;
  background: #fffbeb;
}

.tone-sold {
  border-color: #fecaca;
  background: #fef2f2;
}

.tone-expired {
  border-color: #fed7aa;
  background: #fff7ed;
}

.inventory-table thead th {
  font-size: 0.72rem;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #64748b;
  background: #f8fafc;
}

.cell-main {
  display: flex;
  flex-direction: column;
}

.cell-title {
  font-weight: 600;
  color: #0f172a;
  text-decoration: none;
}

.cell-copy {
  color: #64748b;
}

.action-stack {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 0.4rem;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 767px) {
  .page-head {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .page-actions {
    width: 100%;
  }
}
</style>
