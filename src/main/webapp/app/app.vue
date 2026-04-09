<template>
  <BToastOrchestrator />

  <!-- Layout autenticado -->
  <AppLayout v-if="authenticated">
    <router-view />
  </AppLayout>

  <!-- Layout sin autenticar: solo router-view centrado -->
  <div v-else class="d-flex align-items-center justify-content-center min-vh-100 bg-light">
    <router-view />
  </div>

  <!-- Modal de login global -->
  <b-modal id="login-page" focus="username" v-model="loginModalOpen" :no-footer="true" lazy>
    <template #title>
      <span id="login-title">Iniciar sesión</span>
    </template>
    <login-form v-if="loginModalOpen" />
  </b-modal>

  <!-- Ribbon de entorno (solo dev) -->
  <ribbon />
</template>

<script lang="ts" src="./app.component.ts"></script>
