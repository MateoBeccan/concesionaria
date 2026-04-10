<template>
  <div class="dashboard">

    <!-- HEADER -->
    <div class="dashboard-header">
      <div>
        <h1>Dashboard</h1>
        <p>{{ fechaHoy }}</p>
      </div>
    </div>

    <!-- KPIs -->
    <div class="kpi-grid">

      <div class="kpi-card" v-for="kpi in kpiList" :key="kpi.label">
        <div>
          <div class="kpi-label">{{ kpi.label }}</div>

          <div v-if="!loadingKpis" class="kpi-value">
            {{ kpi.value }}
          </div>

          <div v-else class="placeholder">...</div>

          <div class="kpi-sub">{{ kpi.sub }}</div>
        </div>

        <div class="kpi-icon">
          {{ kpi.icon }}
        </div>
      </div>

    </div>

    <!-- MAIN -->
    <div class="dashboard-main">

      <!-- ACCIONES -->
      <div class="card">

        <div class="card-header">Acciones rápidas</div>

        <div class="quick-actions">

          <router-link :to="{ name: 'VentaWizard' }" class="quick-item">
            💰 Nueva Venta
          </router-link>

          <router-link :to="{ name: 'VehiculoSearch' }" class="quick-item">
            🚗 Buscar Vehículo
          </router-link>

          <router-link :to="{ name: 'ClienteCreate' }" class="quick-item">
            👤 Nuevo Cliente
          </router-link>

          <router-link :to="{ name: 'VehiculoCreate' }" class="quick-item">
            ➕ Nuevo Vehículo
          </router-link>

        </div>
      </div>

      <!-- VENTAS -->
      <div class="card">

        <div class="card-header d-flex justify-between">
          <span>Últimas ventas</span>

          <router-link :to="{ name: 'VentaList' }" class="btn btn-outline btn-sm">
            Ver todas
          </router-link>
        </div>

        <div v-if="loadingVentas" class="empty">
          Cargando ventas...
        </div>

        <div v-else-if="ultimasVentas.length === 0" class="empty">
          No hay ventas registradas
        </div>

        <table v-else class="table">

          <thead>
          <tr>
            <th>#</th>
            <th>Fecha</th>
            <th>Cliente</th>
            <th>Total</th>
            <th>Estado</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="v in ultimasVentas" :key="v.id">

            <td>{{ v.id }}</td>

            <td>{{ formatFecha(v.fecha) }}</td>

            <td>
              {{ v.cliente?.nombre }} {{ v.cliente?.apellido }}
            </td>

            <td class="price">
              $ {{ formatPrecio(v.total) }}
            </td>

            <td>
                <span :class="badgeVenta(v.estado)">
                  {{ v.estado ?? '-' }}
                </span>
            </td>

          </tr>
          </tbody>

        </table>

      </div>

    </div>

  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import VentaService from '@/entities/venta/venta.service';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import ClienteService from '@/entities/cliente/cliente.service';
import InventarioService from '@/entities/inventario/inventario.service';
import type { IVenta } from '@/shared/model/venta.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';

const ventaService = new VentaService();
const vehiculoService = new VehiculoService();
const clienteService = new ClienteService();
const inventarioService = new InventarioService();

const loadingKpis = ref(true);
const loadingVentas = ref(true);
const ultimasVentas = ref<IVenta[]>([]);

const kpis = ref({
  ventasMes: 0,
  vehiculosDisponibles: 0,
  clientesActivos: 0,
  inventarioTotal: 0,
});

const kpiList = computed(() => [
  { label: 'Ventas', value: kpis.value.ventasMes, sub: 'este mes', icon: '💰' },
  { label: 'Vehículos', value: kpis.value.vehiculosDisponibles, sub: 'disponibles', icon: '🚗' },
  { label: 'Clientes', value: kpis.value.clientesActivos, sub: 'activos', icon: '👤' },
  { label: 'Inventario', value: kpis.value.inventarioTotal, sub: 'total', icon: '📦' },
]);

const fechaHoy = new Date().toLocaleDateString('es-AR', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
});

onMounted(async () => {
  await Promise.all([cargarKpis(), cargarUltimasVentas()]);
});

async function cargarKpis() {
  loadingKpis.value = true;

  try {
    const [ventas, vehiculos, clientes, inventario] = await Promise.all([
      ventaService.retrieve({ page: 0, size: 1 }),
      vehiculoService.retrieve({ page: 0, size: 1 }),
      clienteService.retrieve({ page: 0, size: 1 }),
      inventarioService.retrieve({ page: 0, size: 1 }),
    ]);

    kpis.value.ventasMes = Number(ventas.headers['x-total-count'] ?? 0);
    kpis.value.vehiculosDisponibles = Number(vehiculos.headers['x-total-count'] ?? 0);
    kpis.value.clientesActivos = Number(clientes.headers['x-total-count'] ?? 0);
    kpis.value.inventarioTotal = Number(inventario.headers['x-total-count'] ?? 0);

  } catch {
    console.error('Error cargando KPIs');
  } finally {
    loadingKpis.value = false;
  }
}

async function cargarUltimasVentas() {
  loadingVentas.value = true;

  try {
    const res = await ventaService.retrieve({
      page: 0,
      size: 5,
      sort: ['fecha,desc'],
    });

    ultimasVentas.value = res.data;

  } catch {
    ultimasVentas.value = [];
  } finally {
    loadingVentas.value = false;
  }
}

function badgeVenta(estado?: EstadoVenta | null) {
  const map: Record<string, string> = {
    PENDIENTE: 'badge warning',
    FINALIZADA: 'badge success',
    CANCELADA: 'badge danger',
  };
  return map[estado ?? ''] ?? 'badge';
}

function formatFecha(f?: Date | string) {
  return f ? new Date(f).toLocaleDateString('es-AR') : '-';
}

function formatPrecio(p?: number | null) {
  return Number(p ?? 0).toLocaleString('es-AR');
}
</script>

<style scoped>
.dashboard {
  padding: 1.5rem;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.actions {
  display: flex;
  gap: .5rem;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.kpi-card {
  background: white;
  padding: 1rem;
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
}

.kpi-value {
  font-size: 1.5rem;
  font-weight: bold;
}

.dashboard-main {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 1rem;
}

.card {
  background: white;
  border-radius: 10px;
  padding: 1rem;
}

.quick-item {
  display: block;
  padding: .6rem;
  border-bottom: 1px solid #eee;
}

.table {
  width: 100%;
}

.price {
  color: #2563eb;
  font-weight: bold;
}

.badge {
  padding: .3rem .6rem;
  border-radius: 10px;
}

.badge.success { background: #22c55e; color: white; }
.badge.warning { background: #facc15; }
.badge.danger  { background: #ef4444; color: white; }

.empty {
  padding: 2rem;
  text-align: center;
  color: #999;
}
</style>
