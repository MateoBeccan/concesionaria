<template>
  <div class="container-fluid px-0">
    <div class="card border-0 shadow-sm mb-3" data-cy="ModeloHeading">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <h2 class="h4 mb-1">Modelos</h2>
          <p class="text-muted mb-0">Gestión de modelos por marca y carrocería.</p>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-outline-primary" @click="handleSyncList" :disabled="isFetching">
            <font-awesome-icon icon="sync" :spin="isFetching" class="me-2"></font-awesome-icon>
            Actualizar
          </button>
          <router-link :to="{ name: 'ModeloCreate' }" custom v-slot="{ navigate }">
            <button @click="navigate" id="jh-create-entity" data-cy="entityCreateButton" class="btn btn-primary jh-create-entity create-modelo">
              <font-awesome-icon icon="plus"></font-awesome-icon>
              <span class="ms-1">Nuevo modelo</span>
            </button>
          </router-link>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-2 align-items-end">
          <div class="col-12 col-md-5">
            <label class="form-label mb-1">Búsqueda</label>
            <input v-model="filters.q" type="text" class="form-control form-control-sm" placeholder="ID, modelo o marca..." />
          </div>
          <div class="col-12 col-md-3">
            <label class="form-label mb-1">Marca</label>
            <input v-model="filters.marca" type="text" class="form-control form-control-sm" placeholder="Toyota, Ford..." />
          </div>
          <div class="col-12 col-md-2">
            <label class="form-label mb-1">Año</label>
            <input v-model="filters.anio" type="number" class="form-control form-control-sm" placeholder="2024" />
          </div>
          <div class="col-12 col-md-2">
            <button class="btn btn-light btn-sm w-100" @click="resetFilters">Limpiar filtros</button>
          </div>
        </div>
      </div>
    </div>

    <div class="alert alert-warning" v-if="!isFetching && filteredModelos?.length === 0">
      <span>No se encontraron modelos con los filtros actuales.</span>
    </div>

    <div class="table-responsive" v-if="filteredModelos?.length > 0">
      <table class="table table-striped" aria-describedby="modelos">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')"><span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'" /></th>
            <th scope="col" @click="changeOrder('nombre')"><span>Nombre</span><jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nombre'" /></th>
            <th scope="col" @click="changeOrder('anioLanzamiento')"><span>Año lanzamiento</span><jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'anioLanzamiento'" /></th>
            <th scope="col" @click="changeOrder('marca.id')"><span>Marca</span><jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'marca.id'" /></th>
            <th scope="col" @click="changeOrder('carroceria.id')"><span>Carrocería</span><jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'carroceria.id'" /></th>
            <th scope="col" class="text-end"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="modelo in filteredModelos" :key="modelo.id" data-cy="entityTable">
            <td><router-link :to="{ name: 'ModeloView', params: { modeloId: modelo.id } }">{{ modelo.id }}</router-link></td>
            <td>{{ modelo.nombre }}</td>
            <td>{{ modelo.anioLanzamiento }}</td>
            <td>{{ modelo.marca?.nombre ?? '-' }}</td>
            <td>{{ modelo.carroceria?.nombre ?? '-' }}</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'ModeloView', params: { modeloId: modelo.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton"><font-awesome-icon icon="eye" /><span class="d-none d-md-inline">Vista</span></button>
                </router-link>
                <router-link :to="{ name: 'ModeloEdit', params: { modeloId: modelo.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton"><font-awesome-icon icon="pencil-alt" /><span class="d-none d-md-inline">Editar</span></button>
                </router-link>
                <b-button @click="prepareRemove(modelo)" variant="danger" class="btn btn-sm" data-cy="entityDeleteButton" v-b-modal.removeEntity>
                  <font-awesome-icon icon="times" /><span class="d-none d-md-inline">Eliminar</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <b-modal ref="removeEntity" id="removeEntity">
      <template #title><span data-cy="modeloDeleteDialogHeading">Confirmar operación de borrado</span></template>
      <div class="modal-body"><p>¿Seguro que quiere eliminar Modelo {{ removeId }}?</p></div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button type="button" class="btn btn-primary" data-cy="entityConfirmDeleteButton" @click="removeModelo">Eliminar</button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./modelo.component.ts"></script>
