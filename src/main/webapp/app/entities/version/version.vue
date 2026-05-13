<template>
  <div class="container-fluid px-0">
    <div class="card border-0 shadow-sm mb-3" data-cy="VersionHeading">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <h2 class="h4 mb-1">Versiones</h2>
          <p class="text-muted mb-0">Gestión de versiones comerciales de cada modelo.</p>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-outline-primary" @click="handleSyncList" :disabled="isFetching">
            <font-awesome-icon icon="sync" :spin="isFetching" class="me-2"></font-awesome-icon>
            Actualizar
          </button>
          <router-link :to="{ name: 'VersionCreate' }" custom v-slot="{ navigate }">
            <button @click="navigate" id="jh-create-entity" data-cy="entityCreateButton" class="btn btn-primary jh-create-entity create-version">
              <font-awesome-icon icon="plus"></font-awesome-icon>
              <span class="ms-1">Nueva versión</span>
            </button>
          </router-link>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-2 align-items-end">
          <div class="col-12 col-md-4">
            <label class="form-label mb-1">Búsqueda</label>
            <input v-model="filters.q" type="text" class="form-control form-control-sm" placeholder="ID, versión o descripción..." />
          </div>
          <div class="col-12 col-md-3">
            <label class="form-label mb-1">Modelo</label>
            <input v-model="filters.modelo" type="text" class="form-control form-control-sm" placeholder="Corolla, Hilux..." />
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label mb-1">Año inicio</label>
            <input v-model="filters.anioInicio" type="number" class="form-control form-control-sm" placeholder="2020" />
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label mb-1">Año fin</label>
            <input v-model="filters.anioFin" type="number" class="form-control form-control-sm" placeholder="2025" />
          </div>
          <div class="col-12 col-md-1">
            <button class="btn btn-light btn-sm w-100" @click="resetFilters">Limpiar</button>
          </div>
        </div>
      </div>
    </div>

    <div class="alert alert-warning" v-if="!isFetching && filteredVersions?.length === 0">
      <span>No se encontraron versiones con los filtros actuales.</span>
    </div>

    <div class="table-responsive" v-if="filteredVersions?.length > 0">
      <table class="table table-striped" aria-describedby="versions">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')"><span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'" /></th>
            <th scope="col" @click="changeOrder('nombre')"><span>Nombre</span><jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nombre'" /></th>
            <th scope="col" @click="changeOrder('descripcion')"><span>Descripción</span><jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'descripcion'" /></th>
            <th scope="col" @click="changeOrder('anioInicio')"><span>Año inicio</span><jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'anioInicio'" /></th>
            <th scope="col" @click="changeOrder('anioFin')"><span>Año fin</span><jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'anioFin'" /></th>
            <th scope="col" @click="changeOrder('modelo.id')"><span>Modelo</span><jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'modelo.id'" /></th>
            <th scope="col" class="text-end"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="version in filteredVersions" :key="version.id" data-cy="entityTable">
            <td><router-link :to="{ name: 'VersionView', params: { versionId: version.id } }">{{ version.id }}</router-link></td>
            <td>{{ version.nombre }}</td>
            <td>{{ version.descripcion || '-' }}</td>
            <td>{{ version.anioInicio }}</td>
            <td>{{ version.anioFin ?? '-' }}</td>
            <td><span class="badge bg-primary">{{ version.modelo?.nombre ?? '-' }}</span></td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'VersionView', params: { versionId: version.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton"><font-awesome-icon icon="eye" /><span class="d-none d-md-inline">Vista</span></button>
                </router-link>
                <router-link :to="{ name: 'VersionEdit', params: { versionId: version.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton"><font-awesome-icon icon="pencil-alt" /><span class="d-none d-md-inline">Editar</span></button>
                </router-link>
                <b-button @click="prepareRemove(version)" variant="danger" class="btn btn-sm" data-cy="entityDeleteButton" v-b-modal.removeEntity>
                  <font-awesome-icon icon="times" /><span class="d-none d-md-inline">Eliminar</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <b-modal ref="removeEntity" id="removeEntity">
      <template #title><span data-cy="versionDeleteDialogHeading">Confirmar operación de borrado</span></template>
      <div class="modal-body"><p>¿Seguro que quiere eliminar Versión {{ removeId }}?</p></div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button type="button" class="btn btn-primary" data-cy="entityConfirmDeleteButton" @click="removeVersion">Eliminar</button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./version.component.ts"></script>
