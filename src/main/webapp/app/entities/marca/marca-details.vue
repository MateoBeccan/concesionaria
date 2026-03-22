<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="marca">
        <h2 class="jh-entity-heading" data-cy="marcaDetailsHeading"><span>MarcaMarca</span> {{ marca.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Nombre</span>
          </dt>
          <dd>
            <span>{{ marca.nombre }}</span>
          </dd>
          <dt>
            <span>Pais Origen</span>
          </dt>
          <dd>
            <span>{{ marca.paisOrigen }}</span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="marca.id" :to="{ name: 'MarcaEdit', params: { marcaId: marca.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./marca-details.component.ts"></script>
