<template>
  <!-- Este componente está obsoleto. Redirige automáticamente al wizard. -->
</template>

<script lang="ts" setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
onMounted(() => router.replace({ name: 'VentaWizard' }));
</script>
