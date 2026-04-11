<template>
  <div class="container-fluid">

    <!-- HEADER -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="mb-0">Clientes</h3>
        <small class="text-muted">Gestión de clientes del sistema</small>
      </div>

      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" />
        </button>

        <router-link :to="{ name: 'ClienteCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="plus" />
            Nuevo Cliente
          </button>
        </router-link>
      </div>
    </div>

    <!-- 🔍 FILTROS -->
    <div class="card shadow-sm p-3 mb-4">
      <div class="row g-3">

        <div class="col-md-4">
          <input
            v-model="search"
            class="form-control"
            placeholder="Buscar por nombre, email o documento"
          />
        </div>

        <div class="col-md-3">
          <select v-model="filtroActivo" class="form-control">
            <option value="">Todos</option>
            <option value="true">Activos</option>
            <option value="false">Inactivos</option>
          </select>
        </div>

        <div class="col-md-3">
          <input v-model="filtroCiudad" class="form-control" placeholder="Ciudad" />
        </div>

        <div class="col-md-2 d-grid">
          <button class="btn btn-outline-secondary" @click="resetFiltros">
            Limpiar
          </button>
        </div>

      </div>
    </div>

    <!-- EMPTY -->
    <div class="alert alert-warning" v-if="!isFetching && filteredClientes.length === 0">
      No hay resultados
    </div>

    <!-- TABLE -->
    <div class="card shadow-sm" v-if="filteredClientes.length > 0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">

          <thead class="table-light">
            <tr>
              <th>#</th>
              <th>Cliente</th>
              <th>Email</th>
              <th>Teléfono</th>
              <th>Ubicación</th>
              <th>Estado</th>
              <th>Fecha Alta</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="cliente in filteredClientes" :key="cliente.id">

              <td>{{ cliente.id }}</td>

              <td>
                <div class="fw-semibold">
                  {{ cliente.nombre }} {{ cliente.apellido }}
                </div>
                <small class="text-muted">{{ cliente.nroDocumento }}</small>
              </td>

              <td>{{ cliente.email }}</td>

              <td>{{ cliente.telefono || '-' }}</td>

              <td>
                {{ cliente.ciudad || '-' }},
                {{ cliente.provincia || '-' }}
              </td>

              <td>
                <span class="badge" :class="cliente.activo ? 'bg-success' : 'bg-secondary'">
                  {{ cliente.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </td>

              <td>{{ formatDateShort(cliente.fechaAlta) || '-' }}</td>

              <td class="text-end">
                <router-link :to="{ name: 'ClienteView', params: { clienteId: cliente.id } }">
                  <button class="btn btn-sm btn-outline-info">Ver</button>
                </router-link>
              </td>

            </tr>
          </tbody>

        </table>
      </div>
    </div>

  </div>
</template>

<script lang="ts" src="./cliente.component.ts"></script>
