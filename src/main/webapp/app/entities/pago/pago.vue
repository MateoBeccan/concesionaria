<template>
  <div>
    <h2 id="page-heading" data-cy="PagoHeading">
      <span id="pago">Pagos</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'PagoCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" id="jh-create-entity" data-cy="entityCreateButton" class="btn btn-primary jh-create-entity create-pago">
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Pago</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && pagos?.length === 0">
      <span>Ningún Pagos encontrado</span>
    </div>
    <div class="table-responsive" v-if="pagos?.length > 0">
      <table class="table table-striped" aria-describedby="pagos">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('monto')">
              <span>Monto</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'monto'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('fecha')">
              <span>Fecha</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fecha'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('referencia')">
              <span>Referencia</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'referencia'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('createdDate')">
              <span>Created Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('venta.id')">
              <span>Venta</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'venta.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('metodoPago.id')">
              <span>Metodo Pago</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'metodoPago.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('moneda.id')">
              <span>Moneda</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'moneda.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="pago in pagos" :key="pago.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'PagoView', params: { pagoId: pago.id } }">{{ pago.id }}</router-link>
            </td>
            <td>{{ pago.monto }}</td>
            <td>{{ formatDateShort(pago.fecha) || '' }}</td>
            <td>{{ pago.referencia }}</td>
            <td>{{ formatDateShort(pago.createdDate) || '' }}</td>
            <td>
              <div v-if="pago.venta">
                <router-link :to="{ name: 'VentaView', params: { ventaId: pago.venta.id } }">{{ pago.venta.id }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="pago.metodoPago">
                <router-link :to="{ name: 'MetodoPagoView', params: { metodoPagoId: pago.metodoPago.id } }">{{
                  pago.metodoPago.id
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="pago.moneda">
                <router-link :to="{ name: 'MonedaView', params: { monedaId: pago.moneda.id } }">{{ pago.moneda.id }}</router-link>
              </div>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'PagoView', params: { pagoId: pago.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'PagoEdit', params: { pagoId: pago.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(pago)"
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
        <span id="concesionariaApp.pago.delete.question" data-cy="pagoDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-pago-heading">¿Seguro que quiere eliminar Pago {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-pago"
            data-cy="entityConfirmDeleteButton"
            @click="removePago"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="pagos?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./pago.component.ts"></script>
