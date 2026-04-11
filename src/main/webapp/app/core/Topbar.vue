<template>
  <header class="app-topbar">
    <div class="topbar-title-wrap">
      <h2 class="topbar-title">{{ pageMeta.title }}</h2>
      <p class="topbar-subtitle mb-0">{{ pageMeta.subtitle }}</p>
    </div>

    <div class="topbar-actions">
      <router-link :to="{ name: 'VentaWizard' }" class="btn btn-primary btn-sm d-none d-md-inline-flex">+ Nueva venta</router-link>

      <router-link v-if="isAdmin" :to="{ name: 'JhiMetricsComponent' }" class="btn btn-outline-secondary btn-sm d-none d-lg-inline-flex"
        >Panel admin</router-link
      >

      <div class="role-pill" :class="isAdmin ? 'admin' : 'user'">
        {{ isAdmin ? 'Admin' : 'Usuario' }}
      </div>

      <div class="user-chip">
        <div class="chip-avatar">{{ userInitials }}</div>
        <span class="chip-name d-none d-md-inline">{{ username ?? 'Usuario' }}</span>
      </div>
    </div>
  </header>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useStore } from '@/store';

interface PageMeta {
  title: string;
  subtitle: string;
}

const PAGE_META_BY_ROUTE: Record<string, PageMeta> = {
  Home: { title: 'Dashboard', subtitle: 'Resumen general del negocio' },
  VentaWizard: { title: 'Nueva venta', subtitle: 'Generá una operación completa' },
  VentaList: { title: 'Ventas', subtitle: 'Seguimiento de operaciones y estados' },
  VehiculoSearch: { title: 'Buscar vehículo', subtitle: 'Catálogo y disponibilidad' },
  Vehiculo: { title: 'Vehículos', subtitle: 'Gestión de unidades' },
  Cliente: { title: 'Clientes', subtitle: 'Gestión de clientes y contactos' },
  Inventario: { title: 'Inventario', subtitle: 'Stock y movimientos' },
};

const DEFAULT_PAGE_META: PageMeta = {
  title: 'AutoGestión',
  subtitle: 'Plataforma de concesionaria',
};

const store = useStore();
const route = useRoute();

const username = computed(() => store.account?.login);
const userInitials = computed(() => (store.account?.login ?? '').slice(0, 2).toUpperCase());
const isAdmin = computed(() => store.account?.authorities?.includes('ROLE_ADMIN') ?? false);
const pageMeta = computed(() => PAGE_META_BY_ROUTE[route.name as string] ?? DEFAULT_PAGE_META);
</script>

<style scoped>
.app-topbar {
  min-height: 72px;
  background: #fff;
  border-bottom: 1px solid #e3e9f3;
  padding: 0.7rem 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.topbar-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: #111827;
}

.topbar-subtitle {
  font-size: 0.8rem;
  color: #6b7280;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 0.55rem;
}

.role-pill {
  border-radius: 999px;
  padding: 0.2rem 0.55rem;
  font-size: 0.72rem;
  font-weight: 600;
}

.role-pill.user {
  background: #e0f2fe;
  color: #0369a1;
}

.role-pill.admin {
  background: #fef3c7;
  color: #92400e;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.2rem 0.35rem;
  border-radius: 999px;
  background: #f8fafc;
}

.chip-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #2563eb;
  color: #fff;
  display: grid;
  place-content: center;
  font-size: 0.72rem;
  font-weight: 700;
}

.chip-name {
  font-size: 0.82rem;
  font-weight: 600;
  color: #1f2937;
  padding-right: 0.3rem;
}
</style>
