<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-semibold mb-0">{{ isAdmin ? 'Reservas globales' : 'Mis reservas' }}</h4>
        <p class="text-muted small mb-0" v-if="!loading">
          {{ total }} {{ total === 1 ? 'reserva' : 'reservas' }}
        </p>
      </div>
      <button class="btn btn-outline-warning" @click="expirarVencidas" :disabled="loadingExpirar || loading">
        <span v-if="loadingExpirar" class="spinner-border spinner-border-sm me-2" />
        Expirar vencidas
      </button>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <input
              v-model="search"
              type="text"
              class="form-control"
              placeholder="Buscar por cliente, patente o venta"
            />
          </div>
          <div class="col-md-3">
            <select v-model="estadoFiltro" class="form-select">
              <option value="">Todos los estados</option>
              <option value="ACTIVA">Activa</option>
              <option value="VENCIDA">Vencida</option>
              <option value="CANCELADA">Cancelada</option>
              <option value="CONVERTIDA">Convertida</option>
            </select>
          </div>
          <div class="col-md-2 d-grid">
            <button class="btn btn-outline-secondary" @click="resetFiltros">Limpiar</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-4">
      <span class="spinner-border" />
    </div>

    <div v-else-if="error" class="alert alert-danger d-flex justify-content-between align-items-center">
      <span>{{ error }}</span>
      <button class="btn btn-sm btn-outline-danger" @click="cargar">Reintentar</button>
    </div>

    <div v-else-if="filteredReservas.length === 0" class="alert alert-warning mb-0">
      {{ isAdmin ? 'No hay reservas para mostrar.' : 'Todavia no tenes reservas para mostrar.' }}
    </div>

    <div v-else class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th>#</th>
              <th>Cliente</th>
              <th>Vehiculo</th>
              <th>Seña</th>
              <th>Vencimiento</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="reserva in filteredReservas" :key="reserva.id">
              <td>{{ reserva.id }}</td>
              <td>{{ clienteLabel(reserva) }}</td>
              <td>{{ vehiculoLabel(reserva) }}</td>
              <td>{{ montoLabel(reserva) }}</td>
              <td>{{ formatDate(reserva.fechaVencimiento) }}</td>
              <td>
                <span class="badge" :class="estadoBadge(reserva.estado)">
                  {{ reserva.estado }}
                </span>
              </td>
              <td class="text-end">
                <router-link :to="{ name: 'ReservaView', params: { reservaId: reserva.id } }" class="btn btn-sm btn-outline-secondary">
                  Ver
                </router-link>
                <router-link
                  v-if="reserva.estado === 'ACTIVA'"
                  :to="{
                    name: 'VentaEditor',
                    query: {
                      reservaId: reserva.id,
                      inventarioId: reserva.inventario?.id ?? undefined,
                      vehiculoId: reserva.inventario?.vehiculo?.id ?? undefined,
                      clienteId: reserva.cliente?.id ?? undefined,
                    },
                  }"
                  class="btn btn-sm btn-success ms-2"
                >
                  Convertir
                </router-link>
                <button
                  v-if="reserva.estado === 'ACTIVA'"
                  class="btn btn-sm btn-outline-danger ms-2"
                  @click="cancelar(reserva.id)"
                >
                  Cancelar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useAlertService } from '@/shared/alert/alert.service';
import type { IInventario } from '@/shared/model/inventario.model';
import type { IReserva } from '@/shared/model/reserva.model';
import { useStore } from '@/store';
import { Authority } from '@/shared/jhipster/constants';
import InventarioService from '@/entities/inventario/inventario.service';
import ReservaService from './reserva.service';

const reservaService = new ReservaService();
const inventarioService = new InventarioService();
const alertService = useAlertService();
const route = useRoute();
const store = useStore();

const reservas = ref<IReserva[]>([]);
const total = ref(0);
const loading = ref(false);
const loadingExpirar = ref(false);
const error = ref<string | null>(null);
const search = ref('');
const estadoFiltro = ref('');
const isAdmin = computed(() => (store.account?.authorities ?? []).includes(Authority.ADMIN));

const filteredReservas = computed(() => {
  const q = search.value.trim().toLowerCase();
  return reservas.value.filter(r => {
    const matchEstado = !estadoFiltro.value || r.estado === estadoFiltro.value;
    const text = [
      r.cliente?.nombre ?? '',
      r.cliente?.apellido ?? '',
      r.inventario?.vehiculo?.patente ?? '',
      r.ventaAsociada?.id?.toString() ?? '',
    ]
      .join(' ')
      .toLowerCase();
    const matchSearch = !q || text.includes(q);
    return matchEstado && matchSearch;
  });
});

onMounted(cargar);
onMounted(() => {
  if (typeof route.query.q === 'string') {
    search.value = route.query.q;
  }
  if (typeof route.query.estado === 'string') {
    estadoFiltro.value = route.query.estado;
  }
});

async function cargar() {
  loading.value = true;
  error.value = null;
  try {
    const res = await reservaService.retrieve({ page: 0, size: 100, sort: ['fechaReserva,desc'] });
    reservas.value = await enriquecerReservas(res.data);
    total.value = Number(res.headers?.['x-total-count'] ?? res.data.length);
  } catch (e: any) {
    error.value = 'Error al cargar reservas';
    alertService.showHttpError(e.response);
  } finally {
    loading.value = false;
  }
}

function resetFiltros() {
  search.value = '';
  estadoFiltro.value = '';
}

function formatDate(value?: Date | string) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString('es-AR');
}

function formatImporte(value?: number | null) {
  return Number(value).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function clienteLabel(reserva: IReserva) {
  const cliente = reserva.cliente;
  if (!cliente) return '-';
  return [cliente.nombre, cliente.apellido].filter(Boolean).join(' ') || `Cliente #${cliente.id ?? ''}`.trim();
}

function vehiculoLabel(reserva: IReserva) {
  const vehiculo = reserva.inventario?.vehiculo;
  if (!vehiculo) return '-';
  return vehiculo.patente || `Vehiculo #${vehiculo.id ?? ''}`.trim();
}

async function enriquecerReservas(data: IReserva[]): Promise<IReserva[]> {
  return Promise.all(data.map(async reserva => enriquecerReserva(reserva)));
}

async function enriquecerReserva(reserva: IReserva): Promise<IReserva> {
  let enriched = reserva;

  const inventarioId = reserva.inventario?.id;
  if (inventarioId && !reserva.inventario?.vehiculo) {
    try {
      const inventario: IInventario = await inventarioService.find(inventarioId);
      if (inventario?.vehiculo) {
        enriched = {
          ...enriched,
          inventario: {
            ...enriched.inventario,
            vehiculo: inventario.vehiculo,
          },
        };
      }
    } catch {
      // noop
    }
  }

  if (!enriched.ventaAsociada?.id && enriched.id) {
    try {
      const venta = await reservaService.ventaByReserva(enriched.id);
      if (venta?.id) {
        enriched = {
          ...enriched,
          ventaAsociada: {
            id: venta.id,
            estado: venta.estado ?? null,
          },
        };
      }
    } catch {
      // noop
    }
  }

  const montoActual = safeNumber(enriched.montoSenia);
  if (enriched.id && montoActual <= 0) {
    try {
      const pagos = await reservaService.pagosByReserva(enriched.id);
      const montoReal = pagos.filter(p => p.estado !== 'ANULADO').reduce((acc, p) => acc + safeNumber(p.monto), 0);
      if (montoReal > 0) {
        enriched = {
          ...enriched,
          montoSenia: montoReal,
        };
      }
    } catch {
      // noop
    }
  }

  return enriched;
}

function monedaLabel(reserva: IReserva) {
  return reserva.moneda?.codigo || '$';
}

function montoLabel(reserva: IReserva) {
  if (reserva.montoSenia === null || reserva.montoSenia === undefined) return '-';
  return `${monedaLabel(reserva)} ${formatImporte(safeNumber(reserva.montoSenia))}`;
}

function safeNumber(value: unknown): number {
  const parsed = Number(value ?? 0);
  return Number.isFinite(parsed) ? parsed : 0;
}

function estadoBadge(estado?: string) {
  if (estado === 'ACTIVA') return 'bg-warning text-dark';
  if (estado === 'CONVERTIDA') return 'bg-success';
  if (estado === 'CANCELADA') return 'bg-danger';
  if (estado === 'VENCIDA') return 'bg-secondary';
  return 'bg-light text-dark border';
}

async function cancelar(id?: number) {
  if (!id) return;
  try {
    await reservaService.cancelar(id, 'Cancelada desde gestion de reservas');
    alertService.showInfo(`Reserva ${id} cancelada`);
    await cargar();
  } catch (e: any) {
    alertService.showHttpError(e.response);
  }
}

async function expirarVencidas() {
  loadingExpirar.value = true;
  try {
    const result = await reservaService.expirarVencidas();
    alertService.showInfo(`Reservas expiradas: ${result.reservasExpiradas ?? 0}`);
    await cargar();
  } catch (e: any) {
    alertService.showHttpError(e.response);
  } finally {
    loadingExpirar.value = false;
  }
}
</script>
