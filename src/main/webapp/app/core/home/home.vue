<template>
  <div class="home container mt-4">

    <!-- HEADER -->
    <div class="mb-4 text-center">
      <h2 class="fw-bold">Sistema de Gestión - Concesionaria</h2>
      <p class="text-muted">
        Plataforma para la administración integral de vehículos, clientes y operaciones comerciales.
      </p>
    </div>

    <!-- USUARIO -->
    <div v-if="authenticated" class="alert alert-success text-center">
      Bienvenido <strong>{{ username }}</strong>
    </div>

    <div v-if="!authenticated" class="alert alert-warning text-center">
      Debe iniciar sesión para utilizar el sistema.
      <a @click="showLogin()" class="alert-link ms-2">Iniciar sesión</a>
    </div>

    <!-- ACCESOS RÁPIDOS -->
    <div class="row g-3 mb-4">

      <div class="col-md-3">
        <div class="card h-100 shadow-sm text-center">
          <div class="card-body">
            <h5 class="card-title">Vehículos</h5>
            <p class="text-muted small">Gestión de unidades en stock</p>
            <router-link to="/vehiculo" class="btn btn-primary btn-sm">Acceder</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-3">
        <div class="card h-100 shadow-sm text-center">
          <div class="card-body">
            <h5 class="card-title">Marcas</h5>
            <p class="text-muted small">Administración de fabricantes</p>
            <router-link to="/marca" class="btn btn-primary btn-sm">Acceder</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-3">
        <div class="card h-100 shadow-sm text-center">
          <div class="card-body">
            <h5 class="card-title">Modelos</h5>
            <p class="text-muted small">Configuración de modelos</p>
            <router-link to="/modelo" class="btn btn-primary btn-sm">Acceder</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-3">
        <div class="card h-100 shadow-sm text-center">
          <div class="card-body">
            <h5 class="card-title">Motores</h5>
            <p class="text-muted small">Datos técnicos de vehículos</p>
            <router-link to="/motor" class="btn btn-primary btn-sm">Acceder</router-link>
          </div>
        </div>
      </div>

    </div>

    <!-- INFORMACIÓN DEL SISTEMA -->
    <div class="row g-3 mb-4">

      <div class="col-md-6">
        <div class="card shadow-sm h-100">
          <div class="card-body">
            <h5 class="card-title">Estado del sistema</h5>
            <ul class="mb-0">
              <li>✔ Backend operativo</li>
              <li>✔ Base de datos conectada</li>
              <li>✔ API funcionando</li>
            </ul>
          </div>
        </div>
      </div>

      <div class="col-md-6">
        <div class="card shadow-sm h-100">
          <div class="card-body">
            <h5 class="card-title">Acciones rápidas</h5>
            <ul class="mb-0">
              <li>
                <router-link to="/vehiculo/new">➕ Registrar vehículo</router-link>
              </li>
              <li>
                <router-link to="/marca/new">➕ Crear marca</router-link>
              </li>
              <li>
                <router-link to="/modelo/new">➕ Crear modelo</router-link>
              </li>
            </ul>
          </div>
        </div>
      </div>

    </div>

    <!-- MENSAJE FINAL -->
    <div class="alert alert-info text-center">
      Utilice el menú o los accesos rápidos para operar el sistema de gestión.
    </div>

  </div>
</template>

<script lang="ts" src="./home.component.ts"></script>
