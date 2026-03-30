<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="vehiculo">

        <!-- TITULO LIMPIO -->
        <h2 class="jh-entity-heading">
          {{ vehiculo.patente }}
        </h2>

        <dl class="row-md jh-entity-details">

          <!-- ESTADO -->
          <dt>Estado</dt>
          <dd>
            <span
              class="badge"
              :class="{
                'bg-secondary': vehiculo.estado === 'USADO',
                'bg-success': vehiculo.estado === 'NUEVO'
              }"
            >
              {{ vehiculo.estado }}
            </span>
          </dd>
          <dt>Marca</dt>
<dd>
  <span v-if="vehiculo.version?.modelo?.marca">
    {{ vehiculo.version.modelo.marca.nombre }}
  </span>
</dd>

<dt>Modelo</dt>
<dd>
  <span v-if="vehiculo.version?.modelo">
    {{ vehiculo.version.modelo.nombre }}
  </span>
</dd>

          <dt>Fecha Fabricación</dt>
          <dd>{{ vehiculo.fechaFabricacion }}</dd>

          <dt>Kilómetros</dt>
          <dd>{{ vehiculo.km }}</dd>

          <dt>Precio</dt>
          <dd>
            {{ vehiculo.precio }}
          </dd>

          <!--  VERSION -->
          <dt>Versión</dt>
          <dd>
            <span v-if="vehiculo.version">
              {{ vehiculo.version.nombre }}
            </span>
          </dd>

          <!-- 🔥 MOTOR -->
          <dt>Motor</dt>
          <dd>
            <span v-if="vehiculo.motor">
              {{ vehiculo.motor.nombre }}
              ({{ vehiculo.motor.potenciaHp }} HP - {{ vehiculo.motor.cilindradaCc }} cc)
            </span>
          </dd>

          <!-- TIPO -->
          <dt>Tipo Vehículo</dt>
          <dd>
            <span v-if="vehiculo.tipoVehiculo">
              {{ vehiculo.tipoVehiculo.nombre }}
            </span>
          </dd>

          <!-- AUDITORIA (MAS LIMPIO) -->
          <dt>Fecha Alta</dt>
          <dd v-if="vehiculo.createdDate">
            {{ formatDateLong(vehiculo.createdDate) }}
          </dd>

          <dt v-if="vehiculo.lastModifiedDate">Última Modificación</dt>
          <dd v-if="vehiculo.lastModifiedDate">
            {{ formatDateLong(vehiculo.lastModifiedDate) }}
          </dd>

        </dl>

        <!-- BOTONES -->
        <div class="mt-3">
          <button @click.prevent="previousState()" class="btn btn-info">
            <font-awesome-icon icon="arrow-left"></font-awesome-icon>
            Volver
          </button>

          <router-link
            v-if="vehiculo.id"
            :to="{ name: 'VehiculoEdit', params: { vehiculoId: vehiculo.id } }"
          >
            <button class="btn btn-primary ms-2">
              <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
              Editar
            </button>
          </router-link>
        </div>

      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./vehiculo-details.component.ts"></script>
