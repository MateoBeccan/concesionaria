<template>
  <div>
    <h2 id="page-heading" data-cy="VehiculoHeading">
      <span id="vehiculo">Vehiculos</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span>Refrescar lista</span>
        </button>

        <router-link :to="{ name: 'VehiculoCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Vehiculo</span>
          </button>
        </router-link>
      </div>
    </h2>

    <br />

    <div class="alert alert-warning" v-if="!isFetching && vehiculos?.length === 0">
      <span>Ningún Vehiculo encontrado</span>
    </div>

    <div class="table-responsive" v-if="vehiculos?.length > 0">
      <table class="table table-striped">
        <thead>
          <tr>
            <th @click="changeOrder('estado')">Estado</th>
            <th @click="changeOrder('fechaFabricacion')">Fecha</th>
            <th @click="changeOrder('km')">Km</th>
            <th @click="changeOrder('patente')">Patente</th>
            <th @click="changeOrder('precio')">Precio</th>
            <th>Versión</th>
            <th>Motor</th>
            <th>Tipo</th>
            <th></th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="vehiculo in vehiculos" :key="vehiculo.id">
            <!-- ESTADO -->
            <td>
              <span
                class="badge"
                :class="{
                  'bg-secondary': vehiculo.estado === 'USADO',
                  'bg-success': vehiculo.estado === 'NUEVO'
                }"
              >
                {{ vehiculo.estado }}
              </span>
            </td>

            <td>{{ vehiculo.fechaFabricacion }}</td>
            <td>{{ vehiculo.km }}</td>

            <!-- 🔥 PATENTE COMO IDENTIFICADOR -->
            <td>
              <router-link :to="{ name: 'VehiculoView', params: { vehiculoId: vehiculo.id } }">
                {{ vehiculo.patente }}
              </router-link>
            </td>

            <td>{{ vehiculo.precio }}</td>

            <!-- VERSION -->
            <td>
              <span v-if="vehiculo.version">
                {{ vehiculo.version.nombre }}
              </span>
            </td>

            <!--MOTOR -->
            <td>
              <span v-if="vehiculo.motor">
                {{ vehiculo.motor.nombre }} ({{ vehiculo.motor?.potenciaHp ?? '-' }}hp)
              </span>
            </td>

            <!--TIPO -->
            <td>
              <span v-if="vehiculo.tipoVehiculo">
                {{ vehiculo.tipoVehiculo.nombre }}
              </span>
            </td>

            <!--ACCIONES -->
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'VehiculoView', params: { vehiculoId: vehiculo.id } }">
                  <button class="btn btn-info btn-sm">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                  </button>
                </router-link>

                <router-link :to="{ name: 'VehiculoEdit', params: { vehiculoId: vehiculo.id } }">
                  <button class="btn btn-primary btn-sm">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                  </button>
                </router-link>

                <b-button
                  @click="prepareRemove(vehiculo)"
                  variant="danger"
                  class="btn btn-sm"
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- MODAL -->
    <b-modal ref="removeEntity">
      <template #title>Confirmar eliminación</template>
      <div class="modal-body">
        ¿Eliminar vehículo?
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
        <button class="btn btn-danger" @click="removeVehiculo">Eliminar</button>
      </template>
    </b-modal>

    <!-- PAGINACION -->
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
