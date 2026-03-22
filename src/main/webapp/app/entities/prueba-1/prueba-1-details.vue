<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="prueba1">
        <h2 class="jh-entity-heading" data-cy="prueba1DetailsHeading"><span>Prueba 1Prueba1</span> {{ prueba1.id }}</h2>
        <dl class="row-md jh-entity-details"></dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="prueba1.id" :to="{ name: 'Prueba1Edit', params: { prueba1Id: prueba1.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./prueba-1-details.component.ts"></script>
