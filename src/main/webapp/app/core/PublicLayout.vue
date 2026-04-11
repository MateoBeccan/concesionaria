<template>
  <div class="public-shell">
    <div class="public-brand">
      <div class="brand-icon">🚗</div>
      <div>
        <h1 class="brand-title">AutoGestión</h1>
        <p class="brand-subtitle mb-0">Plataforma de gestión para concesionarias</p>
      </div>
    </div>

    <main class="public-main">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.public-shell {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f8ff 0%, #edf2f9 100%);
  display: flex;
  flex-direction: column;
}

.public-brand {
  height: 72px;
  padding: 0 1.25rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border-bottom: 1px solid #e3e9f3;
  background: #fff;
}

.brand-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: grid;
  place-content: center;
  background: #0d6efd;
  color: #fff;
}

.brand-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #1f2937;
}

.brand-subtitle {
  font-size: 0.8rem;
  color: #6b7280;
}

.public-main {
  flex: 1;
  display: grid;
  place-content: center;
  padding: 1.5rem;
}
</style>
