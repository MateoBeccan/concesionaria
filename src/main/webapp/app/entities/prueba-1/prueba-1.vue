<template>
  <div>
    <h2 id="page-heading" data-cy="Prueba1Heading">
      <span id="prueba-1">Prueba 1s</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'Prueba1Create' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-prueba-1"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Prueba 1</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && prueba1s?.length === 0">
      <span>Ningún Prueba 1s encontrado</span>
    </div>
    <div class="table-responsive" v-if="prueba1s?.length > 0">
      <table class="table table-striped" aria-describedby="prueba1s">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="prueba1 in prueba1s" :key="prueba1.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'Prueba1View', params: { prueba1Id: prueba1.id } }">{{ prueba1.id }}</router-link>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'Prueba1View', params: { prueba1Id: prueba1.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'Prueba1Edit', params: { prueba1Id: prueba1.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(prueba1)"
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
        <span id="concesionariaApp.prueba1.delete.question" data-cy="prueba1DeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-prueba1-heading">¿Seguro que quiere eliminar Prueba 1 {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-prueba1"
            data-cy="entityConfirmDeleteButton"
            @click="removePrueba1"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="prueba1s?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./prueba-1.component.ts"></script>
