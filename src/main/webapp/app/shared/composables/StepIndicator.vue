<template>
  <div class="d-flex flex-column align-items-center gap-1">
    <div
      class="rounded-circle d-flex align-items-center justify-content-center fw-bold"
      style="width:38px;height:38px;font-size:.9rem;transition:all .2s"
      :class="claseCirculo"
    >
      <span v-if="paso < actual">✓</span>
      <span v-else>{{ paso }}</span>
    </div>
    <small :class="paso === actual ? 'text-primary fw-semibold' : 'text-muted'">{{ label }}</small>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

const props = defineProps<{ paso: number; actual: number; label: string }>();

const claseCirculo = computed(() => {
  if (props.paso < props.actual) return 'bg-success text-white';
  if (props.paso === props.actual) return 'bg-primary text-white';
  return 'bg-light text-muted border';
});
</script>
