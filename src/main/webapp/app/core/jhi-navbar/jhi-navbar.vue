<template>
  <b-navbar data-cy="navbar" toggleable="md" variant="dark" data-bs-theme="dark">
    <b-navbar-brand class="logo" b-link to="/">
      <span class="logo-img"></span>
      <span class="navbar-title">Concesionaria</span> <span class="navbar-version">{{ version }}</span>
    </b-navbar-brand>

    <b-navbar-toggle
      right
      class="jh-navbar-toggler d-lg-none"
      href="javascript:void(0);"
      data-toggle="collapse"
      target="header-tabs"
      aria-expanded="false"
      aria-label="Toggle navigation"
    >
      <font-awesome-icon icon="bars" />
    </b-navbar-toggle>

    <b-collapse is-nav id="header-tabs">
      <b-navbar-nav class="ms-auto">
        <b-nav-item to="/" exact>
          <span>
            <font-awesome-icon icon="fa-solid fa-home" />
            <span>Inicio</span>
          </span>
        </b-nav-item>

        <b-nav-item-dropdown
          :no-size="true"
          end
          id="entity-menu"
          v-if="hasAnyAuthority('ROLE_ADMIN') && authenticated"
          active-class="active"
          class="pointer"
          data-cy="entity"
        >
          <template #button-content>
            <span class="navbar-dropdown-menu">
              <font-awesome-icon icon="th-list" />
              <span class="no-bold">Catalogo</span>
            </span>
          </template>
          <entities-menu></entities-menu>
        </b-nav-item-dropdown>

        <b-nav-item-dropdown
          right
          id="admin-menu"
          v-if="hasAnyAuthority('ROLE_ADMIN') && authenticated"
          :class="{ 'router-link-active': subIsActive('/admin') }"
          active-class="active"
          class="pointer"
          data-cy="adminMenu"
        >
          <template #button-content>
            <span class="navbar-dropdown-menu">
              <font-awesome-icon icon="users-cog" />
              <span class="no-bold">Administracion</span>
            </span>
          </template>
          <b-dropdown-item to="/admin/user-management" active-class="active">
            <font-awesome-icon icon="users" />
            <span>Usuarios</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/metrics" active-class="active">
            <font-awesome-icon icon="tachometer-alt" />
            <span>Metricas</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/health" active-class="active">
            <font-awesome-icon icon="heart" />
            <span>Salud</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/configuration" active-class="active">
            <font-awesome-icon icon="cogs" />
            <span>Configuracion</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/version-compatibilities" active-class="active">
            <font-awesome-icon icon="cogs" />
            <span>Compatibilidades</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/logs" active-class="active">
            <font-awesome-icon icon="tasks" />
            <span>Logs</span>
          </b-dropdown-item>
          <b-dropdown-item v-if="openAPIEnabled" to="/admin/docs" active-class="active">
            <font-awesome-icon icon="book" />
            <span>API</span>
          </b-dropdown-item>
        </b-nav-item-dropdown>

        <b-nav-item-dropdown
          right
          href="javascript:void(0);"
          id="account-menu"
          :class="{ 'router-link-active': subIsActive('/account') }"
          active-class="active"
          class="pointer"
          data-cy="accountMenu"
        >
          <template #button-content>
            <span class="navbar-dropdown-menu">
              <font-awesome-icon icon="user" />
              <span class="no-bold">Cuenta</span>
            </span>
          </template>
          <b-dropdown-item data-cy="settings" to="/account/settings" v-if="authenticated" active-class="active">
            <font-awesome-icon icon="wrench" />
            <span>Ajustes</span>
          </b-dropdown-item>
          <b-dropdown-item data-cy="passwordItem" to="/account/password" v-if="authenticated" active-class="active">
            <font-awesome-icon icon="lock" />
            <span>Contrasena</span>
          </b-dropdown-item>
          <b-dropdown-item data-cy="logout" v-if="authenticated" @click="logout()" id="logout" active-class="active">
            <font-awesome-icon icon="sign-out-alt" />
            <span>Cerrar sesion</span>
          </b-dropdown-item>
          <b-dropdown-item data-cy="login" v-if="!authenticated" @click="showLogin()" id="login" active-class="active">
            <font-awesome-icon icon="sign-in-alt" />
            <span>Iniciar sesion</span>
          </b-dropdown-item>
          <b-dropdown-item data-cy="register" to="/register" id="register" v-if="!authenticated" active-class="active">
            <font-awesome-icon icon="user-plus" />
            <span>Crear una cuenta</span>
          </b-dropdown-item>
        </b-nav-item-dropdown>
      </b-navbar-nav>
    </b-collapse>
  </b-navbar>
</template>

<script lang="ts" src="./jhi-navbar.component.ts"></script>

<style scoped>
.navbar-version {
  font-size: 0.65em;
  color: #ccc;
}

.navbar .navbar-nav .nav-item {
  margin-right: 0.5rem;
}

@media screen and (min-width: 768px) {
  .jh-navbar-toggler {
    display: none;
  }
}

@media screen and (min-width: 768px) and (max-width: 1150px) {
  span span {
    display: none;
  }
}

.navbar-title {
  display: inline-block;
  color: white;
}

.navbar-brand.logo {
  padding: 0 7px;
}

.logo .logo-img {
  height: 45px;
  display: inline-block;
  vertical-align: middle;
  width: 45px;
}

.logo-img {
  height: 100%;
  background: url('/content/images/logo-jhipster.png') no-repeat center center;
  background-size: contain;
  width: 100%;
  filter: drop-shadow(0 0 0.05rem white);
  margin: 0 5px;
}
</style>
