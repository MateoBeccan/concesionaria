<template>
  <div>
    <div class="d-flex justify-content-center">
      <div class="col-md-8 toastify-container">
        <h1 id="register-title" data-cy="registerTitle">Registro</h1>

        <div class="alert alert-success" role="alert" v-if="success">
          <strong>¡Registro guardado!</strong> Por favor, revise su correo electrónico para confirmar.
        </div>

        <div class="alert alert-danger" role="alert" v-if="error">
          <strong>¡El registro ha fallado!</strong> Por favor, inténtelo de nuevo más tarde.
        </div>

        <div class="alert alert-danger" role="alert" v-if="errorUserExists">
          <strong>¡El nombre de usuario ya está registrado!</strong> Por favor, escoja otro usuario.
        </div>

        <div class="alert alert-danger" role="alert" v-if="errorEmailExists">
          <strong>¡El correo electrónico ya está en uso!</strong> Por favor, escoja otro email.
        </div>
      </div>
    </div>
    <div class="d-flex justify-content-center">
      <div class="col-md-8">
        <form id="register-form" name="registerForm" @submit.prevent="register()" v-if="!success" no-validate>
          <div class="mb-3">
            <label class="form-control-label" for="username">Usuario</label>
            <input
              type="text"
              class="form-control"
              v-model="v$.registerAccount.login.$model"
              id="username"
              name="login"
              :class="{ 'is-valid': !v$.registerAccount.login.$invalid, 'is-invalid': v$.registerAccount.login.$invalid }"
              required
              minlength="1"
              maxlength="50"
              pattern="^[a-zA-Z0-9!#$&'*+=?^_`{|}~.-]+@?[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$"
              placeholder="Nombre de usuario"
              data-cy="username"
            />
            <div v-if="v$.registerAccount.login.$anyDirty && v$.registerAccount.login.$invalid">
              <small class="form-text text-danger" v-if="v$.registerAccount.login.required.$invalid"
                >Su nombre de usuario es obligatorio.</small
              >
              <small class="form-text text-danger" v-if="v$.registerAccount.login.minLength.$invalid"
                >Su nombre de usuario debe tener al menos 1 caracter.</small
              >
              <small class="form-text text-danger" v-if="v$.registerAccount.login.maxLength.$invalid"
                >Su nombre de usuario no puede tener más de 50 caracteres.</small
              >
              <small class="form-text text-danger" v-if="v$.registerAccount.login.pattern.$invalid"
                >Su nombre de usuario no es válido.</small
              >
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="email">Correo electrónico</label>
            <input
              type="email"
              class="form-control"
              id="email"
              name="email"
              :class="{ 'is-valid': !v$.registerAccount.email.$invalid, 'is-invalid': v$.registerAccount.email.$invalid }"
              v-model="v$.registerAccount.email.$model"
              minlength="5"
              maxlength="254"
              email
              required
              placeholder="Su correo electrónico"
              data-cy="email"
            />
            <div v-if="v$.registerAccount.email.$anyDirty && v$.registerAccount.email.$invalid">
              <small class="form-text text-danger" v-if="v$.registerAccount.email.required.$invalid"
                >Se requiere un correo electrónico.</small
              >
              <small class="form-text text-danger" v-if="v$.registerAccount.email.email.$invalid"
                >Su correo electrónico no es válido.</small
              >
              <small class="form-text text-danger" v-if="v$.registerAccount.email.minLength.$invalid"
                >Se requiere que su correo electrónico tenga por lo menos 5 caracteres</small
              >
              <small class="form-text text-danger" v-if="v$.registerAccount.email.maxLength.$invalid"
                >Su correo electrónico no puede tener más de 50 caracteres</small
              >
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="firstPassword">Nueva contraseña</label>
            <input
              type="password"
              class="form-control"
              id="firstPassword"
              name="password"
              :class="{ 'is-valid': !v$.registerAccount.password.$invalid, 'is-invalid': v$.registerAccount.password.$invalid }"
              v-model="v$.registerAccount.password.$model"
              minlength="4"
              maxlength="50"
              required
              placeholder="Nueva contraseña"
              data-cy="firstPassword"
            />
            <div v-if="v$.registerAccount.password.$anyDirty && v$.registerAccount.password.$invalid">
              <small class="form-text text-danger" v-if="v$.registerAccount.password.required.$invalid"
                >Se requiere que ingrese una contraseña.</small
              >
              <small class="form-text text-danger" v-if="v$.registerAccount.password.minLength.$invalid"
                >Se requiere que su contraseña tenga por lo menos 4 caracteres</small
              >
              <small class="form-text text-danger" v-if="v$.registerAccount.password.maxLength.$invalid"
                >Su contraseña no puede tener más de 50 caracteres</small
              >
            </div>
          </div>
          <div class="mb-3">
            <label class="form-control-label" for="secondPassword">Confirmación de la nueva contraseña</label>
            <input
              type="password"
              class="form-control"
              id="secondPassword"
              name="confirmPasswordInput"
              :class="{ 'is-valid': !v$.confirmPassword.$invalid, 'is-invalid': v$.confirmPassword.$invalid }"
              v-model="v$.confirmPassword.$model"
              minlength="4"
              maxlength="50"
              required
              placeholder="Confirmación de la nueva contraseña"
              data-cy="secondPassword"
            />
            <div v-if="v$.confirmPassword.$dirty && v$.confirmPassword.$invalid">
              <small class="form-text text-danger" v-if="v$.confirmPassword.required.$invalid"
                >Se requiere que confirme la contraseña.</small
              >
              <small class="form-text text-danger" v-if="v$.confirmPassword.minLength.$invalid"
                >Se requiere que su contraseña de confirmación tenga por lo menos 4 caracteres</small
              >
              <small class="form-text text-danger" v-if="v$.confirmPassword.maxLength.$invalid"
                >Su contraseña de confirmación no puede tener más de 50 caracteres</small
              >
              <small class="form-text text-danger" v-if="v$.confirmPassword.sameAsPassword"
                >¡La contraseña y la confirmación de contraseña no coinciden!</small
              >
            </div>
          </div>

          <button type="submit" :disabled="v$.$invalid" class="btn btn-primary" data-cy="submit">Crear la cuenta</button>
        </form>
        <p></p>
        <div class="alert alert-warning">
          <span>Si desea </span>
          <a class="alert-link" @click="showLogin()">iniciar sesión</a
          ><span
            >, puede intentar con las cuentas predeterminadas:<br />- Administrador (usuario="admin" y contraseña="admin") <br />- Usuario
            (usuario="user" y contraseña="user").</span
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./register.component.ts"></script>
