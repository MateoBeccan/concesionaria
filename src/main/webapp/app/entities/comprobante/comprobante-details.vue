<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="comprobante">
        <h2 class="jh-entity-heading" data-cy="comprobanteDetailsHeading"><span>ComprobanteComprobante</span> {{ comprobante.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Numero Comprobante</span>
          </dt>
          <dd>
            <span>{{ comprobante.numeroComprobante }}</span>
          </dd>
          <dt>
            <span>Fecha Emision</span>
          </dt>
          <dd>
            <span v-if="comprobante.fechaEmision">{{ formatDateLong(comprobante.fechaEmision) }}</span>
          </dd>
          <dt>
            <span>Importe Neto</span>
          </dt>
          <dd>
            <span>{{ comprobante.importeNeto }}</span>
          </dd>
          <dt>
            <span>Impuesto</span>
          </dt>
          <dd>
            <span>{{ comprobante.impuesto }}</span>
          </dd>
          <dt>
            <span>Total</span>
          </dt>
          <dd>
            <span>{{ comprobante.total }}</span>
          </dd>
          <dt>
            <span>Venta</span>
          </dt>
          <dd>
            <div v-if="comprobante.venta">
              <router-link :to="{ name: 'VentaView', params: { ventaId: comprobante.venta.id } }">{{ comprobante.venta.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Tipo Comprobante</span>
          </dt>
          <dd>
            <div v-if="comprobante.tipoComprobante">
              <router-link :to="{ name: 'TipoComprobanteView', params: { tipoComprobanteId: comprobante.tipoComprobante.id } }">{{
                comprobante.tipoComprobante.id
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Moneda</span>
          </dt>
          <dd>
            <div v-if="comprobante.moneda">
              <router-link :to="{ name: 'MonedaView', params: { monedaId: comprobante.moneda.id } }">{{
                comprobante.moneda.id
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link
          v-if="comprobante.id"
          :to="{ name: 'ComprobanteEdit', params: { comprobanteId: comprobante.id } }"
          custom
          v-slot="{ navigate }"
        >
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./comprobante-details.component.ts"></script>
