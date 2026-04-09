<template>
  <div>

    <!-- PAGE HEADER -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Dashboard</h1>
        <p class="page-subtitle">Resumen de operaciones — {{ fechaHoy }}</p>
      </div>
      <router-link :to="{ name: 'VentaEditor' }" class="btn btn-primary">
        ＋ Nueva Venta
      </router-link>
    </div>

    <!-- KPIs -->
    <div class="row g-3 mb-4">

      <div class="col-sm-6 col-xl-3">
        <div class="kpi-card">
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <div class="kpi-label">Ventas del mes</div>
              <div class="kpi-value" v-if="!loadingKpis">{{ kpis.ventasMes }}</div>
              <div v-else class="skeleton" style="height:2rem;width:4rem;margin-top:.25rem" />
              <div class="kpi-sub">operaciones registradas</div>
            </div>
            <div class="kpi-icon" style="background:#eff6ff;color:#2563eb">💰</div>
          </div>
        </div>
      </div>

      <div class="col-sm-6 col-xl-3">
        <div class="kpi-card">
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <div class="kpi-label">Vehículos disponibles</div>
              <div class="kpi-value" v-if="!loadingKpis">{{ kpis.vehiculosDisponibles }}</div>
              <div v-else class="skeleton" style="height:2rem;width:4rem;margin-top:.25rem" />
              <div class="kpi-sub">en stock activo</div>
            </div>
            <div class="kpi-icon" style="background:#f0fdf4;color:#16a34a">🚗</div>
          </div>
        </div>
      </div>

      <div class="col-sm-6 col-xl-3">
        <div class="kpi-card">
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <div class="kpi-label">Clientes activos</div>
              <div class="kpi-value" v-if="!loadingKpis">{{ kpis.clientesActivos }}</div>
              <div v-else class="skeleton" style="height:2rem;width:4rem;margin-top:.25rem" />
              <div class="kpi-sub">registrados en el sistema</div>
            </div>
            <div class="kpi-icon" style="background:#faf5ff;color:#7c3aed">👤</div>
          </div>
        </div>
      </div>

      <div class="col-sm-6 col-xl-3">
        <div class="kpi-card">
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <div class="kpi-label">Inventario total</div>
              <div class="kpi-value" v-if="!loadingKpis">{{ kpis.inventarioTotal }}</div>
              <div v-else class="skeleton" style="height:2rem;width:4rem;margin-top:.25rem" />
              <div class="kpi-sub">unidades en inventario</div>
            </div>
            <div class="kpi-icon" style="background:#fff7ed;color:#d97706">📦</div>
          </div>
        </div>
      </div>

    </div>

    <!-- ACCESOS RÁPIDOS + ÚLTIMAS VENTAS -->
    <div class="row g-3">

      <!-- ACCESOS RÁPIDOS -->
      <div class="col-lg-4">
        <div class="card h-100">
          <div class="card-header">Accesos rápidos</div>
          <div class="card-body p-0">

            <router-link :to="{ name: 'VentaEditor' }" class="quick-action-item">
              <div class="quick-action-icon" style="background:#eff6ff;color:#2563eb">💰</div>
              <div>
                <div class="quick-action-title">Nueva Venta</div>
                <div class="quick-action-sub">Registrar una operación</div>
              </div>
              <span class="ms-auto text-muted">›</span>
            </router-link>

            <router-link :to="{ name: 'VehiculoSearch' }" class="quick-action-item">
              <div class="quick-action-icon" style="background:#f0fdf4;color:#16a34a">🔍</div>
              <div>
                <div class="quick-action-title">Buscar Vehículo</div>
                <div class="quick-action-sub">Por patente o crear nuevo</div>
              </div>
              <span class="ms-auto text-muted">›</span>
            </router-link>

            <router-link :to="{ name: 'ClienteCreate' }" class="quick-action-item">
              <div class="quick-action-icon" style="background:#faf5ff;color:#7c3aed">👤</div>
              <div>
                <div class="quick-action-title">Nuevo Cliente</div>
                <div class="quick-action-sub">Registrar un cliente</div>
              </div>
              <span class="ms-auto text-muted">›</span>
            </router-link>

            <router-link :to="{ name: 'VehiculoCreate' }" class="quick-action-item" style="border-bottom:none">
              <div class="quick-action-icon" style="background:#fff7ed;color:#d97706">🚗</div>
              <div>
                <div class="quick-action-title">Nuevo Vehículo</div>
                <div class="quick-action-sub">Agregar al inventario</div>
              </div>
              <span class="ms-auto text-muted">›</span>
            </router-link>

          </div>
        </div>
      </div>

      <!-- ÚLTIMAS VENTAS -->
      <div class="col-lg-8">
        <div class="card h-100">
          <div class="card-header d-flex justify-content-between align-items-center">
            <span>Últimas ventas</span>
            <router-link :to="{ name: 'VentaList' }" class="btn btn-sm btn-outline-secondary">
              Ver todas
            </router-link>
          </div>

          <!-- Loading -->
          <div v-if="loadingVentas" class="card-body">
            <div v-for="i in 4" :key="i" class="d-flex gap-3 mb-3">
              <div class="skeleton" style="height:1rem;width:3rem" />
              <div class="skeleton flex-grow-1" style="height:1rem" />
              <div class="skeleton" style="height:1rem;width:5rem" />
            </div>
          </div>

          <!-- Empty -->
          <div v-else-if="ultimasVentas.length === 0" class="card-body text-center py-5">
            <div style="font-size:2.5rem">📋</div>
            <p class="text-muted mt-2 mb-0 small">No hay ventas registradas aún</p>
            <router-link :to="{ name: 'VentaEditor' }" class="btn btn-primary btn-sm mt-3">
              Registrar primera venta
            </router-link>
          </div>

          <!-- Tabla -->
          <div v-else class="table-responsive">
            <table class="table mb-0">
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
                  <td class="text-muted">{{ v.id }}</td>
                  <td>{{ formatFecha(v.fecha) }}</td>
                  <td class="fw-semibold">{{ v.cliente?.nombre }} {{ v.cliente?.apellido }}</td>
                  <td class="fw-semibold" style="color:var(--color-primary)">$ {{ formatPrecio(v.total) }}</td>
                  <td>
                    <span class="badge" :class="badgeVenta(v.estadoVenta)">{{ v.estadoVenta ?? '-' }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

        </div>
      </div>

    </div>

  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import VentaService from '@/entities/venta/venta.service';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import ClienteService from '@/entities/cliente/cliente.service';
import InventarioService from '@/entities/inventario/inventario.service';
import type { IVenta } from '@/shared/model/venta.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';

const ventaService    = new VentaService();
const vehiculoService = new VehiculoService();
const clienteService  = new ClienteService();
const inventarioService = new InventarioService();

const loadingKpis   = ref(true);
const loadingVentas = ref(true);
const ultimasVentas = ref<IVenta[]>([]);

const kpis = ref({ ventasMes: 0, vehiculosDisponibles: 0, clientesActivos: 0, inventarioTotal: 0 });

const fechaHoy = new Date().toLocaleDateString('es-AR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });

onMounted(async () => {
  await Promise.allSettled([cargarKpis(), cargarUltimasVentas()]);
});

async function cargarKpis() {
  loadingKpis.value = true;
  try {
    const [ventas, vehiculos, clientes, inventario] = await Promise.allSettled([
      ventaService.retrieve({ page: 0, size: 1 }),
      vehiculoService.retrieve({ page: 0, size: 1 }),
      clienteService.retrieve({ page: 0, size: 1 }),
      inventarioService.retrieve({ page: 0, size: 1 }),
    ]);

    kpis.value.ventasMes           = ventas.status === 'fulfilled'     ? Number(ventas.value.headers?.['x-total-count'] ?? 0) : 0;
    kpis.value.vehiculosDisponibles = vehiculos.status === 'fulfilled'  ? Number(vehiculos.value.headers?.['x-total-count'] ?? 0) : 0;
    kpis.value.clientesActivos      = clientes.status === 'fulfilled'   ? Number(clientes.value.headers?.['x-total-count'] ?? 0) : 0;
    kpis.value.inventarioTotal      = inventario.status === 'fulfilled' ? Number(inventario.value.headers?.['x-total-count'] ?? 0) : 0;
  } finally {
    loadingKpis.value = false;
  }
}

async function cargarUltimasVentas() {
  loadingVentas.value = true;
  try {
    const res = await ventaService.retrieve({ page: 0, size: 5, sort: ['fecha,desc'] });
    ultimasVentas.value = res.data;
  } catch {
    ultimasVentas.value = [];
  } finally {
    loadingVentas.value = false;
  }
}

function badgeVenta(estado?: EstadoVenta | null) {
  const map: Record<string, string> = {
    [EstadoVenta.PENDIENTE]: 'bg-warning text-dark',
    [EstadoVenta.PAGADA]:    'bg-success',
    [EstadoVenta.CANCELADA]: 'bg-danger',
  };
  return map[estado ?? ''] ?? 'bg-light text-dark border';
}

function formatFecha(f?: Date | string) {
  return f ? new Date(f).toLocaleDateString('es-AR') : '-';
}

function formatPrecio(p?: number | null) {
  return Number(p ?? 0).toLocaleString('es-AR');
}
</script>

<style scoped>
.quick-action-item {
  display: flex;
  align-items: center;
  gap: .85rem;
  padding: .9rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
  text-decoration: none;
  color: var(--color-text);
  transition: background .12s;
  cursor: pointer;
}
.quick-action-item:hover { background: #f8fafc; }

.quick-action-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  flex-shrink: 0;
}
.quick-action-title { font-size: .85rem; font-weight: 600; }
.quick-action-sub   { font-size: .75rem; color: var(--color-text-muted); }
</style>
