<template>
  <div>
    <h2 id="page-heading" data-cy="DetalleVentaHeading">
      <span id="detalle-venta">Detalle Ventas</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'DetalleVentaCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-detalle-venta"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Detalle Venta</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && detalleVentas?.length === 0">
      <span>Ningún Detalle Ventas encontrado</span>
    </div>
    <div class="table-responsive" v-if="detalleVentas?.length > 0">
      <table class="table table-striped" aria-describedby="detalleVentas">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('precioUnitario')">
              <span>Precio Unitario</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'precioUnitario'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('cantidad')">
              <span>Cantidad</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'cantidad'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('subtotal')">
              <span>Subtotal</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'subtotal'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('venta.id')">
              <span>Venta</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'venta.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('auto.id')">
              <span>Auto</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'auto.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="detalleVenta in detalleVentas" :key="detalleVenta.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'DetalleVentaView', params: { detalleVentaId: detalleVenta.id } }">{{
                detalleVenta.id
              }}</router-link>
            </td>
            <td>{{ detalleVenta.precioUnitario }}</td>
            <td>{{ detalleVenta.cantidad }}</td>
            <td>{{ detalleVenta.subtotal }}</td>
            <td>
              <div v-if="detalleVenta.venta">
                <router-link :to="{ name: 'VentaView', params: { ventaId: detalleVenta.venta.id } }">{{
                  detalleVenta.venta.id
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="detalleVenta.auto">
                <router-link :to="{ name: 'AutoView', params: { autoId: detalleVenta.auto.id } }">{{ detalleVenta.auto.id }}</router-link>
              </div>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'DetalleVentaView', params: { detalleVentaId: detalleVenta.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'DetalleVentaEdit', params: { detalleVentaId: detalleVenta.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(detalleVenta)"
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
        <span id="concesionariaApp.detalleVenta.delete.question" data-cy="detalleVentaDeleteDialogHeading"
          >Confirmar operación de borrado</span
        >
      </template>
      <div class="modal-body">
        <p id="jhi-delete-detalleVenta-heading">¿Seguro que quiere eliminar Detalle Venta {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-detalleVenta"
            data-cy="entityConfirmDeleteButton"
            @click="removeDetalleVenta"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="detalleVentas?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./detalle-venta.component.ts"></script>
