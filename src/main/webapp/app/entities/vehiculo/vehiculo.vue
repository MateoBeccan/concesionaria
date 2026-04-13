<template>
  <div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="mb-0">Vehículos</h3>
        <small class="text-muted">Catálogo comercial y operativo de unidades disponibles, reservadas y vendidas.</small>
      </div>

      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" />
        </button>

        <router-link :to="{ name: 'VehiculoCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="plus" />
            Nuevo Vehículo
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
            placeholder="Buscar por patente, marca, modelo, versión o motor"
          />
        </div>

        <div class="col-md-2">
          <select v-model="filtroEstado" class="form-control">
            <option value="">Todos los estados</option>
            <option value="NUEVO">Nuevo</option>
            <option value="USADO">Usado</option>
          </select>
        </div>

        <div class="col-md-2">
          <select v-model="filtroCondicion" class="form-control">
            <option value="">Todas las condiciones</option>
            <option value="EN_VENTA">En venta</option>
            <option value="RESERVADO">Reservado</option>
            <option value="VENDIDO">Vendido</option>
          </select>
        </div>

        <div class="col-md-2">
          <select v-model="filtroTipo" class="form-control">
            <option value="">Todos los tipos</option>
            <option v-for="tipo in tipoVehiculoOptions" :key="tipo" :value="tipo">{{ tipo }}</option>
          </select>
        </div>

        <div class="col-md-2 d-grid">
          <button class="btn btn-outline-secondary" @click="resetFiltros">Limpiar</button>
        </div>
      </div>
    </div>

    <div class="alert alert-warning" v-if="!isFetching && filteredVehiculos.length === 0">
      No hay vehículos que coincidan con los filtros aplicados.
    </div>

    <div class="card shadow-sm" v-if="filteredVehiculos.length > 0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th @click="changeOrder('id')">#</th>
              <th @click="changeOrder('patente')">Unidad</th>
              <th @click="changeOrder('estado')">Estado</th>
              <th @click="changeOrder('condicion')">Condición</th>
              <th>Catálogo</th>
              <th @click="changeOrder('km')">Km</th>
              <th @click="changeOrder('precio')">Precio</th>
              <th @click="changeOrder('fechaFabricacion')">Fabricación</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="vehiculo in filteredVehiculos" :key="vehiculo.id">
              <td>{{ vehiculo.id }}</td>

              <td>
                <div class="fw-semibold">
                  <router-link :to="{ name: 'VehiculoView', params: { vehiculoId: vehiculo.id } }" class="text-decoration-none">
                    {{ vehiculo.patente || 'Sin patente asignada' }}
                  </router-link>
                </div>
                <small class="text-muted">{{ vehiculo.tipoVehiculo?.nombre || 'Tipo sin definir' }}</small>
              </td>

              <td>
                <span class="badge" :class="badgeEstado(vehiculo.estado)">
                  {{ vehiculo.estado || 'Sin estado' }}
                </span>
              </td>

              <td>
                <span class="badge" :class="badgeCondicion(vehiculo.condicion)">
                  {{ vehiculo.condicion || 'Sin condición' }}
                </span>
              </td>

              <td>
                <div class="fw-semibold">
                  {{ vehiculo.version?.modelo?.marca?.nombre || '-' }} {{ vehiculo.version?.modelo?.nombre || '' }}
                </div>
                <small class="text-muted">
                  {{ vehiculo.version?.nombre || 'Sin versión' }}
                  <span v-if="vehiculo.motor?.nombre"> · {{ vehiculo.motor.nombre }}</span>
                </small>
              </td>

              <td>{{ vehiculo.km?.toLocaleString('es-AR') ?? '-' }}</td>
              <td>{{ formatPrecio(vehiculo.precio) }}</td>
              <td>{{ formatDateShort(vehiculo.fechaFabricacion) || '-' }}</td>

              <td class="text-end">
                <div class="btn-group">
                  <router-link :to="{ name: 'VehiculoView', params: { vehiculoId: vehiculo.id } }">
                    <button class="btn btn-sm btn-outline-info">Ver</button>
                  </router-link>

                  <router-link :to="{ name: 'VehiculoEdit', params: { vehiculoId: vehiculo.id } }">
                    <button class="btn btn-sm btn-outline-primary">Editar</button>
                  </router-link>

                  <button @click="prepareRemove(vehiculo)" class="btn btn-sm btn-outline-danger">
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <b-modal ref="removeEntity">
      <template #title>Confirmar eliminación</template>
      <div class="modal-body">¿Eliminar vehículo?</div>
      <template #footer>
        <button class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
        <button class="btn btn-danger" @click="removeVehiculo">Eliminar</button>
      </template>
    </b-modal>

    <div v-show="vehiculos?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination v-model="page" :total-rows="totalItems" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./vehiculo.component.ts"></script>
