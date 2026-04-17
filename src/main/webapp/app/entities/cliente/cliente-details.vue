<template>
  <div class="d-flex justify-content-center">
    <div class="col-10 col-lg-8">
      <div v-if="cliente" class="card shadow-sm p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 class="mb-0">{{ cliente.nombre }} {{ cliente.apellido }}</h3>
            <small class="text-muted">ID: {{ cliente.id }}</small>
          </div>

          <span class="badge" :class="cliente.activo ? 'bg-success' : 'bg-secondary'">
            {{ cliente.activo ? 'Activo' : 'Inactivo' }}
          </span>
        </div>

        <h5 class="border-bottom pb-2 mb-3">Datos personales</h5>
        <div class="row mb-3">
          <div class="col-md-6">
            <strong>Documento:</strong>
            <div>{{ cliente.nroDocumento }}</div>
          </div>

          <div class="col-md-6">
            <strong>Condicion IVA:</strong>
            <div v-if="cliente.condicionIva">
              <router-link :to="{ name: 'CondicionIvaView', params: { condicionIvaId: cliente.condicionIva.id } }">
                {{ cliente.condicionIva.descripcion || cliente.condicionIva.id }}
              </router-link>
            </div>
          </div>
        </div>

        <h5 class="border-bottom pb-2 mb-3">Contacto</h5>
        <div class="row mb-3">
          <div class="col-md-6">
            <strong>Telefono:</strong>
            <div>{{ cliente.telefono || '-' }}</div>
          </div>

          <div class="col-md-6">
            <strong>Email:</strong>
            <div>{{ cliente.email }}</div>
          </div>
        </div>

        <div class="row mb-3">
          <div class="col-md-12">
            <strong>Direccion:</strong>
            <div>
              {{ cliente.direccion || '-' }},
              {{ cliente.ciudad || '-' }},
              {{ cliente.provincia || '-' }},
              {{ cliente.pais || '-' }}
            </div>
          </div>
        </div>

        <h5 class="border-bottom pb-2 mb-3">Informacion del sistema</h5>
        <div class="row mb-3 g-3">
          <div class="col-md-4">
            <strong>Fecha alta:</strong>
            <div>{{ cliente.fechaAlta ? formatDateLong(cliente.fechaAlta) : '-' }}</div>
          </div>

          <div class="col-md-4">
            <strong>Creado:</strong>
            <div>{{ cliente.createdDate ? formatDateLong(cliente.createdDate) : '-' }}</div>
          </div>

          <div class="col-md-4">
            <strong>Creado por:</strong>
            <div>{{ cliente.createdBy || '-' }}</div>
          </div>

          <div class="col-md-4">
            <strong>Ultima modificacion:</strong>
            <div>{{ cliente.lastModifiedDate ? formatDateLong(cliente.lastModifiedDate) : '-' }}</div>
          </div>

          <div class="col-md-4">
            <strong>Modificado por:</strong>
            <div>{{ cliente.lastModifiedBy || '-' }}</div>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button @click.prevent="previousState()" class="btn btn-outline-secondary">
            <font-awesome-icon icon="arrow-left" />
            Volver
          </button>

          <router-link v-if="cliente.id" :to="{ name: 'ClienteEdit', params: { clienteId: cliente.id } }" custom v-slot="{ navigate }">
            <button @click="navigate" class="btn btn-primary">
              <font-awesome-icon icon="pencil-alt" />
              Editar
            </button>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./cliente-details.component.ts"></script>
