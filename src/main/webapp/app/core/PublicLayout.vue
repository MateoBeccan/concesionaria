<template>
  <div class="public-shell">
    <header class="public-brand">
      <div class="brand-mark">AG</div>
      <div class="brand-copy">
        <h1 class="brand-title">AutoGestion</h1>
        <p class="brand-subtitle">Sistema de gestion para concesionarias</p>
      </div>
    </header>

    <main class="public-main">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.public-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at top left, rgba(37, 99, 235, 0.06), transparent 22%),
    linear-gradient(180deg, #f7f9fc 0%, #f1f5f9 100%);
}

.public-brand {
  min-height: 68px;
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.8rem 1.15rem;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 11px;
  display: grid;
  place-content: center;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  font-size: 0.74rem;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.brand-copy {
  min-width: 0;
}

.brand-title {
  margin: 0;
  font-size: 0.94rem;
  font-weight: 700;
  color: #0f172a;
}

.brand-subtitle {
  margin: 0.1rem 0 0;
  font-size: 0.76rem;
  color: #64748b;
}

.public-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem 1rem;
}

@media (max-width: 767.98px) {
  .public-brand {
    padding: 0.85rem 1rem;
  }

  .public-main {
    padding: 1rem;
  }
}
</style>
