<template>
  <div class="d-flex justify-content-center">
    <div class="col-10 col-lg-8">
      <div v-if="cotizacion" class="card shadow-sm p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 class="mb-0">Cotizacion #{{ cotizacion.id }}</h3>
            <small class="text-muted">{{ cotizacion.fecha ? formatDateLong(cotizacion.fecha) : 'Sin fecha' }}</small>
          </div>
          <span class="badge" :class="cotizacion.activo ? 'bg-success' : 'bg-secondary'">
            {{ cotizacion.activo ? 'Activa' : 'Inactiva' }}
          </span>
        </div>

        <h5 class="border-bottom pb-2 mb-3">Valores</h5>
        <div class="row g-3 mb-3">
          <div class="col-md-4">
            <strong>Valor compra:</strong>
            <div>{{ cotizacion.valorCompra ?? '-' }}</div>
          </div>
          <div class="col-md-4">
            <strong>Valor venta:</strong>
            <div>{{ cotizacion.valorVenta ?? '-' }}</div>
          </div>
          <div class="col-md-4">
            <strong>Moneda:</strong>
            <div v-if="cotizacion.moneda">
              <router-link :to="{ name: 'MonedaView', params: { monedaId: cotizacion.moneda.id } }">
                {{ cotizacion.moneda.codigo || cotizacion.moneda.id }}
              </router-link>
            </div>
            <div v-else>-</div>
          </div>
        </div>

        <h5 class="border-bottom pb-2 mb-3">Auditoria</h5>
        <div class="row g-3">
          <div class="col-md-3">
            <strong>Creado:</strong>
            <div>{{ cotizacion.createdDate ? formatDateLong(cotizacion.createdDate) : '-' }}</div>
          </div>
          <div class="col-md-3">
            <strong>Creado por:</strong>
            <div>{{ cotizacion.createdBy || '-' }}</div>
          </div>
          <div class="col-md-3">
            <strong>Modificado:</strong>
            <div>{{ cotizacion.lastModifiedDate ? formatDateLong(cotizacion.lastModifiedDate) : '-' }}</div>
          </div>
          <div class="col-md-3">
            <strong>Modificado por:</strong>
            <div>{{ cotizacion.lastModifiedBy || '-' }}</div>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="button" @click.prevent="previousState()" class="btn btn-outline-secondary">
            <font-awesome-icon icon="arrow-left" />
            Volver
          </button>
          <router-link v-if="cotizacion.id" :to="{ name: 'CotizacionEdit', params: { cotizacionId: cotizacion.id } }" custom v-slot="{ navigate }">
            <button @click="navigate" class="btn btn-primary">
              <font-awesome-icon icon="pencil-alt" />
              Editar
            </button>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./cotizacion-details.component.ts"></script>
