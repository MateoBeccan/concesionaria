<template>
  <aside class="app-sidebar">
    <div class="sidebar-brand">
      <span class="brand-icon">🚗</span>
      <div>
        <span class="brand-name">AutoGestión</span>
        <span class="brand-sub">Sistema de Concesionaria</span>
      </div>
    </div>

    <nav class="sidebar-nav">
      <template v-for="section in visibleSections" :key="section.title">
        <div class="sidebar-section">{{ section.title }}</div>
npm
        <router-link v-for="item in section.items" :key="item.name" class="sidebar-item" :to="{ name: item.name }">
          <span class="sidebar-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </template>
    </nav>

    <div class="sidebar-footer">
      <div class="user-avatar">{{ userInitials }}</div>
      <div class="user-meta">
        <div class="user-name">{{ username ?? 'Usuario' }}</div>
        <div class="user-status">{{ roleLabel }}</div>
      </div>
      <button class="logout-btn" @click="logout" title="Cerrar sesión">⏻</button>
    </div>
  </aside>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from '@/store';

interface SidebarItem {
  name: string;
  label: string;
  icon: string;
}

interface SidebarSection {
  title: string;
  roles?: string[];
  items: SidebarItem[];
}

const SIDEBAR_SECTIONS: SidebarSection[] = [
  {
    title: 'Principal',
    items: [{ name: 'Home', label: 'Dashboard', icon: '⊞' }],
  },
  {
    title: 'Operaciones',
    items: [
      { name: 'VehiculoSearch', label: 'Buscar Vehículo', icon: '🔍' },
      { name: 'VentaWizard', label: 'Nueva venta', icon: '💰' },
      { name: 'VentaList', label: 'Ventas', icon: '📄' },
    ],
  },
  {
    title: 'Clientes',
    items: [
      { name: 'Cliente', label: 'Clientes', icon: '👤' },
      { name: 'ClienteCreate', label: 'Nuevo Cliente', icon: '＋' },
    ],
  },
  {
    title: 'Administración',
    roles: ['ROLE_ADMIN'],
    items: [
      { name: 'Vehiculo', label: 'Vehículos', icon: '🚙' },
      { name: 'Inventario', label: 'Inventario', icon: '📦' },
    ],
  },
];

const store = useStore();
const router = useRouter();

const username = computed(() => store.account?.login);
const authorities = computed(() => store.account?.authorities ?? []);
const isAdmin = computed(() => authorities.value.includes('ROLE_ADMIN'));

const roleLabel = computed(() => (isAdmin.value ? 'Administrador' : 'Usuario'));

const userInitials = computed(() => {
  const u = store.account?.login ?? '';
  return u.slice(0, 2).toUpperCase();
});

const visibleSections = computed(() =>
  SIDEBAR_SECTIONS.filter(section => {
    if (!section.roles?.length) return true;
    return section.roles.some(role => authorities.value.includes(role));
  }),
);

async function logout() {
  localStorage.removeItem('jhi-authenticationToken');
  sessionStorage.removeItem('jhi-authenticationToken');
  store.logout();
  await router.push({ name: 'Login' });
}
</script>

<style scoped>
.app-sidebar {
  width: 250px;
  min-height: 100vh;
  background: #0f172a;
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(148, 163, 184, 0.2);
}

.sidebar-brand {
  min-height: 72px;
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.9rem 1rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.brand-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: grid;
  place-content: center;
  background: #2563eb;
}

.brand-name {
  display: block;
  font-weight: 700;
  font-size: 0.92rem;
}

.brand-sub {
  display: block;
  font-size: 0.72rem;
  color: #94a3b8;
}

.sidebar-nav {
  padding: 0.9rem 0.55rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.sidebar-section {
  margin: 0.65rem 0.55rem 0.3rem;
  font-size: 0.68rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #94a3b8;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  border-radius: 10px;
  padding: 0.5rem 0.6rem;
  color: #e2e8f0;
  text-decoration: none;
  font-size: 0.88rem;
}

.sidebar-item:hover {
  background: rgba(148, 163, 184, 0.18);
}

.sidebar-item.router-link-active {
  background: rgba(37, 99, 235, 0.3);
  color: #fff;
}

.sidebar-footer {
  margin-top: auto;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  padding: 0.85rem 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.55rem;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #1d4ed8;
  color: #fff;
  display: grid;
  place-content: center;
  font-size: 0.75rem;
  font-weight: 700;
}

.user-meta {
  min-width: 0;
}

.user-name {
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  font-size: 0.7rem;
  color: #94a3b8;
}

.logout-btn {
  margin-left: auto;
  border: none;
  background: transparent;
  color: #94a3b8;
  font-size: 0.9rem;
  cursor: pointer;
}

.logout-btn:hover {
  color: #fff;
}
</style>
