<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="version">
        <h2 class="jh-entity-heading" data-cy="versionDetailsHeading"><span>VersionVersion</span> {{ version.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Nombre</span>
          </dt>
          <dd>
            <span>{{ version.nombre }}</span>
          </dd>
          <dt>
            <span>Descripcion</span>
          </dt>
          <dd>
            <span>{{ version.descripcion }}</span>
          </dd>
          <dt>
            <span>Anio Inicio</span>
          </dt>
          <dd>
            <span>{{ version.anioInicio }}</span>
          </dd>
          <dt>
            <span>Anio Fin</span>
          </dt>
          <dd>
            <span>{{ version.anioFin }}</span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="version.id" :to="{ name: 'VersionEdit', params: { versionId: version.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./version-details.component.ts"></script>
