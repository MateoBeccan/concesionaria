<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="tipoCaja">
        <h2 class="jh-entity-heading" data-cy="tipoCajaDetailsHeading"><span>Tipo CajaTipoCaja</span> {{ tipoCaja.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Nombre</span>
          </dt>
          <dd>
            <span>{{ tipoCaja.nombre }}</span>
          </dd>
          <dt>
            <span>Descripcion</span>
          </dt>
          <dd>
            <span>{{ tipoCaja.descripcion }}</span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="tipoCaja.id" :to="{ name: 'TipoCajaEdit', params: { tipoCajaId: tipoCaja.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./tipo-caja-details.component.ts"></script>
