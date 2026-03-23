<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="metodoPago">
        <h2 class="jh-entity-heading" data-cy="metodoPagoDetailsHeading"><span>Metodo PagoMetodoPago</span> {{ metodoPago.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Codigo</span>
          </dt>
          <dd>
            <span>{{ metodoPago.codigo }}</span>
          </dd>
          <dt>
            <span>Descripcion</span>
          </dt>
          <dd>
            <span>{{ metodoPago.descripcion }}</span>
          </dd>
          <dt>
            <span>Activo</span>
          </dt>
          <dd>
            <span>{{ metodoPago.activo }}</span>
          </dd>
          <dt>
            <span>Requiere Referencia</span>
          </dt>
          <dd>
            <span>{{ metodoPago.requiereReferencia }}</span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link
          v-if="metodoPago.id"
          :to="{ name: 'MetodoPagoEdit', params: { metodoPagoId: metodoPago.id } }"
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

<script lang="ts" src="./metodo-pago-details.component.ts"></script>
