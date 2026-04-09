<template>
  <div class="container py-4">

    <div class="page-header">
      <div>
        <h1 class="page-title">Detalles de venta</h1>
        <p class="page-subtitle">Vehículos asociados a operaciones de venta</p>
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-sm btn-outline-secondary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" /> Refrescar
        </button>
        <router-link :to="{ name: 'DetalleVentaCreate' }" class="btn btn-primary btn-sm">
          + Nuevo detalle
        </router-link>
      </div>
    </div>

    <!-- EMPTY -->
    <div v-if="!isFetching && detalleVentas?.length === 0" class="text-center py-5">
      <div style="font-size:2.5rem">📋</div>
      <p class="text-muted mt-2">No hay detalles de venta registrados</p>
    </div>

    <!-- TABLA -->
    <div class="card border-0 shadow-sm" v-if="detalleVentas?.length > 0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th @click="changeOrder('id')" style="cursor:pointer">
                # <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="id" />
              </th>
              <th>Venta</th>
              <th>Vehículo</th>
              <th>Marca / Modelo</th>
              <th @click="changeOrder('precioUnitario')" style="cursor:pointer" class="text-end">
                Precio <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="precioUnitario" />
              </th>
              <th @click="changeOrder('subtotal')" style="cursor:pointer" class="text-end">
                Subtotal <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="subtotal" />
              </th>
              <th class="text-end pe-4">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in detalleVentas" :key="d.id">
              <td class="text-muted small ps-4">{{ d.id }}</td>
              <td>
                <router-link v-if="d.venta" :to="{ name: 'VentaView', params: { ventaId: d.venta.id } }">
                  Venta #{{ d.venta.id }}
                </router-link>
                <span v-else class="text-muted">—</span>
              </td>
              <td>
                <router-link v-if="d.vehiculo" :to="{ name: 'VehiculoView', params: { vehiculoId: d.vehiculo.id } }" class="fw-semibold">
                  {{ d.vehiculo.patente }}
                </router-link>
                <span v-else class="text-muted">—</span>
              </td>
              <td class="text-muted small">
                {{ d.vehiculo?.version?.modelo?.marca?.nombre ?? '' }}
                {{ d.vehiculo?.version?.modelo?.nombre ?? '' }}
                {{ d.vehiculo?.version?.nombre ?? '' }}
              </td>
              <td class="text-end">$ {{ formatPrecio(d.precioUnitario) }}</td>
              <td class="text-end fw-semibold" style="color:var(--color-primary)">$ {{ formatPrecio(d.subtotal) }}</td>
              <td class="text-end pe-4">
                <div class="btn-group">
                  <router-link :to="{ name: 'DetalleVentaView', params: { detalleVentaId: d.id } }" custom v-slot="{ navigate }">
                    <button @click="navigate" class="btn btn-sm btn-outline-secondary">Ver</button>
                  </router-link>
                  <router-link :to="{ name: 'DetalleVentaEdit', params: { detalleVentaId: d.id } }" custom v-slot="{ navigate }">
                    <button @click="navigate" class="btn btn-sm btn-outline-primary">Editar</button>
                  </router-link>
                  <b-button @click="prepareRemove(d)" variant="danger" class="btn btn-sm" v-b-modal.removeEntity>
                    Eliminar
                  </b-button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- PAGINACIÓN -->
    <div v-show="detalleVentas?.length > 0" class="mt-3">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage" />
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" />
      </div>
    </div>

    <!-- MODAL ELIMINAR -->
    <b-modal ref="removeEntity" id="removeEntity">
      <template #title>Confirmar eliminación</template>
      <div class="modal-body">
        <p>¿Seguro que desea eliminar el detalle de venta #{{ removeId }}?</p>
      </div>
      <template #footer>
        <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
        <button type="button" class="btn btn-danger" @click="removeDetalleVenta">Eliminar</button>
      </template>
    </b-modal>

  </div>
</template>

<script lang="ts" src="./detalle-venta.component.ts"></script>
