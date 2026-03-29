<template>
  <div>
    <h2 id="page-heading" data-cy="ComprobanteHeading">
      <span id="comprobante">Comprobantes</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'ComprobanteCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-comprobante"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Comprobante</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && comprobantes?.length === 0">
      <span>Ningún Comprobantes encontrado</span>
    </div>
    <div class="table-responsive" v-if="comprobantes?.length > 0">
      <table class="table table-striped" aria-describedby="comprobantes">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('numeroComprobante')">
              <span>Numero Comprobante</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'numeroComprobante'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('fechaEmision')">
              <span>Fecha Emision</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fechaEmision'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('importeNeto')">
              <span>Importe Neto</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'importeNeto'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('impuesto')">
              <span>Impuesto</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'impuesto'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('total')">
              <span>Total</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'total'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('createdDate')">
              <span>Created Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('venta.id')">
              <span>Venta</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'venta.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('tipoComprobante.id')">
              <span>Tipo Comprobante</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'tipoComprobante.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('moneda.id')">
              <span>Moneda</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'moneda.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="comprobante in comprobantes" :key="comprobante.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'ComprobanteView', params: { comprobanteId: comprobante.id } }">{{ comprobante.id }}</router-link>
            </td>
            <td>{{ comprobante.numeroComprobante }}</td>
            <td>{{ formatDateShort(comprobante.fechaEmision) || '' }}</td>
            <td>{{ comprobante.importeNeto }}</td>
            <td>{{ comprobante.impuesto }}</td>
            <td>{{ comprobante.total }}</td>
            <td>{{ formatDateShort(comprobante.createdDate) || '' }}</td>
            <td>
              <div v-if="comprobante.venta">
                <router-link :to="{ name: 'VentaView', params: { ventaId: comprobante.venta.id } }">{{ comprobante.venta.id }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="comprobante.tipoComprobante">
                <router-link :to="{ name: 'TipoComprobanteView', params: { tipoComprobanteId: comprobante.tipoComprobante.id } }">{{
                  comprobante.tipoComprobante.id
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="comprobante.moneda">
                <router-link :to="{ name: 'MonedaView', params: { monedaId: comprobante.moneda.id } }">{{
                  comprobante.moneda.id
                }}</router-link>
              </div>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'ComprobanteView', params: { comprobanteId: comprobante.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'ComprobanteEdit', params: { comprobanteId: comprobante.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(comprobante)"
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
        <span id="concesionariaApp.comprobante.delete.question" data-cy="comprobanteDeleteDialogHeading"
          >Confirmar operación de borrado</span
        >
      </template>
      <div class="modal-body">
        <p id="jhi-delete-comprobante-heading">¿Seguro que quiere eliminar Comprobante {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-comprobante"
            data-cy="entityConfirmDeleteButton"
            @click="removeComprobante"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="comprobantes?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./comprobante.component.ts"></script>
