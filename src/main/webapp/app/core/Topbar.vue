<template>
  <header class="app-topbar">
    <div class="topbar-copy">
      <p class="topbar-eyebrow">{{ pageContext.eyebrow }}</p>
      <h1 class="topbar-title">{{ pageContext.title }}</h1>
      <p v-if="pageContext.subtitle" class="topbar-subtitle">{{ pageContext.subtitle }}</p>
    </div>

    <div class="topbar-actions">
      <router-link v-if="pageContext.action" :to="pageContext.action.to" class="btn btn-primary btn-sm topbar-primary-action">
        {{ pageContext.action.label }}
      </router-link>

      <div class="user-chip" :title="username">
        <div class="chip-avatar">{{ userInitials }}</div>
        <div class="chip-copy d-none d-md-flex">
          <span class="chip-label">Usuario</span>
          <span class="chip-name">{{ username }}</span>
        </div>
      </div>
    </div>
  </header>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

import { useStore } from '@/store';

interface TopbarAction {
  label: string;
  to: { name: string; params?: Record<string, string | number> };
}

interface PageContext {
  eyebrow: string;
  title: string;
  subtitle: string;
  action?: TopbarAction;
}

const store = useStore();
const route = useRoute();

const username = computed(() => store.account?.login ?? 'Usuario');

const userInitials = computed(() => {
  const source = username.value.trim();
  if (!source) return 'US';

  const parts = source.split(/[\s._-]+/).filter(Boolean);
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  }

  return source.slice(0, 2).toUpperCase();
});

const routeName = computed(() => String(route.name ?? ''));

const pageContext = computed<PageContext>(() => {
  const ventaId = route.params?.ventaId;

  switch (routeName.value) {
    case 'Home':
      return {
        eyebrow: 'Panel',
        title: 'Dashboard',
        subtitle: 'Resumen rapido del negocio y accesos clave.',
        action: { label: 'Nueva venta', to: { name: 'VentaEditor' } },
      };
    case 'VentaList':
    case 'Venta':
      return {
        eyebrow: 'Ventas',
        title: 'Ventas',
        subtitle: 'Consulta operaciones y continua el seguimiento comercial.',
        action: { label: 'Nueva venta', to: { name: 'VentaEditor' } },
      };
    case 'VentaEditor':
    case 'VentaCreate':
      return {
        eyebrow: 'Ventas',
        title: 'Nueva venta',
        subtitle: 'Registra la operacion en un flujo unico y guiado.',
      };
    case 'VentaEditorEdit':
    case 'VentaEdit':
      return {
        eyebrow: 'Ventas',
        title: ventaId ? `Editar venta #${ventaId}` : 'Editar venta',
        subtitle: 'Ajusta cliente, vehiculos y pagos dentro de la misma operacion.',
      };
    case 'VentaView':
      return {
        eyebrow: 'Ventas',
        title: ventaId ? `Venta #${ventaId}` : 'Detalle de venta',
        subtitle: 'Consulta el estado, los pagos y los comprobantes de la operacion.',
        action: ventaId ? { label: 'Editar venta', to: { name: 'VentaEditorEdit', params: { ventaId: String(ventaId) } } } : undefined,
      };
    case 'VehiculoSearch':
      return {
        eyebrow: 'Catalogo',
        title: 'Buscar vehiculo',
        subtitle: 'Encuentra unidades disponibles para una operacion comercial.',
        action: { label: 'Nueva venta', to: { name: 'VentaEditor' } },
      };
    case 'Vehiculo':
      return {
        eyebrow: 'Catalogo',
        title: 'Vehiculos',
        subtitle: 'Administra unidades publicadas y disponibles.',
      };
    case 'Cliente':
      return {
        eyebrow: 'Clientes',
        title: 'Clientes',
        subtitle: 'Consulta y mantiene la base de clientes.',
        action: { label: 'Nueva venta', to: { name: 'VentaEditor' } },
      };
    case 'Inventario':
      return {
        eyebrow: 'Operaciones',
        title: 'Inventario',
        subtitle: 'Controla stock, estado y disponibilidad.',
      };
    case 'Cotizacion':
      return {
        eyebrow: 'Administracion',
        title: 'Cotizaciones',
        subtitle: 'Revisa los valores vigentes por moneda.',
      };
    default:
      return {
        eyebrow: 'Sistema',
        title: 'Concesionaria',
        subtitle: 'Operacion comercial y administracion en un solo lugar.',
      };
  }
});
</script>

<style scoped>
.app-topbar {
  min-height: 76px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 0.9rem 1.25rem;
  background: #ffffff;
  border-bottom: 1px solid #e5eaf2;
}

.topbar-copy {
  min-width: 0;
}

.topbar-eyebrow {
  margin: 0 0 0.15rem;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
}

.topbar-title {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
  color: #0f172a;
}

.topbar-subtitle {
  margin: 0.15rem 0 0;
  font-size: 0.84rem;
  color: #64748b;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.topbar-primary-action {
  min-width: 118px;
  justify-content: center;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.35rem 0.45rem;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: #f8fafc;
}

.chip-avatar {
  width: 34px;
  height: 34px;
  display: grid;
  place-content: center;
  border-radius: 999px;
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  color: #fff;
  font-size: 0.76rem;
  font-weight: 700;
}

.chip-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.chip-label {
  font-size: 0.68rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.chip-name {
  font-size: 0.84rem;
  font-weight: 600;
  color: #0f172a;
}

@media (max-width: 767px) {
  .app-topbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .topbar-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
