<template>
  <div>
    <h2 id="page-heading" data-cy="CotizacionHeading">
      <span id="cotizacion">Cotizacions</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'CotizacionCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-cotizacion"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Cotizacion</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && cotizacions?.length === 0">
      <span>Ningún Cotizacions encontrado</span>
    </div>
    <div class="table-responsive" v-if="cotizacions?.length > 0">
      <table class="table table-striped" aria-describedby="cotizacions">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('fecha')">
              <span>Fecha</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fecha'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('valorCompra')">
              <span>Valor Compra</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'valorCompra'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('valorVenta')">
              <span>Valor Venta</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'valorVenta'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('activo')">
              <span>Activo</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'activo'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('moneda.id')">
              <span>Moneda</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'moneda.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cotizacion in cotizacions" :key="cotizacion.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'CotizacionView', params: { cotizacionId: cotizacion.id } }">{{ cotizacion.id }}</router-link>
            </td>
            <td>{{ formatDateShort(cotizacion.fecha) || '' }}</td>
            <td>{{ cotizacion.valorCompra }}</td>
            <td>{{ cotizacion.valorVenta }}</td>
            <td>{{ cotizacion.activo }}</td>
            <td>
              <div v-if="cotizacion.moneda">
                <router-link :to="{ name: 'MonedaView', params: { monedaId: cotizacion.moneda.id } }">{{
                  cotizacion.moneda.id
                }}</router-link>
              </div>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'CotizacionView', params: { cotizacionId: cotizacion.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'CotizacionEdit', params: { cotizacionId: cotizacion.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(cotizacion)"
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
        <span id="concesionariaApp.cotizacion.delete.question" data-cy="cotizacionDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-cotizacion-heading">¿Seguro que quiere eliminar Cotizacion {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-cotizacion"
            data-cy="entityConfirmDeleteButton"
            @click="removeCotizacion"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="cotizacions?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./cotizacion.component.ts"></script>
