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
      { name: 'Marca', label: 'Marcas', icon: 'MA' },
      { name: 'Modelo', label: 'Modelos', icon: 'MO' },
      { name: 'Version', label: 'Versiones', icon: 'VE' },
      { name: 'Motor', label: 'Motores', icon: 'MT' },
      { name: 'VersionCompatibilityAdmin', label: 'Compatibilidades', icon: 'CM' },
      { name: 'TipoVehiculo', label: 'Tipos de vehiculo', icon: 'TV' },
      { name: 'Combustible', label: 'Combustibles', icon: 'CB' },
      { name: 'Traccion', label: 'Tracciones', icon: 'TR' },
      { name: 'TipoCaja', label: 'Tipos de caja', icon: 'TC' },
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
  width: var(--sidebar-width, 272px);
  flex: 0 0 var(--sidebar-width, 272px);
  min-height: 100vh;
  min-width: var(--sidebar-width, 272px);
  max-width: var(--sidebar-width, 272px);
  background:
    radial-gradient(circle at top right, rgba(37, 99, 235, 0.16), transparent 28%),
    linear-gradient(180deg, #0f172a 0%, #111827 100%);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(148, 163, 184, 0.14);
  overflow: hidden;
}

.sidebar-brand {
  min-height: 78px;
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 1rem 1.1rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}

.brand-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-content: center;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  font-size: 0.82rem;
  font-weight: 800;
  letter-spacing: 0.06em;
}

.brand-name {
  font-size: 0.98rem;
  font-weight: 700;
}

.brand-sub {
  margin-top: 0.2rem;
  font-size: 0.74rem;
  color: #94a3b8;
}

.sidebar-nav {
  flex: 1;
  padding: 0.95rem 0.7rem 1.1rem;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-section {
  margin: 0.8rem 0.55rem 0.35rem;
  font-size: 0.66rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #7dd3fc;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.2rem;
  padding: 0.68rem 0.72rem;
  border-radius: 14px;
  color: #dbeafe;
  text-decoration: none;
  font-size: 0.89rem;
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
  background: rgba(148, 163, 184, 0.12);
  color: #fff;
  transform: translateX(1px);
}

.sidebar-item.router-link-active {
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.44), rgba(14, 165, 233, 0.22));
  color: #fff;
  box-shadow: inset 0 0 0 1px rgba(125, 211, 252, 0.16);
}

.sidebar-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: grid;
  place-content: center;
  background: rgba(15, 23, 42, 0.4);
  color: #bfdbfe;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.95rem 1rem;
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  background: rgba(15, 23, 42, 0.28);
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: grid;
  place-content: center;
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  color: #fff;
  font-size: 0.77rem;
  font-weight: 700;
}

.user-meta {
  min-width: 0;
}

.user-name {
  font-size: 0.84rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  font-size: 0.72rem;
  color: #94a3b8;
}

.logout-btn {
  margin-left: auto;
  min-width: 44px;
  height: 34px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.5);
  color: #cbd5e1;
  font-size: 0.7rem;
  font-weight: 700;
  cursor: pointer;
}

.logout-btn:hover {
  color: #fff;
  border-color: rgba(125, 211, 252, 0.28);
}

</style>
