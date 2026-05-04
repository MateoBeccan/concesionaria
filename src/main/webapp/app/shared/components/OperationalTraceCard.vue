<template>
  <div class="trace-card">
    <div class="trace-header">
      <div>
        <p class="trace-eyebrow">Trazabilidad</p>
        <h3 class="trace-title">{{ title }}</h3>
      </div>
      <span class="trace-status">{{ status || 'Sin estado' }}</span>
    </div>

    <div class="trace-grid">
      <div class="trace-item">
        <span class="trace-label">Creado por</span>
        <strong class="trace-value">{{ createdBy || 'No disponible' }}</strong>
      </div>
      <div class="trace-item">
        <span class="trace-label">Creado el</span>
        <strong class="trace-value">{{ createdAt || 'No disponible' }}</strong>
      </div>
      <div class="trace-item">
        <span class="trace-label">Actualizado por</span>
        <strong class="trace-value">{{ updatedBy || 'No disponible' }}</strong>
      </div>
      <div class="trace-item">
        <span class="trace-label">Ultima actualizacion</span>
        <strong class="trace-value">{{ updatedAt || 'No disponible' }}</strong>
      </div>
    </div>

    <div class="trace-highlight">
      <span class="trace-label">Ultima accion relevante</span>
      <strong class="trace-highlight-value">{{ lastAction || 'Sin actividad relevante' }}</strong>
      <small v-if="lastActionAt" class="trace-highlight-copy">{{ lastActionAt }}</small>
    </div>
  </div>
</template>

<script lang="ts" setup>
withDefaults(
  defineProps<{
    title?: string;
    status?: string;
    createdBy?: string;
    createdAt?: string;
    updatedBy?: string;
    updatedAt?: string;
    lastAction?: string;
    lastActionAt?: string;
  }>(),
  {
    title: 'Registro operativo',
    status: 'Sin estado',
  },
);
</script>

<style scoped>
.trace-card {
  padding: 1.1rem 1.2rem;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.trace-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.trace-eyebrow {
  margin: 0 0 0.2rem;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #0284c7;
}

.trace-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #0f172a;
}

.trace-status {
  padding: 0.32rem 0.7rem;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 0.74rem;
  font-weight: 700;
}

.trace-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.trace-item,
.trace-highlight {
  padding: 0.85rem 0.95rem;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.trace-label {
  display: block;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #64748b;
}

.trace-value,
.trace-highlight-value {
  display: block;
  margin-top: 0.25rem;
  color: #0f172a;
}

.trace-highlight {
  margin-top: 0.85rem;
  background: #f0f9ff;
  border-color: #bae6fd;
}

.trace-highlight-copy {
  display: block;
  margin-top: 0.2rem;
  color: #64748b;
}

@media (max-width: 767px) {
  .trace-header {
    flex-direction: column;
  }

  .trace-grid {
    grid-template-columns: 1fr;
  }
}
</style>
