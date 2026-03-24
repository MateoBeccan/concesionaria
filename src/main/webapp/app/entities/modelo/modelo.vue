<template>
  <div>
    <h2 id="page-heading" data-cy="ModeloHeading">
      <span id="modelo">Modelos</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'ModeloCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-modelo"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Modelo</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && modelos?.length === 0">
      <span>Ningún Modelos encontrado</span>
    </div>
    <div class="table-responsive" v-if="modelos?.length > 0">
      <table class="table table-striped" aria-describedby="modelos">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('nombre')">
              <span>Nombre</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nombre'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('anioLanzamiento')">
              <span>Anio Lanzamiento</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'anioLanzamiento'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('carroceria')">
              <span>Carroceria</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'carroceria'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('marca.id')">
              <span>Marca</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'marca.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="modelo in modelos" :key="modelo.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'ModeloView', params: { modeloId: modelo.id } }">{{ modelo.id }}</router-link>
            </td>
            <td>{{ modelo.nombre }}</td>
            <td>{{ modelo.anioLanzamiento }}</td>
            <td>{{ modelo.carroceria }}</td>
            <td>
              <div v-if="modelo.marca">
                <router-link :to="{ name: 'MarcaView', params: { marcaId: modelo.marca.id  } }">
  {{ modelo.marca.nombre }}
</router-link>
              </div>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'ModeloView', params: { modeloId: modelo.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'ModeloEdit', params: { modeloId: modelo.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(modelo)"
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
        <span id="concesionariaApp.modelo.delete.question" data-cy="modeloDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-modelo-heading">¿Seguro que quiere eliminar Modelo {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-modelo"
            data-cy="entityConfirmDeleteButton"
            @click="removeModelo"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="modelos?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./modelo.component.ts"></script>
