<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="modelo">
        <h2 class="jh-entity-heading" data-cy="modeloDetailsHeading"><span>ModeloModelo</span> {{ modelo.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Nombre</span>
          </dt>
          <dd>
            <span>{{ modelo.nombre }}</span>
          </dd>
          <dt>
            <span>Anio Lanzamiento</span>
          </dt>
          <dd>
            <span>{{ modelo.anioLanzamiento }}</span>
          </dd>
          <dt>
            <span>Carroceria</span>
          </dt>
          <dd>
            <span>{{ modelo.carroceria }}</span>
          </dd>
          <dt>
            <span>Marca</span>
          </dt>
          <dd>
            <div v-if="modelo.marca">
              <router-link :to="{ name: 'MarcaView', params: { marcaId: modelo.marca.id } }">{{ modelo.marca.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Versiones</span>
          </dt>
          <dd>
            <span v-for="(versiones, i) in modelo.versioneses" :key="versiones.id"
              >{{ i > 0 ? ', ' : '' }}
              <router-link :to="{ name: 'VersionView', params: { versionId: versiones.id } }">{{ versiones.id }}</router-link>
            </span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="modelo.id" :to="{ name: 'ModeloEdit', params: { modeloId: modelo.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./modelo-details.component.ts"></script>
