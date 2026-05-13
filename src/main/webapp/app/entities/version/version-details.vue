<template>
  <div class="container-fluid px-0" v-if="version">
    <div class="card border-0 shadow-sm mb-3" data-cy="versionDetailsHeading">
      <div class="card-body">
        <h1 class="h4 mb-1">Versión #{{ version.id }}</h1>
        <p class="text-muted mb-0">Detalle comercial y vigencia de la versión.</p>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-12 col-lg-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Datos principales</h2>
            <dl class="row mb-0">
              <dt class="col-5">Nombre</dt>
              <dd class="col-7">{{ version.nombre || '-' }}</dd>
              <dt class="col-5">Descripción</dt>
              <dd class="col-7">{{ version.descripcion || '-' }}</dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Vigencia y relación</h2>
            <dl class="row mb-0">
              <dt class="col-5">Año inicio</dt>
              <dd class="col-7">{{ version.anioInicio ?? '-' }}</dd>
              <dt class="col-5">Año fin</dt>
              <dd class="col-7">{{ version.anioFin ?? '-' }}</dd>
              <dt class="col-5">Modelo</dt>
              <dd class="col-7">
                <router-link v-if="version.modelo?.id" :to="{ name: 'ModeloView', params: { modeloId: version.modelo.id } }">
                  #{{ version.modelo.id }} {{ version.modelo.nombre ? `- ${version.modelo.nombre}` : '' }}
                </router-link>
                <span v-else>-</span>
              </dd>
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
      <router-link v-if="version.id" :to="{ name: 'VersionEdit', params: { versionId: version.id } }" custom v-slot="{ navigate }">
        <button @click="navigate" class="btn btn-primary">
          <font-awesome-icon icon="pencil-alt" class="me-2"></font-awesome-icon>
          Editar
        </button>
      </router-link>
    </div>
  </div>
</template>

<script lang="ts" src="./version-details.component.ts"></script>
