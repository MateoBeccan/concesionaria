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
  --sidebar-width: 272px;
  display: flex;
  min-height: 100vh;
  background: #f3f6fb;
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
  padding: 1.25rem 1.35rem;
  overflow-y: auto;
  overflow-x: hidden;
}

@media (max-width: 1100px) {
  .app-shell {
    --sidebar-width: 248px;
  }
}

@media (max-width: 920px) {
  .app-shell {
    --sidebar-width: 232px;
  }

  .app-content {
    padding: 1rem;
  }
}
</style>
