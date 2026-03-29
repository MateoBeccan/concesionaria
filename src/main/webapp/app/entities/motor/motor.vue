<template>
  <div>
    <h2 id="page-heading" data-cy="MotorHeading">
      <span id="motor">Motors</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link :to="{ name: 'MotorCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-motor"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span>Crear nuevo Motor</span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && motors?.length === 0">
      <span>Ningún Motors encontrado</span>
    </div>
    <div class="table-responsive" v-if="motors?.length > 0">
      <table class="table table-striped" aria-describedby="motors">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('nombre')">
              <span>Nombre</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nombre'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('cilindradaCc')">
              <span>Cilindrada Cc</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'cilindradaCc'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('cilindroCant')">
              <span>Cilindro Cant</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'cilindroCant'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('potenciaHp')">
              <span>Potencia Hp</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'potenciaHp'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('turbo')">
              <span>Turbo</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'turbo'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('combustible.id')">
              <span>Combustible</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'combustible.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('tipoCaja.id')">
              <span>Tipo Caja</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'tipoCaja.id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('traccion.id')">
              <span>Traccion</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'traccion.id'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="motor in motors" :key="motor.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'MotorView', params: { motorId: motor.id } }">{{ motor.id }}</router-link>
            </td>
            <td>{{ motor.nombre }}</td>
            <td>{{ motor.cilindradaCc }}</td>
            <td>{{ motor.cilindroCant }}</td>
            <td>{{ motor.potenciaHp }}</td>
            <td>{{ motor.turbo }}</td>
            <td>
              <div v-if="motor.combustible">
                <router-link :to="{ name: 'CombustibleView', params: { combustibleId: motor.combustible.id } }">{{
                  motor.combustible.id
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="motor.tipoCaja">
                <router-link :to="{ name: 'TipoCajaView', params: { tipoCajaId: motor.tipoCaja.id } }">{{ motor.tipoCaja.id }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="motor.traccion">
                <router-link :to="{ name: 'TraccionView', params: { traccionId: motor.traccion.id } }">{{ motor.traccion.id }}</router-link>
              </div>
            </td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'MotorView', params: { motorId: motor.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'MotorEdit', params: { motorId: motor.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(motor)"
                  variant="danger"
                  class="btn btn-sm"
                  data-cy="entityDeleteButton"
                  v-b-modal.removeEntity
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                  <span class="d-none d-md-inline">Eliminar</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <b-modal ref="removeEntity" id="removeEntity">
      <template #title>
        <span id="concesionariaApp.motor.delete.question" data-cy="motorDeleteDialogHeading">Confirmar operación de borrado</span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-motor-heading">¿Seguro que quiere eliminar Motor {{ removeId }}?</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-motor"
            data-cy="entityConfirmDeleteButton"
            @click="removeMotor"
          >
            Eliminar
          </button>
        </div>
      </template>
    </b-modal>
    <div v-show="motors?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./motor.component.ts"></script>
