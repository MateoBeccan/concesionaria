<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="motor">
        <h2 class="jh-entity-heading" data-cy="motorDetailsHeading"><span>MotorMotor</span> {{ motor.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Nombre</span>
          </dt>
          <dd>
            <span>{{ motor.nombre }}</span>
          </dd>
          <dt>
            <span>Cilindrada Cc</span>
          </dt>
          <dd>
            <span>{{ motor.cilindradaCc }}</span>
          </dd>
          <dt>
            <span>Cilindro Cant</span>
          </dt>
          <dd>
            <span>{{ motor.cilindroCant }}</span>
          </dd>
          <dt>
            <span>Potencia Hp</span>
          </dt>
          <dd>
            <span>{{ motor.potenciaHp }}</span>
          </dd>
          <dt>
            <span>Turbo</span>
          </dt>
          <dd>
            <span>{{ motor.turbo }}</span>
          </dd>
          <dt>
            <span>Versiones</span>
          </dt>
          <dd>
            <span v-for="(versiones, i) in motor.versioneses" :key="versiones.id"
              >{{ i > 0 ? ', ' : '' }}
              <router-link :to="{ name: 'VersionView', params: { versionId: versiones.id } }">{{ versiones.id }}</router-link>
            </span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link v-if="motor.id" :to="{ name: 'MotorEdit', params: { motorId: motor.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span>Editar</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./motor-details.component.ts"></script>
