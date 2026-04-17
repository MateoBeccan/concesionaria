<template>
  <div class="dashboard-page">
    <section class="hero-card">
      <div>
        <p class="hero-eyebrow">{{ isAdmin ? 'Administracion' : 'Operacion diaria' }}</p>
        <h1 class="hero-title">{{ isAdmin ? 'Panel de gestion de concesionaria' : 'Panel operativo de ventas' }}</h1>
        <p class="hero-subtitle">
          {{ isAdmin ? 'Supervisa catalogo, inventario, ventas y configuracion clave del negocio.' : 'Trabaja mas rapido con accesos directos a ventas, clientes y vehiculos disponibles.' }}
        </p>
      </div>

      <div class="hero-meta">
        <div class="hero-date">{{ fechaHoy }}</div>
        <div class="hero-user">{{ username }}</div>
      </div>
    </section>

    <section class="kpi-grid">
      <article v-for="kpi in kpiCards" :key="kpi.label" class="kpi-card" :class="`tone-${kpi.tone}`">
        <div class="kpi-header">
          <div class="kpi-label">{{ kpi.label }}</div>
          <div class="kpi-badge">{{ kpi.badge }}</div>
        </div>
        <div class="kpi-value">{{ loading ? '...' : kpi.value }}</div>
        <div class="kpi-sub">{{ kpi.sub }}</div>
      </article>
    </section>

    <section class="content-grid summary-grid">
      <article class="card overview-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">{{ isAdmin ? 'Resumen ejecutivo' : 'Resumen operativo' }}</p>
            <h2 class="card-title">{{ isAdmin ? 'Estado del negocio hoy' : 'Foco del dia' }}</h2>
          </div>
        </div>

        <div v-if="loading" class="empty-state">Preparando resumen...</div>

        <div v-else class="overview-layout">
          <div class="overview-main">
            <span class="overview-label">{{ isAdmin ? 'Resultado visible' : 'Operaciones cerradas hoy' }}</span>
            <strong class="overview-value">{{ isAdmin ? formatCurrency(kpiData.totalIngresos) : String(kpiData.salesToday) }}</strong>
            <p class="overview-copy">
              {{
                isAdmin
                  ? `${kpiData.totalVentas} ventas registradas y ${kpiData.soldInventory} unidades ya comercializadas.`
                  : `${kpiData.pendingSales} ventas pendientes y ${kpiData.availableInventory} unidades listas para ofrecer.`
              }}
            </p>
          </div>

          <div class="overview-points">
            <div v-for="point in overviewPoints" :key="point.label" class="overview-point">
              <span>{{ point.label }}</span>
              <strong>{{ point.value }}</strong>
              <small>{{ point.copy }}</small>
            </div>
          </div>
        </div>
      </article>

      <article class="card pulse-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">{{ isAdmin ? 'Control' : 'Seguimiento' }}</p>
            <h2 class="card-title">{{ isAdmin ? 'Pulso operativo' : 'Seguimiento del flujo' }}</h2>
          </div>
        </div>

        <div v-if="loading" class="empty-state">Cargando indicadores...</div>

        <div v-else class="pulse-list">
          <div v-for="metric in pulseMetrics" :key="metric.label" class="pulse-item">
            <div class="pulse-copy">
              <span class="pulse-label">{{ metric.label }}</span>
              <small class="pulse-sub">{{ metric.copy }}</small>
            </div>
            <strong class="pulse-value">{{ metric.value }}</strong>
          </div>
        </div>
      </article>
    </section>

    <section class="content-grid">
      <article class="card quick-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">{{ isAdmin ? 'Acciones de gestion' : 'Acciones rapidas' }}</p>
            <h2 class="card-title">{{ isAdmin ? 'Atajos administrativos' : 'Atajos operativos' }}</h2>
          </div>
        </div>

        <div class="quick-actions">
          <router-link v-for="action in quickActions" :key="action.label" :to="{ name: action.routeName }" class="quick-action">
            <div class="quick-icon">{{ action.icon }}</div>
            <div>
              <div class="quick-label">{{ action.label }}</div>
              <div class="quick-copy">{{ action.description }}</div>
            </div>
          </router-link>
        </div>
      </article>

      <article class="card highlight-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">{{ isAdmin ? 'Atencion' : 'Seguimiento' }}</p>
            <h2 class="card-title">{{ isAdmin ? 'Alertas y control' : 'Clientes recientes' }}</h2>
          </div>
        </div>

        <div v-if="loading" class="empty-state">Cargando informacion...</div>

        <div v-else-if="isAdmin" class="status-list">
          <div class="status-item">
            <span>Reservas vencidas</span>
            <strong>{{ adminAlerts.expiredReservations }}</strong>
          </div>
          <div class="status-item">
            <span>Inventario sin ubicacion</span>
            <strong>{{ adminAlerts.withoutLocation }}</strong>
          </div>
          <div class="status-item">
            <span>Ventas pendientes</span>
            <strong>{{ kpiData.pendingSales }}</strong>
          </div>
          <div class="status-item">
            <span>Cotizacion activa</span>
            <strong>{{ latestCotizacionLabel }}</strong>
          </div>
        </div>

        <div v-else-if="recentClients.length" class="mini-list">
          <router-link v-for="cliente in recentClients" :key="cliente.id" :to="{ name: 'ClienteView', params: { clienteId: String(cliente.id) } }" class="mini-row">
            <div class="mini-main">
              <span class="mini-title">{{ clientFullName(cliente) }}</span>
              <span class="mini-copy">{{ cliente.email || cliente.telefono || 'Sin contacto principal' }}</span>
            </div>
            <span class="mini-meta">{{ formatDate(cliente.fechaAlta ?? cliente.createdDate) }}</span>
          </router-link>
        </div>

        <div v-else class="empty-state">{{ isAdmin ? 'No hay alertas para mostrar.' : 'No hay clientes recientes.' }}</div>
      </article>
    </section>

    <section class="content-grid secondary-grid">
      <article class="card table-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">{{ isAdmin ? 'Ventas' : 'Actividad' }}</p>
            <h2 class="card-title">{{ isAdmin ? 'Ultimas ventas' : 'Ventas del flujo operativo' }}</h2>
          </div>

          <router-link :to="{ name: 'VentaList' }" class="btn btn-outline btn-sm">Ver ventas</router-link>
        </div>

        <div v-if="loading" class="empty-state">Cargando ventas...</div>

        <div v-else-if="latestVentas.length === 0" class="empty-state">No hay ventas registradas.</div>

        <div v-else class="table-shell">
          <table class="table dashboard-table">
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
              <tr v-for="venta in latestVentas" :key="venta.id">
                <td>{{ venta.id }}</td>
                <td>{{ formatDate(venta.fecha) }}</td>
                <td>{{ venta.cliente ? clientFullName(venta.cliente) : 'Sin cliente' }}</td>
                <td class="amount">{{ formatCurrency(venta.total) }}</td>
                <td>
                  <span class="status-pill" :class="statusClass(venta.estado)">
                    {{ venta.estado ?? 'SIN ESTADO' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="card side-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">{{ isAdmin ? 'Catalogo tecnico' : 'Disponibilidad' }}</p>
            <h2 class="card-title">{{ isAdmin ? 'Atajos de mantenimiento' : 'Unidades disponibles' }}</h2>
          </div>
        </div>

        <div v-if="loading" class="empty-state">Cargando informacion...</div>

        <div v-else-if="isAdmin" class="mini-list">
          <router-link v-for="item in adminCatalogLinks" :key="item.label" :to="{ name: item.routeName }" class="mini-row">
            <div class="mini-main">
              <span class="mini-title">{{ item.label }}</span>
              <span class="mini-copy">{{ item.description }}</span>
            </div>
          </router-link>
        </div>

        <div v-else class="status-list">
          <div class="status-item">
            <span>Vehiculos disponibles</span>
            <strong>{{ userAvailability.available }}</strong>
          </div>
          <div class="status-item">
            <span>Vehiculos reservados</span>
            <strong>{{ userAvailability.reserved }}</strong>
          </div>
          <div class="status-item">
            <span>Clientes activos</span>
            <strong>{{ kpiData.activeClients }}</strong>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';

import ClienteService from '@/entities/cliente/cliente.service';
import CotizacionService from '@/entities/cotizacion/cotizacion.service';
import InventarioService from '@/entities/inventario/inventario.service';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import VentaService from '@/entities/venta/venta.service';
import { useStore } from '@/store';
import type { ICliente } from '@/shared/model/cliente.model';
import type { ICotizacion } from '@/shared/model/cotizacion.model';
import type { IInventario } from '@/shared/model/inventario.model';
import type { IVenta } from '@/shared/model/venta.model';

interface QuickAction {
  routeName: string;
  label: string;
  description: string;
  icon: string;
}

interface KpiCard {
  label: string;
  value: string;
  sub: string;
  tone: 'finance' | 'inventory' | 'warning' | 'success';
  badge: string;
}

const ventaService = new VentaService();
const vehiculoService = new VehiculoService();
const clienteService = new ClienteService();
const inventarioService = new InventarioService();
const cotizacionService = new CotizacionService();
const store = useStore();

const loading = ref(true);
const latestVentas = ref<IVenta[]>([]);
const recentClients = ref<ICliente[]>([]);
const inventarioItems = ref<IInventario[]>([]);
const cotizaciones = ref<ICotizacion[]>([]);

const kpiData = ref({
  totalVentas: 0,
  totalIngresos: 0,
  salesToday: 0,
  pendingSales: 0,
  reservedSales: 0,
  paidSales: 0,
  availableInventory: 0,
  reservedInventory: 0,
  soldInventory: 0,
  activeClients: 0,
  vehiclesCount: 0,
});

const username = computed(() => store.account?.login ?? 'Usuario');
const isAdmin = computed(() => store.account?.authorities?.includes('ROLE_ADMIN') ?? false);

const fechaHoy = new Date().toLocaleDateString('es-AR', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
});

const kpiCards = computed<KpiCard[]>(() =>
  isAdmin.value
    ? [
        { label: 'Ingresos', value: formatCurrency(kpiData.value.totalIngresos), sub: 'suma de ventas visibles', tone: 'finance', badge: 'Hoy' },
        { label: 'Disponible', value: String(kpiData.value.availableInventory), sub: 'inventario listo para vender', tone: 'inventory', badge: 'Stock' },
        { label: 'Reservado', value: String(kpiData.value.reservedInventory), sub: 'unidades con reserva activa', tone: 'warning', badge: 'Seguimiento' },
        { label: 'Vendido', value: String(kpiData.value.soldInventory), sub: 'stock ya comercializado', tone: 'success', badge: 'Resultado' },
      ]
    : [
        { label: 'Ventas del dia', value: String(kpiData.value.salesToday), sub: 'operaciones de hoy', tone: 'finance', badge: 'Hoy' },
        { label: 'Pendientes', value: String(kpiData.value.pendingSales), sub: 'operaciones para continuar', tone: 'warning', badge: 'Prioridad' },
        { label: 'Reservadas', value: String(kpiData.value.reservedSales), sub: 'ventas con seguimiento activo', tone: 'inventory', badge: 'Seguimiento' },
        { label: 'Vehiculos disponibles', value: String(kpiData.value.availableInventory), sub: 'listos para ofrecer', tone: 'success', badge: 'Oferta' },
      ],
);

const overviewPoints = computed(() =>
  isAdmin.value
    ? [
        { label: 'Inventario listo', value: String(kpiData.value.availableInventory), copy: 'unidades disponibles para vender' },
        { label: 'Reservas por controlar', value: String(kpiData.value.reservedInventory), copy: 'requieren seguimiento comercial' },
        { label: 'Clientes activos', value: String(kpiData.value.activeClients), copy: 'base comercial actual' },
      ]
    : [
        { label: 'Unidades disponibles', value: String(kpiData.value.availableInventory), copy: 'para ofrecer hoy' },
        { label: 'Ventas pagadas', value: String(kpiData.value.paidSales), copy: 'operaciones ya cerradas' },
        { label: 'Clientes recientes', value: String(recentClients.value.length), copy: 'altas y consultas nuevas' },
      ],
);

const pulseMetrics = computed(() =>
  isAdmin.value
    ? [
        { label: 'Reservas vencidas', value: String(adminAlerts.value.expiredReservations), copy: 'necesitan revision o liberacion' },
        { label: 'Inventario sin ubicacion', value: String(adminAlerts.value.withoutLocation), copy: 'afecta control operativo' },
        { label: 'Ventas pendientes', value: String(kpiData.value.pendingSales), copy: 'operaciones sin cierre definitivo' },
        { label: 'Cotizacion activa', value: latestCotizacionLabel.value, copy: 'referencia comercial vigente' },
      ]
    : [
        { label: 'Pendientes por seguir', value: String(kpiData.value.pendingSales), copy: 'ventas para retomar hoy' },
        { label: 'Reservadas', value: String(kpiData.value.reservedSales), copy: 'operaciones con seguimiento activo' },
        { label: 'Clientes activos', value: String(kpiData.value.activeClients), copy: 'base disponible para trabajar' },
        { label: 'Disponibles', value: String(kpiData.value.availableInventory), copy: 'unidades listas para ofrecer' },
      ],
);

const quickActions = computed<QuickAction[]>(() =>
  isAdmin.value
    ? [
        { routeName: 'VehiculoCreate', label: 'Nuevo vehiculo', description: 'Carga unidades y completa el catalogo comercial.', icon: 'VH' },
        { routeName: 'Inventario', label: 'Controlar inventario', description: 'Revisa stock, reservas y vencimientos.', icon: 'IN' },
        { routeName: 'VersionCompatibilityAdmin', label: 'Compatibilidades', description: 'Relaciona versiones y motores del catalogo.', icon: 'CM' },
        { routeName: 'Cotizacion', label: 'Actualizar cotizaciones', description: 'Mantiene los valores vigentes por moneda.', icon: 'CZ' },
      ]
    : [
        { routeName: 'VentaEditor', label: 'Nueva venta', description: 'Inicia una operacion completa con cliente, vehiculos y pagos.', icon: 'NV' },
        { routeName: 'VehiculoSearch', label: 'Buscar vehiculo', description: 'Encuentra unidades disponibles para vender.', icon: 'BV' },
        { routeName: 'ClienteCreate', label: 'Nuevo cliente', description: 'Registra clientes sin salir del flujo comercial.', icon: 'NC' },
        { routeName: 'VentaList', label: 'Ver ventas', description: 'Consulta operaciones recientes y pendientes.', icon: 'VT' },
      ],
);

const adminCatalogLinks = [
  { routeName: 'Marca', label: 'Marcas', description: 'Gestiona marcas activas del catalogo.' },
  { routeName: 'Modelo', label: 'Modelos', description: 'Organiza modelos por marca.' },
  { routeName: 'Version', label: 'Versiones', description: 'Define versiones comerciales disponibles.' },
  { routeName: 'Motor', label: 'Motores', description: 'Administra motorizaciones y especificaciones.' },
  { routeName: 'VersionCompatibilityAdmin', label: 'Compatibilidades', description: 'Vincula motores validos por version.' },
  { routeName: 'MetodoPago', label: 'Metodos de pago', description: 'Configura medios de cobro habilitados.' },
  { routeName: 'Cotizacion', label: 'Cotizaciones', description: 'Mantiene valores de moneda y referencia comercial.' },
  { routeName: 'JhiUser', label: 'Usuarios', description: 'Administra accesos y permisos del sistema.' },
];

const adminAlerts = computed(() => {
  const today = startOfDay(new Date());

  return {
    expiredReservations: inventarioItems.value.filter(item => {
      if (item.estadoInventario !== 'RESERVADO' || !item.fechaVencimientoReserva) return false;
      return startOfDay(item.fechaVencimientoReserva) < today;
    }).length,
    withoutLocation: inventarioItems.value.filter(item => !String(item.ubicacion ?? '').trim()).length,
  };
});

const latestCotizacionLabel = computed(() => {
  const current = cotizaciones.value.find(item => item.activo) ?? cotizaciones.value[0];
  if (!current) return 'Sin cotizacion';
  const moneda = current.moneda?.descripcion ?? current.moneda?.codigo ?? 'Moneda';
  return `${moneda} ${formatCurrency(current.valorVenta)}`;
});

const userAvailability = computed(() => ({
  available: kpiData.value.availableInventory,
  reserved: kpiData.value.reservedInventory,
}));

onMounted(async () => {
  await loadDashboard();
});

async function loadDashboard() {
  loading.value = true;

  try {
    const [ventasRes, vehiculosRes, clientesRes, inventarioRes, cotizacionesRes] = await Promise.all([
      ventaService.retrieve({ page: 0, size: 200, sort: ['fecha,desc'] }),
      vehiculoService.retrieve({ page: 0, size: 200, sort: ['id,desc'] }),
      clienteService.retrieve({ page: 0, size: 8, sort: ['id,desc'] }),
      inventarioService.retrieve({ page: 0, size: 200, sort: ['id,desc'] }),
      isAdmin.value ? cotizacionService.retrieve({ page: 0, size: 10, sort: ['fecha,desc'] }) : Promise.resolve({ data: [] }),
    ]);

    const ventas = ventasRes.data ?? [];
    const clientes = clientesRes.data ?? [];
    const inventario = inventarioRes.data ?? [];

    latestVentas.value = ventas.slice(0, 6);
    recentClients.value = clientes.slice(0, 5);
    inventarioItems.value = inventario;
    cotizaciones.value = isAdmin.value ? cotizacionesRes.data ?? [] : [];

    kpiData.value.totalVentas = Number(ventasRes.headers['x-total-count'] ?? ventas.length);
    kpiData.value.totalIngresos = ventas.reduce((sum, venta) => sum + Number(venta.total ?? 0), 0);
    kpiData.value.salesToday = ventas.filter(venta => sameDay(venta.fecha, new Date())).length;
    kpiData.value.pendingSales = ventas.filter(venta => venta.estado === 'PENDIENTE').length;
    kpiData.value.reservedSales = ventas.filter(venta => venta.estado === 'RESERVADA').length;
    kpiData.value.paidSales = ventas.filter(venta => venta.estado === 'PAGADA').length;
    kpiData.value.activeClients = Number(clientesRes.headers['x-total-count'] ?? clientes.length);
    kpiData.value.vehiclesCount = Number(vehiculosRes.headers['x-total-count'] ?? 0);
    kpiData.value.availableInventory = inventario.filter(item => item.estadoInventario === 'DISPONIBLE').length;
    kpiData.value.reservedInventory = inventario.filter(item => item.estadoInventario === 'RESERVADO').length;
    kpiData.value.soldInventory = inventario.filter(item => item.estadoInventario === 'VENDIDO').length;
  } catch (error) {
    console.error('Error cargando dashboard', error);
  } finally {
    loading.value = false;
  }
}

function clientFullName(cliente?: Pick<ICliente, 'nombre' | 'apellido'> | null) {
  const fullName = `${cliente?.nombre ?? ''} ${cliente?.apellido ?? ''}`.trim();
  return fullName || 'Cliente sin nombre';
}

function formatDate(value?: Date | string | null) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString('es-AR');
}

function formatCurrency(value?: number | null) {
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(Number(value ?? 0));
}

function statusClass(status?: string | null) {
  return {
    pending: status === 'PENDIENTE',
    done: status === 'PAGADA',
    canceled: status === 'CANCELADA',
  };
}

function sameDay(value?: Date | string | null, compareDate = new Date()) {
  if (!value) return false;
  const date = new Date(value);
  return date.toDateString() === compareDate.toDateString();
}

function startOfDay(value: Date | string) {
  const date = new Date(value);
  date.setHours(0, 0, 0, 0);
  return date.getTime();
}
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 1.2rem;
  padding: 1.5rem;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(14, 165, 233, 0.22), transparent 30%),
    linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);
  color: #eff6ff;
}

.hero-eyebrow {
  margin: 0 0 0.35rem;
  font-size: 0.74rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #bae6fd;
}

.hero-title {
  margin: 0;
  font-size: 1.55rem;
  font-weight: 700;
}

.hero-subtitle {
  max-width: 760px;
  margin: 0.55rem 0 0;
  color: #dbeafe;
}

.hero-meta {
  min-width: 200px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  gap: 0.9rem;
  text-align: right;
}

.hero-date {
  font-size: 0.85rem;
  color: #cbd5e1;
}

.hero-user {
  padding: 0.55rem 0.8rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 0.84rem;
  font-weight: 600;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1rem;
}

.kpi-card,
.card {
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.kpi-card {
  position: relative;
  padding: 1.1rem 1.2rem;
  overflow: hidden;
}

.kpi-card::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  border-radius: 20px 0 0 20px;
  background: #cbd5e1;
}

.tone-finance::before {
  background: #0f766e;
}

.tone-inventory::before {
  background: #2563eb;
}

.tone-warning::before {
  background: #d97706;
}

.tone-success::before {
  background: #16a34a;
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: center;
}

.kpi-badge {
  padding: 0.2rem 0.45rem;
  border-radius: 999px;
  background: #f1f5f9;
  font-size: 0.68rem;
  font-weight: 700;
  color: #475569;
}

.kpi-label {
  font-size: 0.78rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #64748b;
}

.kpi-value {
  margin-top: 0.55rem;
  font-size: 1.65rem;
  font-weight: 700;
  color: #0f172a;
}

.kpi-sub {
  margin-top: 0.3rem;
  font-size: 0.85rem;
  color: #64748b;
}

.content-grid {
  display: grid;
  grid-template-columns: 1.35fr 1fr;
  gap: 1rem;
}

.summary-grid {
  grid-template-columns: 1.3fr 0.95fr;
}

.secondary-grid {
  grid-template-columns: 1.6fr 0.9fr;
}

.card {
  padding: 1.2rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.card-eyebrow {
  margin: 0 0 0.28rem;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #0284c7;
}

.card-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: #0f172a;
}

.overview-layout {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 1rem;
}

.overview-main {
  padding: 1.1rem;
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, rgba(14, 165, 233, 0.18), transparent 30%),
    linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: #eff6ff;
}

.overview-label {
  display: block;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #bae6fd;
}

.overview-value {
  display: block;
  margin-top: 0.55rem;
  font-size: 1.8rem;
  font-weight: 700;
}

.overview-copy {
  margin: 0.55rem 0 0;
  color: #cbd5e1;
}

.overview-points {
  display: grid;
  gap: 0.75rem;
}

.overview-point,
.pulse-item {
  padding: 0.95rem 1rem;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.overview-point span,
.pulse-label {
  display: block;
  color: #475569;
}

.overview-point strong,
.pulse-value {
  display: block;
  margin-top: 0.25rem;
  font-size: 1.08rem;
  color: #0f172a;
}

.overview-point small,
.pulse-sub {
  display: block;
  margin-top: 0.18rem;
  color: #64748b;
}

.pulse-list {
  display: grid;
  gap: 0.75rem;
}

.pulse-item {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}

.pulse-copy {
  min-width: 0;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.8rem;
}

.quick-action,
.mini-row {
  display: flex;
  gap: 0.8rem;
  align-items: flex-start;
  padding: 0.85rem 0.95rem;
  border-radius: 16px;
  text-decoration: none;
  color: inherit;
  background: #f8fafc;
  border: 1px solid transparent;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.quick-action:hover,
.mini-row:hover {
  transform: translateY(-1px);
  border-color: #bfdbfe;
  background: #f0f9ff;
}

.quick-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: grid;
  place-content: center;
  background: linear-gradient(135deg, #dbeafe, #e0f2fe);
  color: #1d4ed8;
  font-size: 0.74rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  flex-shrink: 0;
}

.quick-label,
.mini-title {
  display: block;
  font-size: 0.92rem;
  font-weight: 600;
  color: #0f172a;
}

.quick-copy,
.mini-copy,
.mini-meta {
  display: block;
  margin-top: 0.18rem;
  font-size: 0.82rem;
  color: #64748b;
}

.mini-list {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.mini-main {
  min-width: 0;
}

.mini-row {
  justify-content: space-between;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 0.95rem 1rem;
  border-radius: 16px;
  background: #f8fafc;
}

.status-item span {
  color: #475569;
}

.status-item strong {
  font-size: 1rem;
  color: #0f172a;
}

.empty-state {
  padding: 2.25rem 1rem;
  border-radius: 16px;
  background: #f8fafc;
  text-align: center;
  color: #64748b;
}

.table-shell {
  overflow-x: auto;
}

.dashboard-table th {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #64748b;
}

.dashboard-table td {
  vertical-align: middle;
}

.amount {
  font-weight: 700;
  color: #0f766e;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 92px;
  padding: 0.32rem 0.65rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  background: #e2e8f0;
  color: #334155;
}

.status-pill.pending {
  background: #fef3c7;
  color: #92400e;
}

.status-pill.done {
  background: #dcfce7;
  color: #166534;
}

.status-pill.canceled {
  background: #fee2e2;
  color: #991b1b;
}

@media (max-width: 1200px) {
  .content-grid,
  .secondary-grid,
  .summary-grid,
  .kpi-grid,
  .overview-layout {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 840px) {
  .hero-card,
  .content-grid,
  .secondary-grid,
  .summary-grid,
  .kpi-grid,
  .quick-actions,
  .overview-layout {
    grid-template-columns: 1fr;
  }

  .hero-card {
    flex-direction: column;
  }

  .hero-meta {
    align-items: flex-start;
    text-align: left;
  }
}
</style>
