<template>
  <section class="error-page">
    <div class="error-card">
      <p class="error-eyebrow">{{ titleEyebrow }}</p>
      <h1 class="error-title">{{ title }}</h1>
      <p class="error-copy">{{ description }}</p>

      <div v-if="errorMessage" class="alert alert-danger mt-3">{{ errorMessage }}</div>

      <div class="error-actions">
        <router-link class="btn btn-primary" to="/">Ir al inicio</router-link>
        <router-link class="btn btn-outline-secondary" to="/login">Iniciar sesion</router-link>
      </div>
    </div>
  </section>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const errorMessage = computed(() => (route.meta?.errorMessage as string | undefined) ?? '');
const is403 = computed(() => Boolean(route.meta?.error403));
const is404 = computed(() => Boolean(route.meta?.error404));

const titleEyebrow = computed(() => (is403.value ? 'Acceso restringido' : is404.value ? 'No encontrado' : 'Error'));
const title = computed(() => (is403.value ? 'No tienes permisos para ingresar aqui' : is404.value ? 'La pagina no existe' : 'Ocurrio un error'));
const description = computed(() =>
  is403.value
    ? 'Esta seccion no esta habilitada para tu perfil. Si necesitas acceso, contacta a un administrador.'
    : is404.value
      ? 'La ruta que intentaste abrir no esta disponible. Puedes volver al inicio para continuar.'
      : 'No se pudo completar la operacion. Intenta nuevamente en unos segundos.',
);
</script>

<style scoped>
.error-page {
  min-height: 56vh;
  display: grid;
  place-items: center;
  padding: 1rem;
}

.error-card {
  width: min(720px, 100%);
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.08);
  padding: 1.5rem;
}

.error-eyebrow {
  margin: 0 0 0.35rem;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #0284c7;
}

.error-title {
  margin: 0;
  font-size: 1.45rem;
  color: #0f172a;
}

.error-copy {
  margin: 0.65rem 0 0;
  color: #64748b;
}

.error-actions {
  margin-top: 1.2rem;
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}
</style>
