<template>
  <div class="container-fluid px-0" v-if="marca">
    <div class="card border-0 shadow-sm mb-3" data-cy="marcaDetailsHeading">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-start gap-3">
        <div>
          <h1 class="h4 mb-1">Marca #{{ marca.id }}</h1>
          <p class="text-muted mb-0">Detalle institucional de la marca en catálogo.</p>
        </div>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-12 col-lg-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Datos generales</h2>
            <dl class="row mb-0">
              <dt class="col-5">Nombre</dt>
              <dd class="col-7">{{ marca.nombre || '-' }}</dd>
              <dt class="col-5">País de origen</dt>
              <dd class="col-7">{{ marca.paisOrigen || '-' }}</dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Trazabilidad</h2>
            <dl class="row mb-0">
              <dt class="col-5">Creado</dt>
              <dd class="col-7">{{ marca.createdDate ? formatDateLong(marca.createdDate) : '-' }}</dd>
              <dt class="col-5">Última modificación</dt>
              <dd class="col-7">{{ marca.lastModifiedDate ? formatDateLong(marca.lastModifiedDate) : '-' }}</dd>
            </dl>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-3 d-flex gap-2">
      <button type="button" @click.prevent="previousState()" class="btn btn-light" data-cy="entityDetailsBackButton">
        <font-awesome-icon icon="arrow-left" class="me-2"></font-awesome-icon>
        Volver
      </button>
      <router-link v-if="marca.id" :to="{ name: 'MarcaEdit', params: { marcaId: marca.id } }" custom v-slot="{ navigate }">
        <button @click="navigate" class="btn btn-primary">
          <font-awesome-icon icon="pencil-alt" class="me-2"></font-awesome-icon>
          Editar
        </button>
      </router-link>
    </div>
  </div>
</template>

<script lang="ts" src="./marca-details.component.ts"></script>
