<template>
  <div>
    <h2 id="page-heading" data-cy="VersionHeading">
      <span id="version">Versions</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'VersionCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-version"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Version</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && versions?.length === 0">
      <span>Ningún Versions encontrado</span>
    </div>
    <div class="table-responsive" v-if="versions?.length > 0">
      <table class="table table-striped" aria-describedby="versions">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('nombre')">
              <span>Nombre</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nombre'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('descripcion')">
              <span>Descripcion</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'descripcion'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('anioInicio')">
              <span>Anio Inicio</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'anioInicio'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('anioFin')">
              <span>Anio Fin</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'anioFin'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('modelo.id')">
              <span>Modelo</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'modelo.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="version in versions" :key="version.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'VersionView', params: { versionId: version.id } }">{{ version.id }}</router-link>
            </td>
            <td>{{ version.nombre }}</td>
            <td>{{ version.descripcion }}</td>
            <td>{{ version.anioInicio }}</td>
            <td>{{ version.anioFin }}</td>
            <td>
  <div v-if="version.modelo">
    <span class="badge bg-primary">
      {{ version.modelo?.nombre }}
    </span>
  </div>
</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'VersionView', params: { versionId: version.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'VersionEdit', params: { versionId: version.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(version)"
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
        <span id="concesionariaApp.version.delete.question" data-cy="versionDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-version-heading">¿Seguro que quiere eliminar Version {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-version"
            data-cy="entityConfirmDeleteButton"
            @click="removeVersion"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="versions?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./version.component.ts"></script>
