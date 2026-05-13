<template>
  <div class="container-fluid px-0">
    <div class="card border-0 shadow-sm mb-3" data-cy="MarcaHeading">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <h2 class="h4 mb-1">Marcas</h2>
          <p class="text-muted mb-0">Gestión del catálogo de marcas.</p>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-outline-primary" @click="handleSyncList" :disabled="isFetching">
            <font-awesome-icon icon="sync" :spin="isFetching" class="me-2"></font-awesome-icon>
            Actualizar
          </button>
          <router-link :to="{ name: 'MarcaCreate' }" custom v-slot="{ navigate }">
            <button @click="navigate" id="jh-create-entity" data-cy="entityCreateButton" class="btn btn-primary jh-create-entity create-marca">
              <font-awesome-icon icon="plus"></font-awesome-icon>
              <span class="ms-1">Nueva marca</span>
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
            <input v-model="filters.q" type="text" class="form-control form-control-sm" placeholder="ID, nombre o país..." />
          </div>
          <div class="col-12 col-md-5">
            <label class="form-label mb-1">País de origen</label>
            <input v-model="filters.pais" type="text" class="form-control form-control-sm" placeholder="Argentina, Japón..." />
          </div>
          <div class="col-12 col-md-2">
            <button class="btn btn-light btn-sm w-100" @click="resetFilters">Limpiar filtros</button>
          </div>
        </div>
      </div>
    </div>

    <div class="alert alert-warning" v-if="!isFetching && filteredMarcas?.length === 0">
      <span>No se encontraron marcas con los filtros actuales.</span>
    </div>

    <div class="table-responsive" v-if="filteredMarcas?.length > 0">
      <table class="table table-striped" aria-describedby="marcas">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')"><span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'" /></th>
            <th scope="col" @click="changeOrder('nombre')">
              <span>Nombre</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nombre'" />
            </th>
            <th scope="col" @click="changeOrder('paisOrigen')">
              <span>País origen</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'paisOrigen'" />
            </th>
            <th scope="col" @click="changeOrder('createdDate')">
              <span>Creado</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'" />
            </th>
            <th scope="col" @click="changeOrder('lastModifiedDate')">
              <span>Actualizado</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastModifiedDate'" />
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="marca in filteredMarcas" :key="marca.id" data-cy="entityTable">
            <td><router-link :to="{ name: 'MarcaView', params: { marcaId: marca.id } }">{{ marca.id }}</router-link></td>
            <td>{{ marca.nombre }}</td>
            <td>{{ marca.paisOrigen || '-' }}</td>
            <td>{{ formatDateShort(marca.createdDate) || '-' }}</td>
            <td>{{ formatDateShort(marca.lastModifiedDate) || '-' }}</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'MarcaView', params: { marcaId: marca.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton"><font-awesome-icon icon="eye" /><span class="d-none d-md-inline">Vista</span></button>
                </router-link>
                <router-link :to="{ name: 'MarcaEdit', params: { marcaId: marca.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton"><font-awesome-icon icon="pencil-alt" /><span class="d-none d-md-inline">Editar</span></button>
                </router-link>
                <b-button @click="prepareRemove(marca)" variant="danger" class="btn btn-sm" data-cy="entityDeleteButton" v-b-modal.removeEntity>
                  <font-awesome-icon icon="times" /><span class="d-none d-md-inline">Eliminar</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <b-modal ref="removeEntity" id="removeEntity">
      <template #title><span data-cy="marcaDeleteDialogHeading">Confirmar operación de borrado</span></template>
      <div class="modal-body"><p>¿Seguro que quiere eliminar Marca {{ removeId }}?</p></div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button type="button" class="btn btn-primary" data-cy="entityConfirmDeleteButton" @click="removeMarca">Eliminar</button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./marca.component.ts"></script>
