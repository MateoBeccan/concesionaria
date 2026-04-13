<template>
  <header class="app-topbar">
    <div class="topbar-copy">
      <p class="topbar-eyebrow">{{ pageContext.eyebrow }}</p>
      <h1 class="topbar-title">{{ pageContext.title }}</h1>
      <p v-if="pageContext.subtitle" class="topbar-subtitle">{{ pageContext.subtitle }}</p>
    </div>

    <div class="topbar-actions">
      <router-link v-if="pageContext.secondaryAction" :to="pageContext.secondaryAction.to" class="btn btn-outline btn-sm">
        {{ pageContext.secondaryAction.label }}
      </router-link>

      <router-link v-if="pageContext.action" :to="pageContext.action.to" class="btn btn-primary btn-sm topbar-primary-action">
        {{ pageContext.action.label }}
      </router-link>

      <div class="user-chip" :title="username">
        <div class="chip-avatar">{{ userInitials }}</div>
        <div class="chip-copy d-none d-md-flex">
          <span class="chip-label">{{ isAdmin ? 'Administrador' : 'Usuario' }}</span>
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
  secondaryAction?: TopbarAction;
}

const store = useStore();
const route = useRoute();

const username = computed(() => store.account?.login ?? 'Usuario');
const isAdmin = computed(() => store.account?.authorities?.includes('ROLE_ADMIN') ?? false);

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
      return isAdmin.value
        ? {
            eyebrow: 'Administracion',
            title: 'Dashboard administrativo',
            subtitle: 'Controla ventas, inventario, catalogo tecnico y configuracion del negocio.',
            secondaryAction: { label: 'Inventario', to: { name: 'Inventario' } },
            action: { label: 'Nuevo vehiculo', to: { name: 'VehiculoCreate' } },
          }
        : {
            eyebrow: 'Operacion',
            title: 'Dashboard operativo',
            subtitle: 'Prioriza ventas, clientes y unidades disponibles para trabajar mas rapido.',
            secondaryAction: { label: 'Buscar vehiculo', to: { name: 'VehiculoSearch' } },
            action: { label: 'Nueva venta', to: { name: 'VentaEditor' } },
          };
    case 'VentaList':
    case 'Venta':
      return {
        eyebrow: 'Ventas',
        title: isAdmin.value ? 'Historial de ventas' : 'Ventas',
        subtitle: isAdmin.value
          ? 'Revisa operaciones, seguimiento comercial y resultados recientes.'
          : 'Consulta y continua el flujo operativo de ventas.',
        action: { label: 'Nueva venta', to: { name: 'VentaEditor' } },
      };
    case 'VentaEditor':
    case 'VentaCreate':
      return {
        eyebrow: 'Ventas',
        title: 'Nueva venta',
        subtitle: 'Registra la operacion en un flujo unico, rapido y guiado.',
      };
    case 'VentaEditorEdit':
    case 'VentaEdit':
      return {
        eyebrow: 'Ventas',
        title: ventaId ? `Editar venta #${ventaId}` : 'Editar venta',
        subtitle: 'Ajusta cliente, unidades y pagos dentro de la misma operacion.',
      };
    case 'VentaView':
      return {
        eyebrow: 'Ventas',
        title: ventaId ? `Venta #${ventaId}` : 'Detalle de venta',
        subtitle: 'Consulta estado, pagos y datos clave de la operacion.',
        action: ventaId ? { label: 'Editar venta', to: { name: 'VentaEditorEdit', params: { ventaId: String(ventaId) } } } : undefined,
      };
    case 'VehiculoSearch':
      return {
        eyebrow: 'Operacion',
        title: 'Buscar vehiculo',
        subtitle: 'Encuentra unidades disponibles y abre una venta desde el resultado correcto.',
        action: { label: 'Nueva venta', to: { name: 'VentaEditor' } },
      };
    case 'Vehiculo':
      return {
        eyebrow: 'Catalogo',
        title: 'Catalogo de vehiculos',
        subtitle: 'Administra unidades, catalogo comercial y alta de vehiculos.',
        action: { label: 'Nuevo vehiculo', to: { name: 'VehiculoCreate' } },
      };
    case 'Inventario':
      return {
        eyebrow: 'Administracion',
        title: 'Inventario',
        subtitle: 'Controla disponibilidad, reservas, vencimientos y consistencia del stock.',
        action: { label: 'Nuevo registro', to: { name: 'InventarioCreate' } },
      };
    case 'Cliente':
      return {
        eyebrow: 'Clientes',
        title: 'Clientes',
        subtitle: 'Consulta y mantiene la base comercial de clientes.',
        action: { label: 'Nuevo cliente', to: { name: 'ClienteCreate' } },
      };
    case 'Cotizacion':
      return {
        eyebrow: 'Configuracion',
        title: 'Cotizaciones',
        subtitle: 'Gestiona valores vigentes por moneda para operaciones y presupuestos.',
        action: { label: 'Nueva cotizacion', to: { name: 'CotizacionCreate' } },
      };
    case 'VersionCompatibilityAdmin':
      return {
        eyebrow: 'Catalogo tecnico',
        title: 'Compatibilidades',
        subtitle: 'Relaciona versiones y motores con una vista administrativa orientada a negocio.',
      };
    case 'Marca':
    case 'Modelo':
    case 'Version':
    case 'Motor':
    case 'Combustible':
    case 'Traccion':
    case 'TipoCaja':
    case 'TipoVehiculo':
      return {
        eyebrow: 'Catalogo tecnico',
        title: routeName.value.replace(/([A-Z])/g, ' $1').trim(),
        subtitle: 'Mantiene la estructura tecnica del catalogo que usa el negocio.',
      };
    case 'MetodoPago':
    case 'Moneda':
    case 'TipoDocumento':
    case 'CondicionIva':
      return {
        eyebrow: 'Configuracion',
        title: routeName.value.replace(/([A-Z])/g, ' $1').trim(),
        subtitle: 'Gestiona parametros y catalogos administrativos del sistema.',
      };
    default:
      return {
        eyebrow: 'Sistema',
        title: isAdmin.value ? 'Panel administrativo' : 'Panel operativo',
        subtitle: isAdmin.value
          ? 'Administracion comercial y tecnica en una sola vista.'
          : 'Operacion comercial simple, guiada y enfocada en ventas.',
      };
  }
});
</script>

<style scoped>
.app-topbar {
  min-height: 84px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.35rem;
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid #e5eaf2;
  backdrop-filter: blur(10px);
}

.topbar-copy {
  min-width: 0;
}

.topbar-eyebrow {
  margin: 0 0 0.18rem;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #0284c7;
}

.topbar-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
  color: #0f172a;
}

.topbar-subtitle {
  margin: 0.2rem 0 0;
  font-size: 0.86rem;
  color: #64748b;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.topbar-primary-action {
  min-width: 132px;
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
    flex-wrap: wrap;
  }
}
</style>
