<template>
  <div>
    <h2 id="page-heading" data-cy="CombustibleHeading">
      <span id="combustible">Combustibles</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'CombustibleCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-combustible"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Combustible</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && combustibles?.length === 0">
      <span>Ningún Combustibles encontrado</span>
    </div>
    <div class="table-responsive" v-if="combustibles?.length > 0">
      <table class="table table-striped" aria-describedby="combustibles">
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
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="combustible in combustibles" :key="combustible.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'CombustibleView', params: { combustibleId: combustible.id } }">{{ combustible.id }}</router-link>
            </td>
            <td>{{ combustible.nombre }}</td>
            <td>{{ combustible.descripcion }}</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'CombustibleView', params: { combustibleId: combustible.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'CombustibleEdit', params: { combustibleId: combustible.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(combustible)"
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
        <span id="concesionariaApp.combustible.delete.question" data-cy="combustibleDeleteDialogHeading"
          >Confirmar operación de borrado</span
        >
      </template>
      <div class="modal-body">
        <p id="jhi-delete-combustible-heading">¿Seguro que quiere eliminar Combustible {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-combustible"
            data-cy="entityConfirmDeleteButton"
            @click="removeCombustible"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="combustibles?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./combustible.component.ts"></script>
