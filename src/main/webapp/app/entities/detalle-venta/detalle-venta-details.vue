<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="detalleVenta">
        <h2 class="jh-entity-heading" data-cy="detalleVentaDetailsHeading"><span>Detalle VentaDetalleVenta</span> {{ detalleVenta.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Precio Unitario</span>
          </dt>
          <dd>
            <span>{{ detalleVenta.precioUnitario }}</span>
          </dd>
          <dt>
            <span>Cantidad</span>
          </dt>
          <dd>
            <span>{{ detalleVenta.cantidad }}</span>
          </dd>
          <dt>
            <span>Subtotal</span>
          </dt>
          <dd>
            <span>{{ detalleVenta.subtotal }}</span>
          </dd>
          <dt>
            <span>Venta</span>
          </dt>
          <dd>
            <div v-if="detalleVenta.venta">
              <router-link :to="{ name: 'VentaView', params: { ventaId: detalleVenta.venta.id } }">{{ detalleVenta.venta.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Auto</span>
          </dt>
          <dd>
            <div v-if="detalleVenta.auto">
              <router-link :to="{ name: 'AutoView', params: { autoId: detalleVenta.auto.id } }">{{ detalleVenta.auto.id }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link
          v-if="detalleVenta.id"
          :to="{ name: 'DetalleVentaEdit', params: { detalleVentaId: detalleVenta.id } }"
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

<script lang="ts" src="./detalle-venta-details.component.ts"></script>
