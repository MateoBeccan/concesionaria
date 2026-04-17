<template>
  <div class="container py-4" style="max-width: 860px">
    <div v-if="!inventario.id" class="text-center py-5">
      <div class="spinner-border text-primary" />
    </div>

    <template v-else>
      <div class="page-header">
        <div>
          <h1 class="page-title mb-0">{{ inventario.vehiculo?.patente || `Inventario ${inventario.id}` }}</h1>
          <p class="page-subtitle">Estado real de disponibilidad, reservas y coherencia comercial de la unidad.</p>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-sm btn-outline-secondary" @click="previousState()">Volver</button>
          <router-link :to="{ name: 'InventarioEdit', params: { inventarioId: inventario.id } }" class="btn btn-sm btn-primary">
            Editar
          </router-link>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <div class="card border-0" style="background: var(--color-primary-light)">
            <div class="card-body py-3 d-flex flex-wrap justify-content-between align-items-center gap-2">
              <div>
                <div class="text-muted small text-uppercase fw-semibold">Estado operativo</div>
                <div class="fw-bold fs-5">{{ inventario.estadoInventario }}</div>
              </div>
              <div class="d-flex gap-2">
                <span class="badge" :class="inventario.estadoInventario === 'DISPONIBLE' ? 'bg-success' : 'bg-secondary'">
                  {{ inventario.estadoInventario === 'DISPONIBLE' ? 'Disponible' : 'No disponible' }}
                </span>
                <span class="badge bg-dark">{{ inventario.vehiculo?.condicion ?? 'Sin condición' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="col-12">
          <OperationalTraceCard
            title="Trazabilidad del inventario"
            :status="traceStatus"
            :created-by="inventario.createdBy || 'No disponible'"
            :created-at="inventario.createdDate ? formatDateLong(inventario.createdDate) : 'No disponible'"
            :updated-by="inventario.lastModifiedBy || 'No disponible'"
            :updated-at="inventario.lastModifiedDate ? formatDateLong(inventario.lastModifiedDate) : 'No disponible'"
            :last-action="traceLastAction"
            :last-action-at="traceLastActionAt"
          />
        </div>

        <div v-if="incoherencias.length" class="col-12">
          <div class="alert alert-danger mb-0">
            <div class="fw-semibold mb-1">Se detectaron incoherencias</div>
            <div v-for="item in incoherencias" :key="item">{{ item }}</div>
          </div>
        </div>

        <div v-else-if="reservaVencida" class="col-12">
          <div class="alert alert-warning mb-0">
            La reserva está vencida. Revisá si corresponde liberarla o renovarla.
          </div>
        </div>

        <div class="col-md-6">
          <div class="card h-100">
            <div class="card-header">Unidad asociada</div>
            <div class="card-body">
              <dl class="detail-list">
                <dt>Vehículo</dt>
                <dd>
                  <router-link v-if="inventario.vehiculo?.id" :to="{ name: 'VehiculoView', params: { vehiculoId: inventario.vehiculo.id } }">
                    {{ inventario.vehiculo?.patente || `Vehículo ${inventario.vehiculo?.id}` }}
                  </router-link>
                </dd>

                <dt>Condición comercial</dt>
                <dd>{{ inventario.vehiculo?.condicion ?? 'Sin dato' }}</dd>

                <dt>Fecha de ingreso</dt>
                <dd>{{ inventario.fechaIngreso ? formatDateLong(inventario.fechaIngreso) : 'Sin dato' }}</dd>

                <dt>Ubicación</dt>
                <dd>{{ inventario.ubicacion || 'Sin definir' }}</dd>
              </dl>
            </div>
          </div>
        </div>

        <div class="col-md-6">
          <div class="card h-100">
            <div class="card-header">Reserva actual</div>
            <div class="card-body">
              <dl class="detail-list">
                <dt>Cliente</dt>
                <dd>
                  <router-link
                    v-if="inventario.clienteReserva?.id"
                    :to="{ name: 'ClienteView', params: { clienteId: inventario.clienteReserva.id } }"
                  >
                    {{ `${inventario.clienteReserva?.apellido ?? ''}, ${inventario.clienteReserva?.nombre ?? ''}`.replace(/^, |, $/g, '') }}
                  </router-link>
                  <span v-else>Sin reserva</span>
                </dd>

                <dt>Desde</dt>
                <dd>{{ inventario.fechaReserva ? formatDateLong(inventario.fechaReserva) : 'Sin dato' }}</dd>

                <dt>Vence</dt>
                <dd>{{ inventario.fechaVencimientoReserva ? formatDateLong(inventario.fechaVencimientoReserva) : 'Sin dato' }}</dd>

                <dt>Estado</dt>
                <dd>{{ reservaVencida ? 'Vencida' : inventario.estadoInventario === 'RESERVADO' ? 'Activa' : 'Sin reserva' }}</dd>
              </dl>
            </div>
          </div>
        </div>

        <div class="col-12">
          <div class="card">
            <div class="card-header">Observaciones</div>
            <div class="card-body">
              {{ inventario.observaciones || 'Sin observaciones registradas.' }}
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script lang="ts" src="./inventario-details.component.ts"></script>

<style scoped>
.detail-list {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.35rem 1rem;
  margin: 0;
}

.detail-list dt {
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.detail-list dd {
  margin: 0;
}
</style>
