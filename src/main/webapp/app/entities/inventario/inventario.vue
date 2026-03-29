<template>
  <div>
    <h2 id="page-heading" data-cy="InventarioHeading">
      <span id="inventario">Inventarios</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'InventarioCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-inventario"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Inventario</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && inventarios?.length === 0">
      <span>Ningún Inventarios encontrado</span>
    </div>
    <div class="table-responsive" v-if="inventarios?.length > 0">
      <table class="table table-striped" aria-describedby="inventarios">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('fechaIngreso')">
              <span>Fecha Ingreso</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fechaIngreso'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('ubicacion')">
              <span>Ubicacion</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'ubicacion'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('estadoInventario')">
              <span>Estado Inventario</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'estadoInventario'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('observaciones')">
              <span>Observaciones</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'observaciones'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('disponible')">
              <span>Disponible</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'disponible'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('createdDate')">
              <span>Created Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('lastModifiedDate')">
              <span>Last Modified Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastModifiedDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('fechaReserva')">
              <span>Fecha Reserva</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fechaReserva'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('fechaVencimientoReserva')">
              <span>Fecha Vencimiento Reserva</span>
              <jhi-sort-indicator
                :current-order="propOrder"
                :reverse="reverse"
                :field-name="'fechaVencimientoReserva'"
              ></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('vehiculo.id')">
              <span>Vehiculo</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'vehiculo.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('clienteReserva.id')">
              <span>Cliente Reserva</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'clienteReserva.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="inventario in inventarios" :key="inventario.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'InventarioView', params: { inventarioId: inventario.id } }">{{ inventario.id }}</router-link>
            </td>
            <td>{{ formatDateShort(inventario.fechaIngreso) || '' }}</td>
            <td>{{ inventario.ubicacion }}</td>
            <td>{{ inventario.estadoInventario }}</td>
            <td>{{ inventario.observaciones }}</td>
            <td>{{ inventario.disponible }}</td>
            <td>{{ formatDateShort(inventario.createdDate) || '' }}</td>
            <td>{{ formatDateShort(inventario.lastModifiedDate) || '' }}</td>
            <td>{{ formatDateShort(inventario.fechaReserva) || '' }}</td>
            <td>{{ formatDateShort(inventario.fechaVencimientoReserva) || '' }}</td>
            <td>
              <div v-if="inventario.vehiculo">
                <router-link :to="{ name: 'VehiculoView', params: { vehiculoId: inventario.vehiculo.id } }">{{
                  inventario.vehiculo.id
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="inventario.clienteReserva">
                <router-link :to="{ name: 'ClienteView', params: { clienteId: inventario.clienteReserva.id } }">{{
                  inventario.clienteReserva.id
                }}</router-link>
              </div>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'InventarioView', params: { inventarioId: inventario.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'InventarioEdit', params: { inventarioId: inventario.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(inventario)"
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
        <span id="concesionariaApp.inventario.delete.question" data-cy="inventarioDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-inventario-heading">¿Seguro que quiere eliminar Inventario {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-inventario"
            data-cy="entityConfirmDeleteButton"
            @click="removeInventario"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="inventarios?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./inventario.component.ts"></script>
