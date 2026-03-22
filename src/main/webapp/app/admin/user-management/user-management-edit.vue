<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()" v-if="userAccount">
        <h2 id="myUserLabel" data-cy="UserManagementCreateUpdateHeading">Crear o editar un usuario</h2>
        <div>
          <div class="mb-3" :hidden="!userAccount.id">
            <label>ID</label>
            <input type="text" class="form-control" name="id" data-cy="id" v-model="userAccount.id" readonly />
          </div>

          <div class="mb-3">
            <label class="form-control-label">Login</label>
            <input
              type="text"
              class="form-control"
              name="login"
              data-cy="login"
              :class="{ 'is-valid': !v$.userAccount.login.$invalid, 'is-invalid': v$.userAccount.login.$invalid }"
              v-model="v$.userAccount.login.$model"
            />

            <div v-if="v$.userAccount.login.$anyDirty && v$.userAccount.login.$invalid">
              <small class="form-text text-danger" v-if="v$.userAccount.login.required.$invalid">Este campo es obligatorio.</small>

              <small class="form-text text-danger" v-if="v$.userAccount.login.maxLength.$invalid"
                >Este campo no puede superar más de 50 caracteres.</small
              >

              <small class="form-text text-danger" v-if="v$.userAccount.login.pattern.$invalid"
                >This field can only contain letters, digits and e-mail addresses.</small
              >
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="firstName">Nombre</label>
            <input
              type="text"
              class="form-control"
              id="firstName"
              name="firstName"
              data-cy="firstName"
              placeholder="Su nombre"
              :class="{ 'is-valid': !v$.userAccount.firstName.$invalid, 'is-invalid': v$.userAccount.firstName.$invalid }"
              v-model="v$.userAccount.firstName.$model"
            />
            <div v-if="v$.userAccount.firstName.$anyDirty && v$.userAccount.firstName.$invalid">
              <small class="form-text text-danger" v-if="v$.userAccount.firstName.maxLength.$invalid"
                >Este campo no puede superar más de 50 caracteres.</small
              >
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="lastName">Apellidos</label>
            <input
              type="text"
              class="form-control"
              id="lastName"
              name="lastName"
              data-cy="lastName"
              placeholder="Sus apellidos"
              :class="{ 'is-valid': !v$.userAccount.lastName.$invalid, 'is-invalid': v$.userAccount.lastName.$invalid }"
              v-model="v$.userAccount.lastName.$model"
            />
            <div v-if="v$.userAccount.lastName.$anyDirty && v$.userAccount.lastName.$invalid">
              <small class="form-text text-danger" v-if="v$.userAccount.lastName.maxLength.$invalid"
                >Este campo no puede superar más de 50 caracteres.</small
              >
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="email">Email</label>
            <input
              type="email"
              class="form-control"
              id="email"
              name="email"
              data-cy="email"
              placeholder="Su correo electrónico"
              :class="{ 'is-valid': !v$.userAccount.email.$invalid, 'is-invalid': v$.userAccount.email.$invalid }"
              v-model="v$.userAccount.email.$model"
              email
              required
            />
            <div v-if="v$.userAccount.email.$anyDirty && v$.userAccount.email.$invalid">
              <small class="form-text text-danger" v-if="v$.userAccount.email.required.$invalid">Se requiere un correo electrónico.</small>
              <small class="form-text text-danger" v-if="v$.userAccount.email.email.$invalid">Su correo electrónico no es válido.</small>
              <small class="form-text text-danger" v-if="v$.userAccount.email.minLength.$invalid"
                >Se requiere que su correo electrónico tenga por lo menos 5 caracteres</small
              >
              <small class="form-text text-danger" v-if="v$.userAccount.email.maxLength.$invalid"
                >Su correo electrónico no puede tener más de 50 caracteres</small
              >
            </div>
          </div>
          <div class="form-check">
            <label class="form-check-label" for="activated">
              <input
                class="form-check-input"
                :disabled="userAccount.id === null"
                type="checkbox"
                id="activated"
                name="activated"
                data-cy="activated"
                v-model="userAccount.activated"
              />
              <span>Activado</span>
            </label>
          </div>

          <div class="mb-3">
            <label>Perfiles</label>
            <select class="form-control" multiple name="authority" data-cy="profiles" v-model="userAccount.authorities">
              <option v-for="authority of authorities" :value="authority" :key="authority">{{ authority }}</option>
            </select>
          </div>
        </div>
        <div>
          <button type="button" class="btn btn-secondary" @click="previousState()" data-cy="entityCreateCancelButton">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span>Cancelar</span>
          </button>
          <button type="submit" :disabled="v$.userAccount.$invalid || isSaving" class="btn btn-primary" data-cy="entityCreateSaveButton">
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span>Guardar</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts" src="./user-management-edit.component.ts"></script>
