<template>
  <div class="home container mt-4">

    <!-- HEADER -->
    <div class="mb-4">
      <h2>Panel de Gestión - Concesionaria</h2>
      <p class="text-muted">
        Sistema interno para administración de vehículos, marcas y operaciones.
      </p>
    </div>

    <!-- USUARIO -->
    <div v-if="authenticated" class="alert alert-success">
      Sesión iniciada como <strong>{{ username }}</strong>
    </div>

    <div v-if="!authenticated" class="alert alert-warning">
      Debe iniciar sesión para utilizar el sistema.
      <a @click="showLogin()" class="alert-link">Iniciar sesión</a>
    </div>

    <!-- ACCESOS RÁPIDOS -->
    <div class="row mb-4">
      <div class="col-md-3">
        <div class="p-3 border rounded bg-light text-center">
          <h5>Autos</h5>
          <p class="text-muted">Gestión de vehículos</p>
          <router-link to="/auto" class="btn btn-primary btn-sm">Acceder</router-link>
        </div>
      </div>

      <div class="col-md-3">
        <div class="p-3 border rounded bg-light text-center">
          <h5>Marcas</h5>
          <p class="text-muted">Administrar marcas</p>
          <router-link to="/marca" class="btn btn-primary btn-sm">Acceder</router-link>
        </div>
      </div>

      <div class="col-md-3">
        <div class="p-3 border rounded bg-light text-center">
          <h5>Modelos</h5>
          <p class="text-muted">Administrar modelos</p>
          <router-link to="/modelo" class="btn btn-primary btn-sm">Acceder</router-link>
        </div>
      </div>

      <div class="col-md-3">
        <div class="p-3 border rounded bg-light text-center">
          <h5>Motores</h5>
          <p class="text-muted">Configuración técnica</p>
          <router-link to="/motor" class="btn btn-primary btn-sm">Acceder</router-link>
        </div>
      </div>
    </div>

    <!-- INFORMACIÓN DEL SISTEMA -->
    <div class="row mb-4">
      <div class="col-md-6">
        <div class="p-3 border rounded">
          <h5>Estado del sistema</h5>
          <ul class="mb-0">
            <li>✔ Backend operativo</li>
            <li>✔ Base de datos conectada</li>
            <li>✔ API activa</li>
          </ul>
        </div>
      </div>

      <div class="col-md-6">
        <div class="p-3 border rounded">
          <h5>Acciones rápidas</h5>
          <ul class="mb-0">
            <li>
              <router-link to="/auto/new">➕ Crear nuevo auto</router-link>
            </li>
            <li>
              <router-link to="/marca/new">➕ Crear nueva marca</router-link>
            </li>
            <li>
              <router-link to="/modelo/new">➕ Crear nuevo modelo</router-link>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- MENSAJE FINAL -->
    <div class="alert alert-info">
      Utilice el menú lateral o los accesos rápidos para gestionar la información del sistema.
    </div>

  </div>
</template>

<script lang="ts" src="./home.component.ts"></script>
