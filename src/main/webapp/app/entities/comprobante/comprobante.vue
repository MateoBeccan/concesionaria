<template>
  <div>
    <h2 id="page-heading" data-cy="ComprobanteHeading">
      <span id="comprobante">Comprobantes</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" />
          <span>Refrescar lista</span>
        </button>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && comprobantes?.length === 0">
      <span>No se encontraron comprobantes</span>
    </div>
    <div class="table-responsive" v-if="comprobantes?.length > 0">
      <table class="table table-striped" aria-describedby="comprobantes">
        <thead>
          <tr>
            <th>ID</th>
            <th>Número</th>
            <th>Tipo</th>
            <th>Estado</th>
            <th>Fecha emisión</th>
            <th class="text-end">Total</th>
            <th>Moneda</th>
            <th>Venta</th>
            <th>Usuario emisor</th>
            <th class="text-end">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="comprobante in comprobantes" :key="comprobante.id" :class="comprobante.estado === 'ANULADO' ? 'table-secondary' : ''">
            <td><router-link :to="{ name: 'ComprobanteView', params: { comprobanteId: comprobante.id } }">{{ comprobante.id }}</router-link></td>
            <td>{{ comprobante.numeroComprobante }}</td>
            <td>{{ comprobante.tipoComprobante?.codigo ?? '-' }}</td>
            <td>
              <span class="badge" :class="comprobante.estado === 'ANULADO' ? 'bg-secondary' : 'bg-success'">{{ comprobante.estado ?? '-' }}</span>
            </td>
            <td>{{ formatDateShort(comprobante.fechaEmision) || '' }}</td>
            <td class="text-end">{{ comprobante.total }}</td>
            <td>{{ comprobante.moneda?.simbolo ?? '' }} {{ comprobante.moneda?.codigo ?? '-' }}</td>
            <td>
              <router-link v-if="comprobante.venta?.id" :to="{ name: 'VentaView', params: { ventaId: comprobante.venta.id } }">
                {{ comprobante.venta.id }}
              </router-link>
            </td>
            <td>{{ comprobante.usuarioEmision ?? comprobante.createdBy ?? '-' }}</td>
            <td class="text-end">
              <router-link :to="{ name: 'ComprobanteView', params: { comprobanteId: comprobante.id } }" custom v-slot="{ navigate }">
                <button @click="navigate" class="btn btn-info btn-sm me-1"><font-awesome-icon icon="eye" /></button>
              </router-link>
              <button class="btn btn-outline-primary btn-sm me-1" @click="descargarPdf(comprobante.id)">
                <font-awesome-icon icon="file-pdf" />
              </button>
              <b-button v-if="comprobante.estado !== 'ANULADO'" @click="prepareRemove(comprobante)" variant="danger" class="btn btn-sm" v-b-modal.removeEntity>
                <font-awesome-icon icon="times" />
              </b-button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <b-modal ref="removeEntity" id="removeEntity">
      <template #title>Confirmar anulación</template>
      <div class="modal-body">
        <p>¿Seguro que quieres anular el comprobante {{ removeId }}?</p>
        <label class="form-label">Motivo</label>
        <textarea v-model="motivoAnulacion" class="form-control" rows="3" maxlength="500" />
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button type="button" class="btn btn-primary" :disabled="!motivoAnulacion || !motivoAnulacion.trim()" @click="removeComprobante">
            Anular
          </button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./comprobante.component.ts"></script>
