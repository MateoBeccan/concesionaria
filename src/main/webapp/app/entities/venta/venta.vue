<template>
  <div>
    <h2 id="page-heading" data-cy="VentaHeading">
      <span id="venta">Ventas</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'VentaCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-venta"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Venta</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && ventas?.length === 0">
      <span>Ningún Ventas encontrado</span>
    </div>
    <div class="table-responsive" v-if="ventas?.length > 0">
      <table class="table table-striped" aria-describedby="ventas">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('fecha')">
              <span>Fecha</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'fecha'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('cotizacion')">
              <span>Cotizacion</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'cotizacion'"></jhi-sort-indicator>
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
            <th scope="col" @click="changeOrder('porcentajeImpuesto')">
              <span>Porcentaje Impuesto</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'porcentajeImpuesto'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('totalPagado')">
              <span>Total Pagado</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'totalPagado'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('saldo')">
              <span>Saldo</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'saldo'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('observaciones')">
              <span>Observaciones</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'observaciones'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('createdDate')">
              <span>Created Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('lastModifiedDate')">
              <span>Last Modified Date</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastModifiedDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('cliente.id')">
              <span>Cliente</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'cliente.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('estadoVenta.id')">
              <span>Estado Venta</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'estadoVenta.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('moneda.id')">
              <span>Moneda</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'moneda.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('user.login')">
              <span>User</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'user.login'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="venta in ventas" :key="venta.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'VentaView', params: { ventaId: venta.id } }">{{ venta.id }}</router-link>
            </td>
            <td>{{ formatDateShort(venta.fecha) || '' }}</td>
            <td>{{ venta.cotizacion }}</td>
            <td>{{ venta.importeNeto }}</td>
            <td>{{ venta.impuesto }}</td>
            <td>{{ venta.total }}</td>
            <td>{{ venta.porcentajeImpuesto }}</td>
            <td>{{ venta.totalPagado }}</td>
            <td>{{ venta.saldo }}</td>
            <td>{{ venta.observaciones }}</td>
            <td>{{ formatDateShort(venta.createdDate) || '' }}</td>
            <td>{{ formatDateShort(venta.lastModifiedDate) || '' }}</td>
            <td>
              <div v-if="venta.cliente">
                <router-link :to="{ name: 'ClienteView', params: { clienteId: venta.cliente.id } }">{{ venta.cliente.id }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="venta.estadoVenta">
                <router-link :to="{ name: 'EstadoVentaView', params: { estadoVentaId: venta.estadoVenta.id } }">{{
                  venta.estadoVenta.id
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="venta.moneda">
                <router-link :to="{ name: 'MonedaView', params: { monedaId: venta.moneda.id } }">{{ venta.moneda.id }}</router-link>
              </div>
            </td>
            <td>
              {{ venta.user ? venta.user.login : '' }}
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'VentaView', params: { ventaId: venta.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'VentaEdit', params: { ventaId: venta.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(venta)"
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
        <span id="concesionariaApp.venta.delete.question" data-cy="ventaDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-venta-heading">¿Seguro que quiere eliminar Venta {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-venta"
            data-cy="entityConfirmDeleteButton"
            @click="removeVenta"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="ventas?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./venta.component.ts"></script>
