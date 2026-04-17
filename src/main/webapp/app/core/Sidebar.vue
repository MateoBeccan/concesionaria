<template>
  <aside class="app-sidebar">
    <div class="sidebar-brand">
      <div class="brand-icon">AG</div>
      <div>
        <div class="brand-name">AutoGestion</div>
        <div class="brand-sub">{{ isAdmin ? 'Panel administrativo' : 'Panel operativo' }}</div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <template v-for="section in visibleSections" :key="section.title">
        <div class="sidebar-section">{{ section.title }}</div>

        <router-link
          v-for="item in section.items"
          :key="item.name"
          class="sidebar-item"
          :to="item.to ?? { name: item.name }"
        >
          <span class="sidebar-icon">{{ item.icon }}</span>
          <span class="sidebar-label">{{ item.label }}</span>
        </router-link>
      </template>
    </nav>

    <div class="sidebar-footer">
      <div class="user-avatar">{{ userInitials }}</div>

      <div class="user-meta">
        <div class="user-name">{{ username ?? 'Usuario' }}</div>
        <div class="user-status">{{ roleLabel }}</div>
      </div>

      <button class="logout-btn" @click="logout" title="Cerrar sesion">
        OUT
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
  to?: { name: string };
}

interface SidebarSection {
  title: string;
  items: SidebarItem[];
}

const USER_SECTIONS: SidebarSection[] = [
  {
    title: 'Principal',
    items: [{ name: 'Home', label: 'Dashboard', icon: 'DB' }],
  },
  {
    title: 'Ventas',
    items: [
      { name: 'VentaEditor', label: 'Nueva venta', icon: 'NV' },
      { name: 'VentaList', label: 'Ventas', icon: 'VT' },
      { name: 'VehiculoSearch', label: 'Buscar vehiculo', icon: 'BV' },
    ],
  },
  {
    title: 'Clientes',
    items: [
      { name: 'Cliente', label: 'Clientes', icon: 'CL' },
      { name: 'ClienteCreate', label: 'Nuevo cliente', icon: 'NC' },
    ],
  },
];

const ADMIN_SECTIONS: SidebarSection[] = [
  {
    title: 'Principal',
    items: [{ name: 'Home', label: 'Dashboard', icon: 'DB' }],
  },
  {
    title: 'Operaciones',
    items: [
      { name: 'VentaEditor', label: 'Nueva venta', icon: 'NV' },
      { name: 'VentaList', label: 'Historial de ventas', icon: 'VT' },
      { name: 'VehiculoSearch', label: 'Buscar vehiculo', icon: 'BV' },
      { name: 'Cliente', label: 'Clientes', icon: 'CL' },
      { name: 'Vehiculo', label: 'Catalogo de vehiculos', icon: 'VH' },
      { name: 'Inventario', label: 'Inventario', icon: 'IN' },
    ],
  },
  {
    title: 'Catalogo tecnico',
    items: [
      { name: 'CatalogoTecnicoAdmin', label: 'Centro de catalogo', icon: 'CT' },
      { name: 'VersionCompatibilityAdmin', label: 'Compatibilidades', icon: 'CM' },
    ],
  },
  {
    title: 'Configuracion',
    items: [
      { name: 'Cotizacion', label: 'Cotizaciones', icon: 'CZ' },
      { name: 'MetodoPago', label: 'Metodos de pago', icon: 'MP' },
      { name: 'Moneda', label: 'Monedas', icon: 'MN' },
      { name: 'TipoDocumento', label: 'Tipos de documento', icon: 'TD' },
      { name: 'CondicionIva', label: 'Condiciones IVA', icon: 'CI' },
    ],
  },
  {
    title: 'Sistema',
    items: [
      { name: 'JhiUser', label: 'Usuarios', icon: 'US' },
      { name: 'JhiMetricsComponent', label: 'Metricas', icon: 'ME' },
      { name: 'JhiHealthComponent', label: 'Salud', icon: 'HL' },
    ],
  },
];

const store = useStore();
const router = useRouter();

const username = computed(() => store.account?.login);
const authorities = computed(() => store.account?.authorities ?? []);

const isAdmin = computed(() => authorities.value.includes('ROLE_ADMIN'));

const roleLabel = computed(() => (isAdmin.value ? 'Administrador' : 'Usuario operativo'));

const userInitials = computed(() => {
  const source = (store.account?.login ?? '').trim();
  if (!source) return 'US';
  return source.slice(0, 2).toUpperCase();
});

const visibleSections = computed(() => (isAdmin.value ? ADMIN_SECTIONS : USER_SECTIONS));

async function logout() {
  localStorage.removeItem('jhi-authenticationToken');
  sessionStorage.removeItem('jhi-authenticationToken');
  store.logout();
  await router.push({ name: 'Login' });
}
</script>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width, 248px);
  flex: 0 0 var(--sidebar-width, 248px);
  min-height: 100vh;
  min-width: var(--sidebar-width, 248px);
  max-width: var(--sidebar-width, 248px);
  background:
    radial-gradient(circle at top right, rgba(37, 99, 235, 0.12), transparent 24%),
    linear-gradient(180deg, #0f172a 0%, #131c2d 100%);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(148, 163, 184, 0.14);
  overflow: hidden;
}

.sidebar-brand {
  min-height: 70px;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.9rem 0.95rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}

.brand-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: grid;
  place-content: center;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.06em;
}

.brand-name {
  font-size: 0.92rem;
  font-weight: 700;
}

.brand-sub {
  margin-top: 0.12rem;
  font-size: 0.7rem;
  color: #94a3b8;
}

.sidebar-nav {
  flex: 1;
  padding: 0.8rem 0.6rem 1rem;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-section {
  margin: 0.7rem 0.5rem 0.28rem;
  font-size: 0.63rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #93c5fd;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  margin-bottom: 0.16rem;
  padding: 0.58rem 0.62rem;
  border-radius: 12px;
  color: #dbeafe;
  text-decoration: none;
  font-size: 0.84rem;
  min-width: 0;
  transition:
    background-color 0.2s ease,
    transform 0.2s ease,
    color 0.2s ease;
}

.sidebar-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-item:hover {
  background: rgba(148, 163, 184, 0.1);
  color: #fff;
  transform: translateX(1px);
}

.sidebar-item.router-link-active {
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.34), rgba(14, 165, 233, 0.16));
  color: #fff;
  box-shadow: inset 0 0 0 1px rgba(125, 211, 252, 0.12);
}

.sidebar-icon {
  width: 28px;
  height: 28px;
  border-radius: 9px;
  display: grid;
  place-content: center;
  background: rgba(15, 23, 42, 0.4);
  color: #bfdbfe;
  font-size: 0.64rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  padding: 0.8rem 0.9rem;
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(15, 23, 42, 0.2);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-content: center;
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 700;
}

.user-meta {
  min-width: 0;
}

.user-name {
  font-size: 0.78rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  font-size: 0.68rem;
  color: #94a3b8;
}

.logout-btn {
  margin-left: auto;
  min-width: 38px;
  height: 30px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 9px;
  background: rgba(15, 23, 42, 0.5);
  color: #cbd5e1;
  font-size: 0.64rem;
  font-weight: 700;
  cursor: pointer;
}

.logout-btn:hover {
  color: #fff;
  border-color: rgba(125, 211, 252, 0.28);
}

</style>
