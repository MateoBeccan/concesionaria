<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="vehiculo">
        <h2 class="jh-entity-heading" data-cy="vehiculoDetailsHeading"><span>VehiculoVehiculo</span> {{ vehiculo.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Estado</span>
          </dt>
          <dd>
            <span>{{ vehiculo.estado }}</span>
          </dd>
          <dt>
            <span>Fecha Fabricacion</span>
          </dt>
          <dd>
            <span>{{ vehiculo.fechaFabricacion }}</span>
          </dd>
          <dt>
            <span>Km</span>
          </dt>
          <dd>
            <span>{{ vehiculo.km }}</span>
          </dd>
          <dt>
            <span>Patente</span>
          </dt>
          <dd>
            <span>{{ vehiculo.patente }}</span>
          </dd>
          <dt>
            <span>Precio</span>
          </dt>
          <dd>
            <span>{{ vehiculo.precio }}</span>
          </dd>
          <dt>
            <span>Created Date</span>
          </dt>
          <dd>
            <span v-if="vehiculo.createdDate">{{ formatDateLong(vehiculo.createdDate) }}</span>
          </dd>
          <dt>
            <span>Last Modified Date</span>
          </dt>
          <dd>
            <span v-if="vehiculo.lastModifiedDate">{{ formatDateLong(vehiculo.lastModifiedDate) }}</span>
          </dd>
          <dt>
            <span>Version</span>
          </dt>
          <dd>
            <div v-if="vehiculo.version">
              <router-link :to="{ name: 'VersionView', params: { versionId: vehiculo.version.id } }">{{ vehiculo.version.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Motor</span>
          </dt>
          <dd>
            <div v-if="vehiculo.motor">
              <router-link :to="{ name: 'MotorView', params: { motorId: vehiculo.motor.id } }">{{ vehiculo.motor.id }}</router-link>
            </div>
          </dd>
          <dt>
            <span>Tipo Vehiculo</span>
          </dt>
          <dd>
            <div v-if="vehiculo.tipoVehiculo">
              <router-link :to="{ name: 'TipoVehiculoView', params: { tipoVehiculoId: vehiculo.tipoVehiculo.id } }">{{
                vehiculo.tipoVehiculo.id
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="vehiculo.id" :to="{ name: 'VehiculoEdit', params: { vehiculoId: vehiculo.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./vehiculo-details.component.ts"></script>
