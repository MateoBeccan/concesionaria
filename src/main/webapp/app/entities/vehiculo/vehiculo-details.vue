<template>
  <div class="container py-4" style="max-width: 760px">
    <div v-if="!vehiculo.id" class="text-center py-5">
      <div class="spinner-border text-primary" />
    </div>

    <template v-else>
      <div class="page-header">
        <div class="d-flex align-items-center gap-3">
          <div>
            <h1 class="page-title mb-0">{{ vehiculo.patente || 'Sin patente asignada' }}</h1>
            <div class="d-flex gap-2 mt-1">
              <span class="badge" :class="vehiculo.estado === 'NUEVO' ? 'bg-success' : 'bg-secondary'">
                {{ vehiculo.estado === 'NUEVO' ? 'Nuevo' : 'Usado' }}
              </span>
              <span class="badge" :class="badgeCondicion(vehiculo.condicion)">
                {{ labelCondicion(vehiculo.condicion) }}
              </span>
            </div>
          </div>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-sm btn-outline-secondary" @click="previousState()">Volver</button>
          <router-link v-if="vehiculo.id" :to="{ name: 'VehiculoEdit', params: { vehiculoId: vehiculo.id } }" class="btn btn-sm btn-primary">
            Editar
          </router-link>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <div class="card border-0" style="background: var(--color-primary-light)">
            <div class="card-body py-3">
              <div class="d-flex justify-content-between align-items-center">
                <span class="text-muted small fw-semibold text-uppercase" style="letter-spacing: 0.05em">Precio de venta</span>
                <h2 class="mb-0 fw-bold" style="color: var(--color-primary)">$ {{ formatPrecio(vehiculo.precio) }}</h2>
              </div>
            </div>
          </div>
        </div>

        <div class="col-12">
          <OperationalTraceCard
            title="Trazabilidad de la unidad"
            :status="traceStatus()"
            :created-by="vehiculo.createdBy || 'No disponible'"
            :created-at="vehiculo.createdDate ? formatDateLong(vehiculo.createdDate) : 'No disponible'"
            :updated-by="vehiculo.lastModifiedBy || 'No disponible'"
            :updated-at="vehiculo.lastModifiedDate ? formatDateLong(vehiculo.lastModifiedDate) : 'No disponible'"
            :last-action="traceLastAction()"
            :last-action-at="vehiculo.lastModifiedDate ? formatDateLong(vehiculo.lastModifiedDate) : vehiculo.createdDate ? formatDateLong(vehiculo.createdDate) : 'No disponible'"
          />
        </div>

        <div class="col-md-6">
          <div class="card h-100">
            <div class="card-header">Identificacion</div>
            <div class="card-body">
              <dl class="detail-list">
                <dt>Patente</dt>
                <dd class="fw-semibold">{{ vehiculo.patente || 'Pendiente de patentamiento' }}</dd>

                <dt>Marca</dt>
                <dd>{{ vehiculo.version?.modelo?.marca?.nombre ?? '-' }}</dd>

                <dt>Modelo</dt>
                <dd>{{ vehiculo.version?.modelo?.nombre ?? '-' }}</dd>

                <dt>Version</dt>
                <dd>{{ vehiculo.version?.nombre ?? '-' }}</dd>

                <dt>Tipo</dt>
                <dd>{{ vehiculo.tipoVehiculo?.nombre ?? '-' }}</dd>

                <dt>Ano fabricacion</dt>
                <dd>{{ formatFecha(vehiculo.fechaFabricacion) }}</dd>
              </dl>
            </div>
          </div>
        </div>

        <div class="col-md-6">
          <div class="card h-100">
            <div class="card-header">Datos tecnicos</div>
            <div class="card-body">
              <dl class="detail-list">
                <dt>Kilometros</dt>
                <dd>{{ vehiculo.km?.toLocaleString('es-AR') ?? '-' }} km</dd>

                <dt>Motor</dt>
                <dd>{{ vehiculo.motor?.nombre ?? '-' }}</dd>

                <dt>Potencia</dt>
                <dd>{{ vehiculo.motor?.potenciaHp ? `${vehiculo.motor.potenciaHp} HP` : '-' }}</dd>

                <dt>Cilindrada</dt>
                <dd>{{ vehiculo.motor?.cilindradaCc ? `${vehiculo.motor.cilindradaCc} cc` : '-' }}</dd>

                <dt>Turbo</dt>
                <dd>
                  <span class="badge" :class="vehiculo.motor?.turbo ? 'bg-primary' : 'bg-light text-dark border'">
                    {{ vehiculo.motor?.turbo ? 'Si' : 'No' }}
                  </span>
                </dd>
              </dl>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script lang="ts" src="./vehiculo-details.component.ts"></script>

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
  align-self: center;
}

.detail-list dd {
  font-size: 0.88rem;
  color: var(--color-text);
  margin: 0;
  align-self: center;
}
</style>
