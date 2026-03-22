<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <div v-if="user">
        <h2 class="jh-entity-heading" data-cy="userManagementDetailsHeading">
          <span>Usuario</span> [<strong>{{ user.login }}</strong
          >]
        </h2>
        <dl class="row-md jh-entity-details">
          <dt><span>Login</span></dt>
          <dd>
            <span>{{ user.login }}</span>
            <b-badge style="margin-left: 10px" :variant="user.activated ? 'success' : 'danger'"
              >Translation missing for ser.activated ? 'userManagement.activated' : 'userManagement.deactivated</b-badge
            >
          </dd>
          <dt><span>Nombre</span></dt>
          <dd>{{ user.firstName }}</dd>
          <dt><span>Apellidos</span></dt>
          <dd>{{ user.lastName }}</dd>
          <dt><span>Email</span></dt>
          <dd>{{ user.email }}</dd>
          <dt><span>Creado por</span></dt>
          <dd>{{ user.createdBy }}</dd>
          <dt><span>Fecha de creación</span></dt>
          <dd>
            <span v-if="user.createdDate">
              {{ formatDate(user.createdDate) }}
            </span>
          </dd>
          <dt><span>Modificado por</span></dt>
          <dd>{{ user.lastModifiedBy }}</dd>
          <dt><span>Fecha de modificación</span></dt>
          <dd>
            <span v-if="user.lastModifiedDate">
              {{ formatDate(user.lastModifiedDate) }}
            </span>
          </dd>
          <dt><span>Perfiles</span></dt>
          <dd>
            <ul class="list-unstyled">
              <li v-for="authority of user.authorities" :key="authority">
                <b-badge variant="info">{{ authority }}</b-badge>
              </li>
            </ul>
          </dd>
        </dl>
        <router-link custom v-slot="{ navigate }" :to="{ name: 'JhiUser' }">
          <button @click="navigate" class="btn btn-info" data-cy="entityDetailsBackButton">
            <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span>Volver</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./user-management-view.component.ts"></script>
