<template>
  <div>
    <h2 id="page-heading" data-cy="VehiculoHeading">
      <span id="vehiculo">Vehiculos</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'VehiculoCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-vehiculo"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Vehiculo</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && vehiculos?.length === 0">
      <span>Ningún Vehiculos encontrado</span>
    </div>
    <div class="table-responsive" v-if="vehiculos?.length > 0">
      <table class="table table-striped" aria-describedby="vehiculos">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('estado')">
              <span>Estado</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'estado'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('fechaFabricacion')">
              <span>Fecha Fabricacion</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fechaFabricacion'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('km')">
              <span>Km</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'km'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('patente')">
              <span>Patente</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'patente'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('precio')">
              <span>Precio</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'precio'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('createdDate')">
              <span>Created Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('lastModifiedDate')">
              <span>Last Modified Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastModifiedDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('version.id')">
              <span>Version</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'version.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('motor.id')">
              <span>Motor</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'motor.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('tipoVehiculo.id')">
              <span>Tipo Vehiculo</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'tipoVehiculo.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="vehiculo in vehiculos" :key="vehiculo.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'VehiculoView', params: { vehiculoId: vehiculo.id } }">{{ vehiculo.id }}</router-link>
            </td>
            <td>{{ vehiculo.estado }}</td>
            <td>{{ vehiculo.fechaFabricacion }}</td>
            <td>{{ vehiculo.km }}</td>
            <td>{{ vehiculo.patente }}</td>
            <td>{{ vehiculo.precio }}</td>
            <td>{{ formatDateShort(vehiculo.createdDate) || '' }}</td>
            <td>{{ formatDateShort(vehiculo.lastModifiedDate) || '' }}</td>
            <td>
              <div v-if="vehiculo.version">
                <router-link :to="{ name: 'VersionView', params: { versionId: vehiculo.version.id } }">{{
                  vehiculo.version.id
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="vehiculo.motor">
                <router-link :to="{ name: 'MotorView', params: { motorId: vehiculo.motor.id } }">{{ vehiculo.motor.id }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="vehiculo.tipoVehiculo">
                <router-link :to="{ name: 'TipoVehiculoView', params: { tipoVehiculoId: vehiculo.tipoVehiculo.id } }">{{
                  vehiculo.tipoVehiculo.id
                }}</router-link>
              </div>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'VehiculoView', params: { vehiculoId: vehiculo.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'VehiculoEdit', params: { vehiculoId: vehiculo.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(vehiculo)"
                  variant="danger"
                  class="btn btn-sm"
                  data-cy="entityDeleteButton"
                  v-b-modal.removeEntity
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                  <span class="d-none d-md-inline">Eliminar</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <b-modal ref="removeEntity" id="removeEntity">
      <template #title>
        <span id="concesionariaApp.vehiculo.delete.question" data-cy="vehiculoDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-vehiculo-heading">¿Seguro que quiere eliminar Vehiculo {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-vehiculo"
            data-cy="entityConfirmDeleteButton"
            @click="removeVehiculo"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="vehiculos?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./vehiculo.component.ts"></script>
