<template>
  <div>
    <h2>
      <span id="user-management-page-heading" data-cy="UserManagementHeading">Usuarios</span>

      <div class="d-flex justify-content-end">
        <button class="btn btn-info me-2" @click="handleSyncList" :disabled="isLoading">
          <font-awesome-icon icon="sync" :spin="isLoading"></font-awesome-icon> <span>Refrescar lista</span>
        </button>
        <router-link custom v-slot="{ navigate }" :to="{ name: 'JhiUserCreate' }">
          <button @click="navigate" class="btn btn-primary jh-create-entity" data-cy="entityCreateButton">
            <font-awesome-icon icon="plus"></font-awesome-icon> <span>Crear un nuevo usuario</span>
          </button>
        </router-link>
      </div>
    </h2>
    <div class="table-responsive" v-if="users">
      <table class="table table-striped" aria-describedby="Users">
        <thead>
          <tr>
            <th scope="col" @click="changeOrder('id')">
              <span>ID</span> <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('login')">
              <span>Login</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'login'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('email')">
              <span>Email</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'email'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
            <th scope="col"><span>Perfiles</span></th>
            <th scope="col" @click="changeOrder('createdDate')">
              <span>Fecha de creación</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
            </th>
            <th scope="col" @click="changeOrder('lastModifiedBy')">
              <span>Modificado por</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastModifiedBy'"></jhi-sort-indicator>
            </th>
            <th scope="col" id="modified-date-sort" @click="changeOrder('lastModifiedDate')">
              <span>Fecha de modificación</span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastModifiedDate'"></jhi-sort-indicator>
            </th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody v-if="users">
          <tr v-for="user in users" :key="user.id" :id="user.login" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'JhiUserView', params: { userId: user.login } }">{{ user.id }}</router-link>
            </td>
            <td>{{ user.login }}</td>
            <td class="jhi-user-email">{{ user.email }}</td>
            <td>
              <button class="btn btn-danger btn-sm deactivated" @click="setActive(user, true)" v-if="!user.activated">Desactivado</button>
              <button
                class="btn btn-success btn-sm"
                @click="setActive(user, false)"
                v-if="user.activated"
                :disabled="username === user.login"
              >
                Activado
              </button>
            </td>

            <td>
              <div v-for="authority of user.authorities" :key="authority">
                <span class="badge bg-info">{{ authority }}</span>
              </div>
            </td>
            <td>{{ formatDate(user.createdDate) }}</td>
            <td>{{ user.lastModifiedBy }}</td>
            <td>{{ formatDate(user.lastModifiedDate) }}</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'JhiUserView', params: { userId: user.login } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline">Vista</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'JhiUserEdit', params: { userId: user.login } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline">Editar</span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(user)"
                  variant="danger"
                  class="btn btn-sm delete"
                  :disabled="username === user.login"
                  data-cy="entityDeleteButton"
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                  <span class="d-none d-md-inline">Eliminar</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <b-modal
        ref="removeUser"
        id="removeUser"
        title="Confirmar operación de borrado"
        @ok="deleteUser()"
        data-cy="userManagementDeleteDialogHeading"
      >
        <div class="modal-body">
          <p id="jhi-delete-user-heading">¿Seguro que quieres eliminar el usuario {{ removeId }}?</p>
        </div>
        <template #footer>
          <div>
            <button type="button" class="btn btn-secondary" @click="closeDialog()">Cancelar</button>
            <button
              type="button"
              class="btn btn-primary"
              id="confirm-delete-user"
              @click="deleteUser()"
              data-cy="entityConfirmDeleteButton"
            >
              Eliminar
            </button>
          </div>
        </template>
      </b-modal>
    </div>
    <div v-show="users?.length > 0">
      <div class="d-flex justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="d-flex justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./user-management.component.ts"></script>
