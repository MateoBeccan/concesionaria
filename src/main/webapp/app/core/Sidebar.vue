<template>
  <aside class="app-sidebar">

    <!-- BRAND -->
    <div class="sidebar-brand">
      <div class="brand-icon">🚗</div>
      <div>
        <div class="brand-name">AutoGestión</div>
        <div class="brand-sub">Sistema de Concesionaria</div>
      </div>
    </div>

    <!-- NAV -->
    <nav class="sidebar-nav">
      <template v-for="section in visibleSections" :key="section.title">
        <div class="sidebar-section">{{ section.title }}</div>

        <router-link
          v-for="item in section.items"
          :key="item.name"
          class="sidebar-item"
          :to="{ name: item.name }"
        >
          <span class="sidebar-icon">{{ item.icon }}</span>
          <span class="sidebar-label">{{ item.label }}</span>
        </router-link>
      </template>
    </nav>

    <!-- FOOTER -->
    <div class="sidebar-footer">
      <div class="user-avatar">{{ userInitials }}</div>

      <div class="user-meta">
        <div class="user-name">{{ username ?? 'Usuario' }}</div>
        <div class="user-status">{{ roleLabel }}</div>
      </div>

      <button class="logout-btn" @click="logout" title="Cerrar sesión">
        ⏻
      </button>
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
    items: [
      { name: 'Home', label: 'Dashboard', icon: '📊' },
    ],
  },
  {
    title: 'Operaciones',
    items: [
      { name: 'VehiculoSearch', label: 'Buscar Vehículo', icon: '🔍' },
      { name: 'VentaEditor', label: 'Nueva Venta', icon: '💰' },
      { name: 'VentaList', label: 'Ventas', icon: '📄' },
    ],
  },
  {
    title: 'Clientes',
    items: [
      { name: 'Cliente', label: 'Clientes', icon: '👤' },
      { name: 'ClienteCreate', label: 'Nuevo Cliente', icon: '➕' },
    ],
  },
  {
    title: 'Administración',
    roles: ['ROLE_ADMIN'],
    items: [
      { name: 'Vehiculo', label: 'Vehículos', icon: '🚙' },
      { name: 'Inventario', label: 'Inventario', icon: '📦' },
      { name: 'Cotizacion', label: 'Cotizaciones', icon: '💱' },
    ],
  },
];

const store = useStore();
const router = useRouter();

const username = computed(() => store.account?.login);
const authorities = computed(() => store.account?.authorities ?? []);

const isAdmin = computed(() =>
  authorities.value.includes('ROLE_ADMIN')
);

const roleLabel = computed(() =>
  isAdmin.value ? 'Administrador' : 'Usuario'
);

const userInitials = computed(() => {
  const u = store.account?.login ?? '';
  return u.slice(0, 2).toUpperCase();
});

const visibleSections = computed(() =>
  SIDEBAR_SECTIONS.filter(section => {
    if (!section.roles?.length) return true;
    return section.roles.some(role =>
      authorities.value.includes(role)
    );
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
  width: 260px;
  min-height: 100vh;
  background: #0f172a;
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(148, 163, 184, 0.15);
}

/* BRAND */
.sidebar-brand {
  height: 70px;
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0 1rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}

.brand-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: grid;
  place-content: center;
  background: #2563eb;
  font-size: 1.1rem;
}

.brand-name {
  font-weight: 700;
  font-size: 0.95rem;
}

.brand-sub {
  font-size: 0.7rem;
  color: #94a3b8;
}

/* NAV */
.sidebar-nav {
  padding: 0.8rem 0.6rem;
  display: flex;
  flex-direction: column;
}

.sidebar-section {
  margin: 0.7rem 0.6rem 0.3rem;
  font-size: 0.65rem;
  text-transform: uppercase;
  color: #94a3b8;
  letter-spacing: 0.05em;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.55rem 0.65rem;
  border-radius: 10px;
  color: #e2e8f0;
  text-decoration: none;
  font-size: 0.88rem;
  transition: all 0.2s ease;
}

.sidebar-item:hover {
  background: rgba(148, 163, 184, 0.12);
}

.sidebar-item.router-link-active {
  background: rgba(37, 99, 235, 0.35);
  color: #fff;
}

.sidebar-icon {
  width: 18px;
  text-align: center;
}

/* FOOTER */
.sidebar-footer {
  margin-top: auto;
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  padding: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #1d4ed8;
  display: grid;
  place-content: center;
  font-weight: 700;
  font-size: 0.75rem;
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
  background: transparent;
  border: none;
  color: #94a3b8;
  cursor: pointer;
}

.logout-btn:hover {
  color: #fff;
}
</style>
