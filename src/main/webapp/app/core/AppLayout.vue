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
  display: flex;
  min-height: 100vh;
  background: #f3f6fb;
}

.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.app-content {
  flex: 1;
  padding: 1.25rem 1.5rem;
  overflow-y: auto;
}
</style>
