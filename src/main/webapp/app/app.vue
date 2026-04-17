<template>
  <BOrchestrator />

  <!-- Layout autenticado -->
  <AppLayout v-if="useAuthenticatedLayout">
    <router-view />
  </AppLayout>

  <!-- Layout público -->
  <PublicLayout v-else>
    <router-view />
  </PublicLayout>

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
