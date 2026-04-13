<template>
  <div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="mb-0">Inventario</h3>
        <small class="text-muted">Control de disponibilidad, reservas y estado comercial de cada unidad.</small>
      </div>

      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" />
        </button>

        <router-link :to="{ name: 'InventarioCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="plus" />
            Nuevo Inventario
          </button>
        </router-link>
      </div>
    </div>

    <div class="card shadow-sm p-3 mb-4">
      <div class="row g-3">
        <div class="col-md-4">
          <input
            v-model="search"
            class="form-control"
            placeholder="Buscar por patente, marca, modelo, cliente o ubicación"
          />
        </div>

        <div class="col-md-2">
          <select v-model="filtroEstado" class="form-control">
            <option value="">Todos los estados</option>
            <option value="DISPONIBLE">Disponible</option>
            <option value="RESERVADO">Reservado</option>
            <option value="VENDIDO">Vendido</option>
          </select>
        </div>

        <div class="col-md-2">
          <select v-model="filtroDisponible" class="form-control">
            <option value="">Toda disponibilidad</option>
            <option value="true">Disponible</option>
            <option value="false">No disponible</option>
          </select>
        </div>

        <div class="col-md-2">
          <select v-model="filtroReserva" class="form-control">
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
    </div>

    <div class="alert alert-warning" v-if="!isFetching && filteredInventarios.length === 0">
      No hay inventarios que coincidan con los filtros aplicados.
    </div>

    <div class="card shadow-sm" v-if="filteredInventarios.length > 0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th @click="changeOrder('id')">#</th>
              <th>Unidad</th>
              <th @click="changeOrder('estadoInventario')">Estado</th>
              <th @click="changeOrder('disponible')">Disponible</th>
              <th @click="changeOrder('ubicacion')">Ubicación</th>
              <th @click="changeOrder('fechaIngreso')">Ingreso</th>
              <th>Reserva</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="inventario in filteredInventarios" :key="inventario.id">
              <td>{{ inventario.id }}</td>

              <td>
                <div class="fw-semibold">
                  <router-link :to="{ name: 'InventarioView', params: { inventarioId: inventario.id } }" class="text-decoration-none">
                    {{ vehiculoLabel(inventario) }}
                  </router-link>
                </div>
                <small class="text-muted">
                  {{ inventario.vehiculo?.version?.modelo?.marca?.nombre || '-' }}
                  {{ inventario.vehiculo?.version?.modelo?.nombre || '' }}
                </small>
              </td>

              <td>
                <span class="badge" :class="badgeEstado(inventario.estadoInventario)">
                  {{ inventario.estadoInventario || 'Sin estado' }}
                </span>
              </td>

              <td>
                <span class="badge" :class="badgeDisponible(inventario.disponible)">
                  {{ inventario.disponible ? 'Sí' : 'No' }}
                </span>
              </td>

              <td>{{ inventario.ubicacion || 'Sin definir' }}</td>
              <td>{{ formatDateShort(inventario.fechaIngreso) || '-' }}</td>

              <td>
                <div v-if="inventario.estadoInventario === 'RESERVADO'">
                  <div class="fw-semibold">{{ clienteLabel(inventario) }}</div>
                  <small :class="isReservaVencida(inventario) ? 'text-danger' : 'text-muted'">
                    {{ isReservaVencida(inventario) ? 'Reserva vencida' : `Vence ${formatDateShort(inventario.fechaVencimientoReserva) || '-'}` }}
                  </small>
                </div>
                <span v-else class="text-muted">Sin reserva</span>
              </td>

              <td class="text-end">
                <div class="btn-group">
                  <router-link :to="{ name: 'InventarioView', params: { inventarioId: inventario.id } }">
                    <button class="btn btn-sm btn-outline-info">Ver</button>
                  </router-link>

                  <router-link :to="{ name: 'InventarioEdit', params: { inventarioId: inventario.id } }">
                    <button class="btn btn-sm btn-outline-primary">Editar</button>
                  </router-link>

                  <button @click="prepareRemove(inventario)" class="btn btn-sm btn-outline-danger">
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <b-modal ref="removeEntity" id="removeEntity">
      <template #title>Confirmar eliminación</template>
      <div class="modal-body">
        ¿Eliminar inventario {{ removeId }}?
      </div>
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
