<template>
  <aside class="app-sidebar">
    <div class="sidebar-brand">
      <router-link to="/" class="brand-link">
        <img :src="brandLogoUrl" alt="AutoGestion" class="brand-logo" />
      </router-link>
    </div>

    <nav class="sidebar-nav">
      <template v-for="section in visibleSections" :key="section.title">
        <div class="sidebar-section">{{ section.title }}</div>
        <router-link v-for="item in section.items" :key="item.name" class="sidebar-item" :to="{ name: item.name }">
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
      <button class="logout-btn" @click="logout" title="Cerrar sesion">SALIR</button>
    </div>
  </aside>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { Authority } from '@/shared/jhipster/constants';
import { useStore } from '@/store';

interface SidebarItem {
  name: string;
  label: string;
  icon: string;
}

interface SidebarSection {
  title: string;
  items: SidebarItem[];
}

const USER_SECTIONS: SidebarSection[] = [
  { title: 'Principal', items: [{ name: 'Home', label: 'Inicio', icon: 'IN' }] },
  {
    title: 'Comercial',
    items: [
      { name: 'Cliente', label: 'Clientes', icon: 'CL' },
      { name: 'VentaEditor', label: 'Nueva venta', icon: 'NV' },
      { name: 'Venta', label: 'Ventas', icon: 'VT' },
      { name: 'Reserva', label: 'Reservas', icon: 'RS' },
      { name: 'TasacionUsado', label: 'Tasaciones', icon: 'TS' },
    ],
  },
  {
    title: 'Inventario',
    items: [
      { name: 'Inventario', label: 'Inventario', icon: 'IV' },
      { name: 'VehiculoSearch', label: 'Buscar vehiculo', icon: 'BV' },
    ],
  },
  {
    title: 'Finanzas',
    items: [
      { name: 'Pago', label: 'Pagos', icon: 'PG' },
      { name: 'Comprobante', label: 'Comprobantes', icon: 'CP' },
    ],
  },
];

const ADMIN_SECTIONS: SidebarSection[] = [
  { title: 'Principal', items: [{ name: 'Home', label: 'Inicio', icon: 'IN' }] },
  {
    title: 'Comercial',
    items: [
      { name: 'Cliente', label: 'Clientes', icon: 'CL' },
      { name: 'VentaEditor', label: 'Nueva venta', icon: 'NV' },
      { name: 'Venta', label: 'Ventas', icon: 'VT' },
      { name: 'Reserva', label: 'Reservas', icon: 'RS' },
      { name: 'TasacionUsado', label: 'Tasaciones', icon: 'TS' },
    ],
  },
  {
    title: 'Inventario',
    items: [
      { name: 'Inventario', label: 'Inventario', icon: 'IV' },
      { name: 'Vehiculo', label: 'Vehiculos', icon: 'VH' },
      { name: 'VehiculoSearch', label: 'Buscar vehiculo', icon: 'BV' },
    ],
  },
  {
    title: 'Finanzas',
    items: [
      { name: 'Pago', label: 'Pagos', icon: 'PG' },
      { name: 'Comprobante', label: 'Comprobantes', icon: 'CP' },
      { name: 'MovimientoCaja', label: 'Caja', icon: 'CJ' },
    ],
  },
  {
    title: 'Administracion',
    items: [
      { name: 'JhiUser', label: 'Usuarios', icon: 'US' },
      { name: 'JhiConfigurationComponent', label: 'Configuracion', icon: 'CF' },
    ],
  },
  {
    title: 'Catalogo tecnico',
    items: [
      { name: 'CatalogoTecnicoAdmin', label: 'Centro de catalogo', icon: 'CT' },
      { name: 'VersionCompatibilityAdmin', label: 'Compatibilidades', icon: 'CM' },
      { name: 'MetodoPago', label: 'Metodos de pago', icon: 'MP' },
      { name: 'Moneda', label: 'Monedas', icon: 'MN' },
      { name: 'Cotizacion', label: 'Cotizaciones', icon: 'CZ' },
    ],
  },
];

const store = useStore();
const router = useRouter();
const brandLogoUrl = '/content/images/branding/logo.png';
const username = computed(() => store.account?.login);
const authorities = computed(() => store.account?.authorities ?? []);
const isAdmin = computed(() => authorities.value.includes(Authority.ADMIN));
const roleLabel = computed(() => (isAdmin.value ? 'Administrador' : 'Usuario operativo'));
const visibleSections = computed(() => (isAdmin.value ? ADMIN_SECTIONS : USER_SECTIONS));

const userInitials = computed(() => {
  const source = (store.account?.login ?? '').trim();
  if (!source) return 'US';
  return source.slice(0, 2).toUpperCase();
});

async function logout() {
  localStorage.removeItem('jhi-authenticationToken');
  sessionStorage.removeItem('jhi-authenticationToken');
  store.logout();
  await router.push({ name: 'Login' });
}
</script>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width, 252px);
  flex: 0 0 var(--sidebar-width, 252px);
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(37, 99, 235, 0.1), transparent 28%),
    linear-gradient(180deg, #0f172a 0%, #131c2d 100%);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(148, 163, 184, 0.14);
}

.sidebar-brand {
  min-height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.95rem 0.9rem 0.8rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}

.brand-link {
  display: block;
  width: min(100%, 206px);
}

.brand-logo {
  width: 100%;
  height: clamp(48px, 5.2vw, 62px);
  object-fit: contain;
  object-position: center;
  display: block;
}

.sidebar-nav {
  flex: 1;
  padding: 0.7rem 0.58rem 1rem;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-section {
  margin: 0.72rem 0.48rem 0.24rem;
  font-size: 0.64rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #93c5fd;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 0.66rem;
  margin-bottom: 0.16rem;
  padding: 0.58rem 0.62rem;
  border-radius: 12px;
  color: #dbeafe;
  text-decoration: none;
  font-size: 0.84rem;
}

.sidebar-item:hover {
  background: rgba(148, 163, 184, 0.1);
  color: #fff;
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
  font-size: 0.63rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  flex-shrink: 0;
}

.sidebar-label {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  padding: 0.78rem 0.88rem;
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
  min-width: 52px;
  height: 30px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 9px;
  background: rgba(15, 23, 42, 0.45);
  color: #cbd5e1;
  font-size: 0.62rem;
  font-weight: 700;
}

.logout-btn:hover {
  color: #fff;
  border-color: rgba(125, 211, 252, 0.3);
}
</style>
