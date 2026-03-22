<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="combustible">
        <h2 class="jh-entity-heading" data-cy="combustibleDetailsHeading"><span>CombustibleCombustible</span> {{ combustible.id }}</h2>
        <dl class="row-md jh-entity-details">
          <dt>
            <span>Nombre</span>
          </dt>
          <dd>
            <span>{{ combustible.nombre }}</span>
          </dd>
          <dt>
            <span>Descripcion</span>
          </dt>
          <dd>
            <span>{{ combustible.descripcion }}</span>
          </dd>
          <dt>
            <span>Motor</span>
          </dt>
          <dd>
            <div v-if="combustible.motor">
              <router-link :to="{ name: 'MotorView', params: { motorId: combustible.motor.id } }">{{ combustible.motor.id }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
        </button>
        <router-link
          v-if="combustible.id"
          :to="{ name: 'CombustibleEdit', params: { combustibleId: combustible.id } }"
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

<script lang="ts" src="./combustible-details.component.ts"></script>
