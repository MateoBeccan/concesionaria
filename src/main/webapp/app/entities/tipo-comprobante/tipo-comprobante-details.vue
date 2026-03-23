<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="tipoComprobante">
        <h2 class="jh-entity-heading" data-cy="tipoComprobanteDetailsHeading">
          <span>Tipo ComprobanteTipoComprobante</span> {{ tipoComprobante.id }}
        </h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Codigo</span>
          </dt>
          <dd>
            <span>{{ tipoComprobante.codigo }}</span>
          </dd>
          <dt>
            <span>Descripcion</span>
          </dt>
          <dd>
            <span>{{ tipoComprobante.descripcion }}</span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link
          v-if="tipoComprobante.id"
          :to="{ name: 'TipoComprobanteEdit', params: { tipoComprobanteId: tipoComprobante.id } }"
          custom
          v-slot="{ navigate }"
        >
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./tipo-comprobante-details.component.ts"></script>
