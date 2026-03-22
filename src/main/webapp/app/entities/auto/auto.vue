<template>
  <div>
    <h2 id="page-heading" data-cy="AutoHeading">
      <span id="auto">Autos</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'AutoCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" id="jh-create-entity" data-cy="entityCreateButton" class="btn btn-primary jh-create-entity create-auto">
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Auto</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && autos?.length === 0">
      <span>Ningún Autos encontrado</span>
    </div>
    <div class="table-responsive" v-if="autos?.length > 0">
      <table class="table table-striped" aria-describedby="autos">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('estado')">
              <span>Estado</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'estado'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('condicion')">
              <span>Condicion</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'condicion'"></jhi-sort-indicator>
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
            <th scope="col" @click="changeOrder('marca.id')">
              <span>Marca</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'marca.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('modelo.id')">
              <span>Modelo</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'modelo.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('version.id')">
              <span>Version</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'version.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('motor.id')">
              <span>Motor</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'motor.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="auto in autos" :key="auto.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'AutoView', params: { autoId: auto.id } }">{{ auto.id }}</router-link>
            </td>
            <td>{{ auto.estado }}</td>
            <td>{{ auto.condicion }}</td>
            <td>{{ auto.fechaFabricacion }}</td>
            <td>{{ auto.km }}</td>
            <td>{{ auto.patente }}</td>
            <td>{{ auto.precio }}</td>
            <td>
              <div v-if="auto.marca">
                <router-link :to="{ name: 'MarcaView', params: { marcaId: auto.marca.id } }">{{ auto.marca.id }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="auto.modelo">
                <router-link :to="{ name: 'ModeloView', params: { modeloId: auto.modelo.id } }">{{ auto.modelo.id }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="auto.version">
                <router-link :to="{ name: 'VersionView', params: { versionId: auto.version.id } }">{{ auto.version.id }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="auto.motor">
                <router-link :to="{ name: 'MotorView', params: { motorId: auto.motor.id } }">{{ auto.motor.id }}</router-link>
              </div>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'AutoView', params: { autoId: auto.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'AutoEdit', params: { autoId: auto.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(auto)"
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
        <span id="concesionariaApp.auto.delete.question" data-cy="autoDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-auto-heading">¿Seguro que quiere eliminar Auto {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-auto"
            data-cy="entityConfirmDeleteButton"
            @click="removeAuto"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="autos?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./auto.component.ts"></script>
