<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="auto">
        <h2 class="jh-entity-heading" data-cy="autoDetailsHeading"><span>AutoAuto</span> {{ auto.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Estado</span>
          </dt>
          <dd>
            <span>{{ auto.estado }}</span>
          </dd>
          <dt>
            <span>Condicion</span>
          </dt>
          <dd>
            <span>{{ auto.condicion }}</span>
          </dd>
          <dt>
            <span>Fecha Fabricacion</span>
          </dt>
          <dd>
            <span>{{ auto.fechaFabricacion }}</span>
          </dd>
          <dt>
            <span>Fecha Ingreso</span>
          </dt>
          <dd>
            <span>{{ auto.fechaIngreso }}</span>
          </dd>
          <dt>
            <span>Km</span>
          </dt>
          <dd>
            <span>{{ auto.km }}</span>
          </dd>
          <dt>
            <span>Patente</span>
          </dt>
          <dd>
            <span>{{ auto.patente }}</span>
          </dd>
          <dt>
            <span>Precio</span>
          </dt>
          <dd>
            <span>{{ auto.precio }}</span>
          </dd>
          <dt>
            <span>Marca</span>
          </dt>
          <dd>
            <div v-if="auto.marca">
              <router-link :to="{ name: 'MarcaView', params: { marcaId: auto.marca.id } }">{{ auto.marca.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Modelo</span>
          </dt>
          <dd>
            <div v-if="auto.modelo">
              <router-link :to="{ name: 'ModeloView', params: { modeloId: auto.modelo.id } }">{{ auto.modelo.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Version</span>
          </dt>
          <dd>
            <div v-if="auto.version">
              <router-link :to="{ name: 'VersionView', params: { versionId: auto.version.id } }">{{ auto.version.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Motor</span>
          </dt>
          <dd>
            <div v-if="auto.motor">
              <router-link :to="{ name: 'MotorView', params: { motorId: auto.motor.id } }">{{ auto.motor.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Moneda</span>
          </dt>
          <dd>
            <div v-if="auto.moneda">
              <router-link :to="{ name: 'MonedaView', params: { monedaId: auto.moneda.id } }">{{ auto.moneda.id }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="auto.id" :to="{ name: 'AutoEdit', params: { autoId: auto.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./auto-details.component.ts"></script>
