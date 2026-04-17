<template>
  <div class="config-page">
    <section class="hero-card">
      <div>
        <p class="hero-eyebrow">Panel admin</p>
        <h1 class="hero-title">Centro de configuracion</h1>
        <p class="hero-copy">
          Administra parametros del sistema, revisa configuraciones activas y accede rapido a catalogos de soporte del negocio.
        </p>
      </div>
      <div class="hero-actions">
        <router-link :to="{ name: 'CatalogoTecnicoAdmin' }" class="btn btn-light btn-sm">Catalogo tecnico</router-link>
        <router-link :to="{ name: 'MetodoPago' }" class="btn btn-outline-light btn-sm">Metodos de pago</router-link>
      </div>
    </section>

    <section class="kpi-grid" v-if="configuration && allConfiguration">
      <article class="kpi-card">
        <span class="kpi-label">Prefijos spring</span>
        <strong class="kpi-value">{{ summary.springPrefixes }}</strong>
      </article>
      <article class="kpi-card">
        <span class="kpi-label">Propiedades spring</span>
        <strong class="kpi-value">{{ summary.springProperties }}</strong>
      </article>
      <article class="kpi-card">
        <span class="kpi-label">Grupos de entorno</span>
        <strong class="kpi-value">{{ summary.envGroups }}</strong>
      </article>
      <article class="kpi-card">
        <span class="kpi-label">Variables listadas</span>
        <strong class="kpi-value">{{ summary.envProperties }}</strong>
      </article>
    </section>

    <section class="quick-grid">
      <router-link class="quick-card" :to="{ name: 'Cotizacion' }">
        <span class="quick-code">CZ</span>
        <div>
          <strong>Cotizaciones</strong>
          <p class="mb-0">Gestion de valores y actualizaciones de moneda.</p>
        </div>
      </router-link>
      <router-link class="quick-card" :to="{ name: 'Moneda' }">
        <span class="quick-code">MN</span>
        <div>
          <strong>Monedas</strong>
          <p class="mb-0">Definicion de monedas para operaciones y pagos.</p>
        </div>
      </router-link>
      <router-link class="quick-card" :to="{ name: 'TipoDocumento' }">
        <span class="quick-code">TD</span>
        <div>
          <strong>Tipos de documento</strong>
          <p class="mb-0">Normalizacion de identidad y validaciones de cliente.</p>
        </div>
      </router-link>
      <router-link class="quick-card" :to="{ name: 'CondicionIva' }">
        <span class="quick-code">CI</span>
        <div>
          <strong>Condiciones IVA</strong>
          <p class="mb-0">Parametrizacion fiscal para clientes y comprobantes.</p>
        </div>
      </router-link>
    </section>

    <section class="surface-card" v-if="allConfiguration && configuration">
      <div class="surface-head">
        <div>
          <p class="surface-eyebrow">Explorador tecnico</p>
          <h2 class="surface-title">Configuracion del sistema</h2>
        </div>
        <input type="text" v-model="filtered" class="form-control filter-input" placeholder="Filtrar por prefijo o propiedad" />
      </div>

      <div class="prefix-grid">
        <details v-for="entry in filteredConfiguration" :key="entry.prefix" class="prefix-card">
          <summary>
            <strong>{{ entry.prefix }}</strong>
            <span>{{ keys(entry.properties).length }} propiedades</span>
          </summary>
          <div class="prop-list">
            <div v-for="key in keys(entry.properties)" :key="key" class="prop-item">
              <span class="prop-key">{{ key }}</span>
              <code class="prop-value">{{ entry.properties[key] }}</code>
            </div>
          </div>
        </details>
      </div>
    </section>

    <section class="surface-card" v-if="allConfiguration && configuration">
      <p class="surface-eyebrow">Entorno</p>
      <h2 class="surface-title">Variables por grupo</h2>
      <div class="env-grid">
        <details v-for="key in keys(allConfiguration)" :key="key" class="prefix-card">
          <summary>
            <strong>{{ key }}</strong>
            <span>{{ allConfiguration[key]?.length ?? 0 }} items</span>
          </summary>
          <div class="prop-list">
            <div v-for="item of allConfiguration[key]" :key="item.key" class="prop-item">
              <span class="prop-key">{{ item.key }}</span>
              <code class="prop-value">{{ item.val }}</code>
            </div>
          </div>
        </details>
      </div>
    </section>
  </div>
</template>

<script lang="ts" src="./configuration.component.ts"></script>

<style scoped>
.config-page {
  max-width: 1140px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.2rem 1.3rem;
  border-radius: 18px;
  color: #eef2ff;
  border: 1px solid #1d4ed8;
  background: linear-gradient(135deg, #0f172a, #1d4ed8);
}

.hero-eyebrow,
.surface-eyebrow {
  margin: 0;
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.hero-title,
.surface-title {
  margin: 0.3rem 0 0;
}

.hero-copy {
  margin: 0.45rem 0 0;
  color: #dbeafe;
}

.hero-actions {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.75rem;
}

.kpi-card,
.quick-card,
.surface-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
}

.kpi-card {
  padding: 0.85rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.kpi-label {
  font-size: 0.78rem;
  color: #64748b;
}

.kpi-value {
  font-size: 1.35rem;
  color: #0f172a;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 0.75rem;
}

.quick-card {
  padding: 0.9rem 1rem;
  display: flex;
  align-items: flex-start;
  gap: 0.7rem;
  text-decoration: none;
  color: inherit;
}

.quick-code {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 0.74rem;
  color: #1d4ed8;
  background: #dbeafe;
  flex-shrink: 0;
}

.quick-card p {
  margin-top: 0.2rem;
  color: #64748b;
  font-size: 0.85rem;
}

.surface-card {
  padding: 1rem;
}

.surface-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 1rem;
  margin-bottom: 0.8rem;
}

.filter-input {
  max-width: 320px;
}

.prefix-grid,
.env-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 0.75rem;
}

.prefix-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0.7rem 0.8rem;
  background: #f8fafc;
}

.prefix-card summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  list-style: none;
}

.prefix-card summary::-webkit-details-marker {
  display: none;
}

.prefix-card summary span {
  color: #64748b;
  font-size: 0.8rem;
}

.prop-list {
  margin-top: 0.7rem;
  display: grid;
  gap: 0.4rem;
}

.prop-item {
  display: grid;
  gap: 0.2rem;
}

.prop-key {
  font-size: 0.8rem;
  color: #334155;
}

.prop-value {
  display: block;
  padding: 0.35rem 0.45rem;
  border-radius: 8px;
  background: #eef2ff;
  color: #1e293b;
  font-size: 0.78rem;
  white-space: pre-wrap;
  word-break: break-all;
}

@media (max-width: 768px) {
  .hero-card,
  .surface-head {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-input {
    max-width: none;
  }
}
</style>
