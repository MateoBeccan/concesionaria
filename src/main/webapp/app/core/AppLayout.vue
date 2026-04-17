<template>
  <div class="app-shell">
    <Sidebar />
    <div class="app-main">
      <Topbar />
      <main class="app-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script lang="ts" setup>
import Sidebar from '@/core/Sidebar.vue';
import Topbar from '@/core/Topbar.vue';
</script>

<style scoped>
.app-shell {
  --sidebar-width: 248px;
  display: flex;
  min-height: 100vh;
  background: linear-gradient(180deg, #f7f9fc 0%, #f1f5f9 100%);
}

.app-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.app-content {
  flex: 1;
  min-width: 0;
  padding: 1rem 1.1rem 1.25rem;
  overflow-y: auto;
  overflow-x: hidden;
}

@media (max-width: 1100px) {
  .app-shell {
    --sidebar-width: 224px;
  }
}

@media (max-width: 920px) {
  .app-shell {
    --sidebar-width: 208px;
  }

  .app-content {
    padding: 1rem;
  }
}
</style>
