<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="pago">
        <h2 class="jh-entity-heading" data-cy="pagoDetailsHeading"><span>PagoPago</span> {{ pago.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Monto</span>
          </dt>
          <dd>
            <span>{{ pago.monto }}</span>
          </dd>
          <dt>
            <span>Fecha</span>
          </dt>
          <dd>
            <span v-if="pago.fecha">{{ formatDateLong(pago.fecha) }}</span>
          </dd>
          <dt>
            <span>Referencia</span>
          </dt>
          <dd>
            <span>{{ pago.referencia }}</span>
          </dd>
          <dt>
            <span>Venta</span>
          </dt>
          <dd>
            <div v-if="pago.venta">
              <router-link :to="{ name: 'VentaView', params: { ventaId: pago.venta.id } }">{{ pago.venta.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Metodo Pago</span>
          </dt>
          <dd>
            <div v-if="pago.metodoPago">
              <router-link :to="{ name: 'MetodoPagoView', params: { metodoPagoId: pago.metodoPago.id } }">{{
                pago.metodoPago.id
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Moneda</span>
          </dt>
          <dd>
            <div v-if="pago.moneda">
              <router-link :to="{ name: 'MonedaView', params: { monedaId: pago.moneda.id } }">{{ pago.moneda.id }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="pago.id" :to="{ name: 'PagoEdit', params: { pagoId: pago.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./pago-details.component.ts"></script>
