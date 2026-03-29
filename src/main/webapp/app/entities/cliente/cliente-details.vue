<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="cliente">
        <h2 class="jh-entity-heading" data-cy="clienteDetailsHeading"><span>ClienteCliente</span> {{ cliente.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Nombre</span>
          </dt>
          <dd>
            <span>{{ cliente.nombre }}</span>
          </dd>
          <dt>
            <span>Apellido</span>
          </dt>
          <dd>
            <span>{{ cliente.apellido }}</span>
          </dd>
          <dt>
            <span>Nro Documento</span>
          </dt>
          <dd>
            <span>{{ cliente.nroDocumento }}</span>
          </dd>
          <dt>
            <span>Telefono</span>
          </dt>
          <dd>
            <span>{{ cliente.telefono }}</span>
          </dd>
          <dt>
            <span>Email</span>
          </dt>
          <dd>
            <span>{{ cliente.email }}</span>
          </dd>
          <dt>
            <span>Direccion</span>
          </dt>
          <dd>
            <span>{{ cliente.direccion }}</span>
          </dd>
          <dt>
            <span>Ciudad</span>
          </dt>
          <dd>
            <span>{{ cliente.ciudad }}</span>
          </dd>
          <dt>
            <span>Provincia</span>
          </dt>
          <dd>
            <span>{{ cliente.provincia }}</span>
          </dd>
          <dt>
            <span>Pais</span>
          </dt>
          <dd>
            <span>{{ cliente.pais }}</span>
          </dd>
          <dt>
            <span>Activo</span>
          </dt>
          <dd>
            <span>{{ cliente.activo }}</span>
          </dd>
          <dt>
            <span>Fecha Alta</span>
          </dt>
          <dd>
            <span v-if="cliente.fechaAlta">{{ formatDateLong(cliente.fechaAlta) }}</span>
          </dd>
          <dt>
            <span>Created Date</span>
          </dt>
          <dd>
            <span v-if="cliente.createdDate">{{ formatDateLong(cliente.createdDate) }}</span>
          </dd>
          <dt>
            <span>Last Modified Date</span>
          </dt>
          <dd>
            <span v-if="cliente.lastModifiedDate">{{ formatDateLong(cliente.lastModifiedDate) }}</span>
          </dd>
          <dt>
            <span>Condicion Iva</span>
          </dt>
          <dd>
            <div v-if="cliente.condicionIva">
              <router-link :to="{ name: 'CondicionIvaView', params: { condicionIvaId: cliente.condicionIva.id } }">{{
                cliente.condicionIva.id
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Tipo Documento</span>
          </dt>
          <dd>
            <div v-if="cliente.tipoDocumento">
              <router-link :to="{ name: 'TipoDocumentoView', params: { tipoDocumentoId: cliente.tipoDocumento.id } }">{{
                cliente.tipoDocumento.id
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="cliente.id" :to="{ name: 'ClienteEdit', params: { clienteId: cliente.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./cliente-details.component.ts"></script>
