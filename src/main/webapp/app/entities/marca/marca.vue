<template>
  <div>
    <h2 id="page-heading" data-cy="MarcaHeading">
      <span id="marca">Marcas</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'MarcaCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-marca"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Marca</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && marcas?.length === 0">
      <span>Ningún Marcas encontrado</span>
    </div>
    <div class="table-responsive" v-if="marcas?.length > 0">
      <table class="table table-striped" aria-describedby="marcas">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('nombre')">
              <span>Nombre</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nombre'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('paisOrigen')">
              <span>Pais Origen</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'paisOrigen'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('createdDate')">
              <span>Created Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('lastModifiedDate')">
              <span>Last Modified Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastModifiedDate'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="marca in marcas" :key="marca.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'MarcaView', params: { marcaId: marca.id } }">{{ marca.id }}</router-link>
            </td>
            <td>{{ marca.nombre }}</td>
            <td>{{ marca.paisOrigen }}</td>
            <td>{{ formatDateShort(marca.createdDate) || '' }}</td>
            <td>{{ formatDateShort(marca.lastModifiedDate) || '' }}</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'MarcaView', params: { marcaId: marca.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'MarcaEdit', params: { marcaId: marca.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(marca)"
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
        <span id="concesionariaApp.marca.delete.question" data-cy="marcaDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-marca-heading">¿Seguro que quiere eliminar Marca {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-marca"
            data-cy="entityConfirmDeleteButton"
            @click="removeMarca"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="marcas?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./marca.component.ts"></script>
