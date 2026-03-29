<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="inventario">
        <h2 class="jh-entity-heading" data-cy="inventarioDetailsHeading"><span>InventarioInventario</span> {{ inventario.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Fecha Ingreso</span>
          </dt>
          <dd>
            <span v-if="inventario.fechaIngreso">{{ formatDateLong(inventario.fechaIngreso) }}</span>
          </dd>
          <dt>
            <span>Ubicacion</span>
          </dt>
          <dd>
            <span>{{ inventario.ubicacion }}</span>
          </dd>
          <dt>
            <span>Estado Inventario</span>
          </dt>
          <dd>
            <span>{{ inventario.estadoInventario }}</span>
          </dd>
          <dt>
            <span>Observaciones</span>
          </dt>
          <dd>
            <span>{{ inventario.observaciones }}</span>
          </dd>
          <dt>
            <span>Disponible</span>
          </dt>
          <dd>
            <span>{{ inventario.disponible }}</span>
          </dd>
          <dt>
            <span>Created Date</span>
          </dt>
          <dd>
            <span v-if="inventario.createdDate">{{ formatDateLong(inventario.createdDate) }}</span>
          </dd>
          <dt>
            <span>Last Modified Date</span>
          </dt>
          <dd>
            <span v-if="inventario.lastModifiedDate">{{ formatDateLong(inventario.lastModifiedDate) }}</span>
          </dd>
          <dt>
            <span>Fecha Reserva</span>
          </dt>
          <dd>
            <span v-if="inventario.fechaReserva">{{ formatDateLong(inventario.fechaReserva) }}</span>
          </dd>
          <dt>
            <span>Fecha Vencimiento Reserva</span>
          </dt>
          <dd>
            <span v-if="inventario.fechaVencimientoReserva">{{ formatDateLong(inventario.fechaVencimientoReserva) }}</span>
          </dd>
          <dt>
            <span>Vehiculo</span>
          </dt>
          <dd>
            <div v-if="inventario.vehiculo">
              <router-link :to="{ name: 'VehiculoView', params: { vehiculoId: inventario.vehiculo.id } }">{{
                inventario.vehiculo.id
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Cliente Reserva</span>
          </dt>
          <dd>
            <div v-if="inventario.clienteReserva">
              <router-link :to="{ name: 'ClienteView', params: { clienteId: inventario.clienteReserva.id } }">{{
                inventario.clienteReserva.id
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link
          v-if="inventario.id"
          :to="{ name: 'InventarioEdit', params: { inventarioId: inventario.id } }"
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

<script lang="ts" src="./inventario-details.component.ts"></script>
