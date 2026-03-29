<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="venta">
        <h2 class="jh-entity-heading" data-cy="ventaDetailsHeading"><span>VentaVenta</span> {{ venta.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Fecha</span>
          </dt>
          <dd>
            <span v-if="venta.fecha">{{ formatDateLong(venta.fecha) }}</span>
          </dd>
          <dt>
            <span>Cotizacion</span>
          </dt>
          <dd>
            <span>{{ venta.cotizacion }}</span>
          </dd>
          <dt>
            <span>Importe Neto</span>
          </dt>
          <dd>
            <span>{{ venta.importeNeto }}</span>
          </dd>
          <dt>
            <span>Impuesto</span>
          </dt>
          <dd>
            <span>{{ venta.impuesto }}</span>
          </dd>
          <dt>
            <span>Total</span>
          </dt>
          <dd>
            <span>{{ venta.total }}</span>
          </dd>
          <dt>
            <span>Porcentaje Impuesto</span>
          </dt>
          <dd>
            <span>{{ venta.porcentajeImpuesto }}</span>
          </dd>
          <dt>
            <span>Total Pagado</span>
          </dt>
          <dd>
            <span>{{ venta.totalPagado }}</span>
          </dd>
          <dt>
            <span>Saldo</span>
          </dt>
          <dd>
            <span>{{ venta.saldo }}</span>
          </dd>
          <dt>
            <span>Observaciones</span>
          </dt>
          <dd>
            <span>{{ venta.observaciones }}</span>
          </dd>
          <dt>
            <span>Created Date</span>
          </dt>
          <dd>
            <span v-if="venta.createdDate">{{ formatDateLong(venta.createdDate) }}</span>
          </dd>
          <dt>
            <span>Last Modified Date</span>
          </dt>
          <dd>
            <span v-if="venta.lastModifiedDate">{{ formatDateLong(venta.lastModifiedDate) }}</span>
          </dd>
          <dt>
            <span>Cliente</span>
          </dt>
          <dd>
            <div v-if="venta.cliente">
              <router-link :to="{ name: 'ClienteView', params: { clienteId: venta.cliente.id } }">{{ venta.cliente.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Estado Venta</span>
          </dt>
          <dd>
            <div v-if="venta.estadoVenta">
              <router-link :to="{ name: 'EstadoVentaView', params: { estadoVentaId: venta.estadoVenta.id } }">{{
                venta.estadoVenta.id
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Moneda</span>
          </dt>
          <dd>
            <div v-if="venta.moneda">
              <router-link :to="{ name: 'MonedaView', params: { monedaId: venta.moneda.id } }">{{ venta.moneda.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>User</span>
          </dt>
          <dd>
            {{ venta.user ? venta.user.login : '' }}
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="venta.id" :to="{ name: 'VentaEdit', params: { ventaId: venta.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./venta-details.component.ts"></script>
