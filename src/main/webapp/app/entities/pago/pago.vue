<template>
  <div>
    <h2 id="page-heading" data-cy="PagoHeading">
      <span id="pago">Pagos</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" />
          <span>Refrescar lista</span>
        </button>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && pagos?.length === 0">
      <span>No se encontraron pagos</span>
    </div>
    <div class="table-responsive" v-if="pagos?.length > 0">
      <table class="table table-striped" aria-describedby="pagos">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')"><span>ID</span></th>
            <th scope="col" @click="changeOrder('fecha')"><span>Fecha</span></th>
            <th scope="col" @click="changeOrder('metodoPago.id')"><span>Método</span></th>
            <th scope="col" @click="changeOrder('monto')"><span>Monto</span></th>
            <th scope="col" @click="changeOrder('moneda.id')"><span>Moneda</span></th>
            <th scope="col" @click="changeOrder('cotizacionUsada')"><span>Cotización</span></th>
            <th scope="col" @click="changeOrder('montoAplicadoVenta')"><span>Aplicado ARS</span></th>
            <th scope="col" @click="changeOrder('usuarioRegistro')"><span>Usuario</span></th>
            <th scope="col" @click="changeOrder('estado')"><span>Estado</span></th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="pago in pagos" :key="pago.id" data-cy="entityTable" :class="pago.estado === 'ANULADO' ? 'table-secondary' : ''">
            <td><router-link :to="{ name: 'PagoView', params: { pagoId: pago.id } }">{{ pago.id }}</router-link></td>
            <td>{{ formatDateShort(pago.fecha) || '' }}</td>
            <td>{{ pago.metodoPago?.descripcion ?? pago.metodoPago?.codigo ?? '-' }}</td>
            <td class="text-end">{{ pago.monto }}</td>
            <td>{{ pago.moneda?.simbolo ?? '' }} {{ pago.moneda?.codigo ?? '-' }}</td>
            <td class="text-end">{{ pago.cotizacionUsada ?? '-' }}</td>
            <td class="text-end">{{ pago.montoAplicadoVenta ?? '-' }}</td>
            <td>{{ pago.usuarioRegistro ?? '-' }}</td>
            <td>
              <span class="badge" :class="pago.estado === 'ANULADO' ? 'bg-secondary' : 'bg-success'">{{ pago.estado ?? '-' }}</span>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'PagoView', params: { pagoId: pago.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details"><font-awesome-icon icon="eye" /></button>
                </router-link>
                <b-button
                  v-if="pago.estado !== 'ANULADO'"
                  @click="prepareRemove(pago)"
                  variant="danger"
                  class="btn btn-sm"
                  v-b-modal.removeEntity
                >
                  <font-awesome-icon icon="times" />
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <b-modal ref="removeEntity" id="removeEntity">
      <template #title>Confirmar anulación</template>
      <div class="modal-body">
        <p>¿Seguro que quieres anular el pago {{ removeId }}?</p>
        <label class="form-label">Motivo</label>
        <textarea v-model="motivoAnulacion" class="form-control" rows="3" maxlength="500" />
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button type="button" class="btn btn-primary" :disabled="!motivoAnulacion || !motivoAnulacion.trim()" @click="removePago">
            Anular
          </button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./pago.component.ts"></script>

