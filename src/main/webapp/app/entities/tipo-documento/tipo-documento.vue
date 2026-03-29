<template>
  <div>
    <h2 id="page-heading" data-cy="TipoDocumentoHeading">
      <span id="tipo-documento">Tipo Documentos</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'TipoDocumentoCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-tipo-documento"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Tipo Documento</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && tipoDocumentos?.length === 0">
      <span>Ningún Tipo Documentos encontrado</span>
    </div>
    <div class="table-responsive" v-if="tipoDocumentos?.length > 0">
      <table class="table table-striped" aria-describedby="tipoDocumentos">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('codigo')">
              <span>Codigo</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'codigo'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('descripcion')">
              <span>Descripcion</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'descripcion'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tipoDocumento in tipoDocumentos" :key="tipoDocumento.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'TipoDocumentoView', params: { tipoDocumentoId: tipoDocumento.id } }">{{
                tipoDocumento.id
              }}</router-link>
            </td>
            <td>{{ tipoDocumento.codigo }}</td>
            <td>{{ tipoDocumento.descripcion }}</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'TipoDocumentoView', params: { tipoDocumentoId: tipoDocumento.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'TipoDocumentoEdit', params: { tipoDocumentoId: tipoDocumento.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(tipoDocumento)"
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
        <span id="concesionariaApp.tipoDocumento.delete.question" data-cy="tipoDocumentoDeleteDialogHeading"
          >Confirmar operación de borrado</span
        >
      </template>
      <div class="modal-body">
        <p id="jhi-delete-tipoDocumento-heading">¿Seguro que quiere eliminar Tipo Documento {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-tipoDocumento"
            data-cy="entityConfirmDeleteButton"
            @click="removeTipoDocumento"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="tipoDocumentos?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./tipo-documento.component.ts"></script>
