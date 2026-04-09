<template>
  <div class="app-shell">
    <Sidebar v-if="authenticated" />
    <div class="app-main" :style="authenticated ? '' : 'margin-left:0'">
      <Topbar v-if="authenticated" />
      <main class="app-content">
        <slot />
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
