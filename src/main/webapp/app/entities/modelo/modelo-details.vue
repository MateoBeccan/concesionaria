<template>
  <div class="container-fluid px-0" v-if="modelo">
    <div class="card border-0 shadow-sm mb-3" data-cy="modeloDetailsHeading">
      <div class="card-body">
        <h1 class="h4 mb-1">Modelo #{{ modelo.id }}</h1>
        <p class="text-muted mb-0">Detalle técnico-comercial del modelo.</p>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-12 col-lg-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Datos del modelo</h2>
            <dl class="row mb-0">
              <dt class="col-5">Nombre</dt>
              <dd class="col-7">{{ modelo.nombre || '-' }}</dd>
              <dt class="col-5">Año lanzamiento</dt>
              <dd class="col-7">{{ modelo.anioLanzamiento ?? '-' }}</dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Relaciones</h2>
            <dl class="row mb-0">
              <dt class="col-5">Marca</dt>
              <dd class="col-7">
                <router-link v-if="modelo.marca?.id" :to="{ name: 'MarcaView', params: { marcaId: modelo.marca.id } }">
                  #{{ modelo.marca.id }} {{ modelo.marca.nombre ? `- ${modelo.marca.nombre}` : '' }}
                </router-link>
                <span v-else>-</span>
              </dd>
              <dt class="col-5">Carrocería</dt>
              <dd class="col-7">
                <router-link v-if="modelo.carroceria?.id" :to="{ name: 'CarroceriaView', params: { carroceriaId: modelo.carroceria.id } }">
                  #{{ modelo.carroceria.id }} {{ modelo.carroceria.nombre ? `- ${modelo.carroceria.nombre}` : '' }}
                </router-link>
                <span v-else>-</span>
              </dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="col-12">
        <div class="card border-0 shadow-sm">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Trazabilidad</h2>
            <dl class="row mb-0">
              <dt class="col-12 col-md-3">Creado</dt>
              <dd class="col-12 col-md-9">{{ modelo.createdDate ? formatDateLong(modelo.createdDate) : '-' }}</dd>
              <dt class="col-12 col-md-3">Última modificación</dt>
              <dd class="col-12 col-md-9">{{ modelo.lastModifiedDate ? formatDateLong(modelo.lastModifiedDate) : '-' }}</dd>
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
      <router-link v-if="modelo.id" :to="{ name: 'ModeloEdit', params: { modeloId: modelo.id } }" custom v-slot="{ navigate }">
        <button @click="navigate" class="btn btn-primary">
          <font-awesome-icon icon="pencil-alt" class="me-2"></font-awesome-icon>
          Editar
        </button>
      </router-link>
    </div>
  </div>
</template>

<script lang="ts" src="./modelo-details.component.ts"></script>
