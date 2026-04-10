<template>
  <div class="app-shell">

    <!-- SIDEBAR (solo si está autenticado) -->
    <Sidebar v-if="authenticated" />

    <!-- MAIN -->
    <div
      class="app-main"
      :class="{ 'full-width': !authenticated }"
    >

      <!-- TOPBAR -->
      <Topbar v-if="authenticated" />

      <!-- CONTENIDO -->
      <main class="app-content">

        <!--  SOLO renderiza si está autenticado -->
        <router-view v-if="authenticated" />

      </main>
    </div>

  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useStore } from '@/store';
import Sidebar from '@/core/Sidebar.vue';
import Topbar from '@/core/Topbar.vue';

const store = useStore();
const authenticated = computed(() => store.authenticated);
</script>

<style scoped>

/* Layout base */
.app-shell {
  display: flex;
  min-height: 100vh;
}

/* Contenido principal */
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* Cuando NO está autenticado */
.app-main.full-width {
  margin-left: 0 !important;
}

/* Contenido interno */
.app-content {
  flex: 1;
  padding: 1.5rem;
  background-color: #f8f9fa;
  overflow-y: auto;
}

</style>
