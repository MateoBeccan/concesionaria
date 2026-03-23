<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="moneda">
        <h2 class="jh-entity-heading" data-cy="monedaDetailsHeading"><span>MonedaMoneda</span> {{ moneda.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Codigo</span>
          </dt>
          <dd>
            <span>{{ moneda.codigo }}</span>
          </dd>
          <dt>
            <span>Descripcion</span>
          </dt>
          <dd>
            <span>{{ moneda.descripcion }}</span>
          </dd>
          <dt>
            <span>Simbolo</span>
          </dt>
          <dd>
            <span>{{ moneda.simbolo }}</span>
          </dd>
          <dt>
            <span>Activo</span>
          </dt>
          <dd>
            <span>{{ moneda.activo }}</span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="moneda.id" :to="{ name: 'MonedaEdit', params: { monedaId: moneda.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./moneda-details.component.ts"></script>
