<template>
  <div class="public-layout">
    <header class="public-header">
      <div class="public-header-inner">
        <router-link class="public-brand-link" to="/">
          <img :src="brandLogoUrl" alt="AutoGestion" class="public-brand-logo" />
        </router-link>

        <div class="public-badge">
          <span>Acceso seguro</span>
        </div>
      </div>
    </header>

    <main class="public-main">
      <div class="public-content">
        <slot />
      </div>
    </main>

    <footer class="public-footer">
      <p>© {{ currentYear }} AutoGestion. Gestion profesional de concesionaria.</p>
    </footer>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

const brandLogoUrl = '/content/images/branding/logo.png';
const currentYear = computed(() => new Date().getFullYear());
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.08), transparent 32%),
    linear-gradient(160deg, #f8fafc 0%, #eef2ff 60%, #e0f2fe 100%);
}

.public-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(4px);
  border-bottom: 1px solid #e2e8f0;
}

.public-header-inner {
  max-width: 1160px;
  margin: 0 auto;
  min-height: 78px;
  padding: 0.8rem 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.public-brand-link {
  display: block;
  line-height: 0;
}

.public-brand-logo {
  width: clamp(250px, 29vw, 360px);
  height: clamp(54px, 6.8vw, 74px);
  object-fit: contain;
  object-position: left center;
  display: block;
}

.public-badge {
  padding: 0.38rem 0.75rem;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.public-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.6rem 1rem;
}

.public-content {
  width: 100%;
  max-width: 1160px;
}

.public-footer {
  border-top: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.9);
  text-align: center;
  padding: 0.8rem 1rem;
  color: #64748b;
  font-size: 0.82rem;
}

.public-footer p {
  margin: 0;
}

@media (max-width: 767px) {
  .public-header-inner {
    min-height: 66px;
    padding: 0.6rem 0.8rem;
  }

  .public-brand-logo {
    width: clamp(210px, 60vw, 285px);
    height: clamp(46px, 12vw, 64px);
  }

  .public-badge {
    font-size: 0.72rem;
    padding: 0.3rem 0.6rem;
  }

  .public-main {
    padding: 1rem 0.8rem;
  }
}
</style>
