<template>
  <div>
    <h2 id="page-heading" data-cy="ClienteHeading">
      <span id="cliente">Clientes</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'ClienteCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-cliente"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Cliente</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && clientes?.length === 0">
      <span>Ningún Clientes encontrado</span>
    </div>
    <div class="table-responsive" v-if="clientes?.length > 0">
      <table class="table table-striped" aria-describedby="clientes">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('nombre')">
              <span>Nombre</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nombre'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('apellido')">
              <span>Apellido</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'apellido'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('nroDocumento')">
              <span>Nro Documento</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nroDocumento'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('telefono')">
              <span>Telefono</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'telefono'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('email')">
              <span>Email</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'email'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('direccion')">
              <span>Direccion</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'direccion'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('ciudad')">
              <span>Ciudad</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'ciudad'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('provincia')">
              <span>Provincia</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'provincia'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('pais')">
              <span>Pais</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'pais'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('activo')">
              <span>Activo</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'activo'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('fechaAlta')">
              <span>Fecha Alta</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fechaAlta'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('createdDate')">
              <span>Created Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('lastModifiedDate')">
              <span>Last Modified Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastModifiedDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('condicionIva.id')">
              <span>Condicion Iva</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'condicionIva.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('tipoDocumento.id')">
              <span>Tipo Documento</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'tipoDocumento.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cliente in clientes" :key="cliente.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'ClienteView', params: { clienteId: cliente.id } }">{{ cliente.id }}</router-link>
            </td>
            <td>{{ cliente.nombre }}</td>
            <td>{{ cliente.apellido }}</td>
            <td>{{ cliente.nroDocumento }}</td>
            <td>{{ cliente.telefono }}</td>
            <td>{{ cliente.email }}</td>
            <td>{{ cliente.direccion }}</td>
            <td>{{ cliente.ciudad }}</td>
            <td>{{ cliente.provincia }}</td>
            <td>{{ cliente.pais }}</td>
            <td>{{ cliente.activo }}</td>
            <td>{{ formatDateShort(cliente.fechaAlta) || '' }}</td>
            <td>{{ formatDateShort(cliente.createdDate) || '' }}</td>
            <td>{{ formatDateShort(cliente.lastModifiedDate) || '' }}</td>
            <td>
  <div v-if="cliente.condicionIva">
    <span class="badge bg-info text-dark">
     {{ cliente.condicionIva?.codigo }}
    </span>
  </div>
</td>

<td>
  <div v-if="cliente.tipoDocumento">
    <span class="badge bg-secondary">
      {{ cliente.tipoDocumento?.codigo }}
    </span>
  </div>
</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'ClienteView', params: { clienteId: cliente.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'ClienteEdit', params: { clienteId: cliente.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(cliente)"
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
        <span id="concesionariaApp.cliente.delete.question" data-cy="clienteDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-cliente-heading">¿Seguro que quiere eliminar Cliente {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-cliente"
            data-cy="entityConfirmDeleteButton"
            @click="removeCliente"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="clientes?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./cliente.component.ts"></script>
