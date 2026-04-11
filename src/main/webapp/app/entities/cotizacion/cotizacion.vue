<template>
  <div class="container-fluid">

    <!-- HEADER -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="mb-0">Cotizaciones</h3>
        <small class="text-muted">Gestión de valores de moneda</small>
      </div>

      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" />
        </button>

        <router-link :to="{ name: 'CotizacionCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="plus" />
            Nueva cotización
          </button>
        </router-link>
      </div>
    </div>

    <!-- EMPTY -->
    <div class="alert alert-warning" v-if="!isFetching && cotizacions?.length === 0">
      No hay cotizaciones registradas
    </div>

    <!-- TABLE -->
    <div class="card shadow-sm" v-if="cotizacions?.length > 0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">

          <thead class="table-light">
            <tr>
              <th>#</th>
              <th>Fecha</th>
              <th>Moneda</th>
              <th>Compra</th>
              <th>Venta</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="cotizacion in cotizacions" :key="cotizacion.id">

              <!-- ID -->
              <td>{{ cotizacion.id }}</td>

              <!-- FECHA -->
              <td>{{ formatDateShort(cotizacion.fecha) }}</td>

              <!-- MONEDA -->
              <td>
                <span class="fw-semibold">
                  {{ cotizacion.moneda?.codigo || cotizacion.moneda?.id }}
                </span>
              </td>

              <!-- COMPRA -->
              <td>
                <span class="text-muted">
                  $ {{ cotizacion.valorCompra }}
                </span>
              </td>

              <!-- VENTA (DESTACADO) -->
              <td>
                <span class="fw-bold text-success">
                  $ {{ cotizacion.valorVenta }}
                </span>
              </td>

              <!-- ESTADO -->
              <td>
                <span
                  class="badge"
                  :class="cotizacion.activo ? 'bg-success' : 'bg-secondary'"
                >
                  {{ cotizacion.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </td>

              <!-- ACCIONES -->
              <td class="text-end">
                <div class="btn-group">

                  <router-link :to="{ name: 'CotizacionView', params: { cotizacionId: cotizacion.id } }">
                    <button class="btn btn-sm btn-outline-info">
                      <font-awesome-icon icon="eye" />
                    </button>
                  </router-link>

                  <router-link :to="{ name: 'CotizacionEdit', params: { cotizacionId: cotizacion.id } }">
                    <button class="btn btn-sm btn-outline-primary">
                      <font-awesome-icon icon="pencil-alt" />
                    </button>
                  </router-link>

                  <button
                    @click="prepareRemove(cotizacion)"
                    class="btn btn-sm btn-outline-danger"
                    v-b-modal.removeEntity
                  >
                    <font-awesome-icon icon="trash" />
                  </button>

                </div>
              </td>

            </tr>
          </tbody>

        </table>
      </div>
    </div>

    <!-- MODAL -->
    <b-modal ref="removeEntity" id="removeEntity">
      <template #title>Confirmar eliminación</template>
      <div>¿Eliminar cotización {{ removeId }}?</div>
      <template #footer>
        <button class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
        <button class="btn btn-danger" @click="removeCotizacion">Eliminar</button>
      </template>
    </b-modal>

    <!-- PAGINACIÓN -->
    <div v-if="cotizacions?.length > 0" class="mt-4">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage" />
      </div>

      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" />
      </div>
    </div>

  </div>
</template>

<script lang="ts" src="./cotizacion.component.ts"></script>
