<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-semibold mb-0">{{ isAdmin ? 'Ventas globales' : 'Mis ventas' }}</h4>
        <p class="text-muted small mb-0" v-if="!loading">
          {{ total }} {{ total === 1 ? 'registro encontrado' : 'registros encontrados' }}
        </p>
      </div>
      <router-link :to="{ name: 'VentaEditor' }" class="btn btn-primary"> + Nueva venta </router-link>
    </div>

    <div v-if="loading">
      <div v-for="i in 5" :key="i" class="card border-0 shadow-sm mb-2 p-3">
        <div class="placeholder-glow d-flex gap-3">
          <span class="placeholder col-1 rounded" />
          <span class="placeholder col-2 rounded" />
          <span class="placeholder col-3 rounded" />
          <span class="placeholder col-2 rounded" />
          <span class="placeholder col-2 rounded" />
        </div>
      </div>
    </div>

    <div v-else-if="error" class="alert alert-danger d-flex justify-content-between align-items-center">
      <span>{{ error }}</span>
      <button class="btn btn-sm btn-outline-danger" @click="cargar">Reintentar</button>
    </div>

    <div v-else-if="ventas.length === 0" class="text-center py-5">
      <div style="font-size: 3rem">📋</div>
      <h5 class="mt-3 text-muted">{{ isAdmin ? 'No hay ventas registradas' : 'Todavia no registraste ventas' }}</h5>
      <p class="text-muted small">{{ isAdmin ? 'Comenza registrando la primera venta.' : 'Cuando registres una venta la vas a ver aqui.' }}</p>
      <router-link :to="{ name: 'VentaEditor' }" class="btn btn-primary mt-2"> + Nueva venta </router-link>
    </div>

    <div v-else class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4">#</th>
              <th>Fecha</th>
              <th>Cliente</th>
              <th>Usuario</th>
              <th>Total</th>
              <th>Cotizacion</th>
              <th>Estado</th>
              <th class="text-end pe-4">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="venta in ventas" :key="venta.id">
              <td class="ps-4 text-muted small">{{ venta.id }}</td>
              <td>{{ formatFecha(venta.fecha) }}</td>
              <td>
                <span class="fw-semibold">{{ venta.cliente?.nombre }} {{ venta.cliente?.apellido }}</span>
              </td>
              <td>
                <span class="text-muted small">{{ venta.user?.login ?? venta.createdBy ?? '-' }}</span>
              </td>
              <td class="fw-semibold text-primary">
                {{ venta.moneda?.simbolo ?? '$' }} {{ formatPrecio(venta.total) }} {{ venta.moneda?.codigo ?? '' }}
              </td>
              <td>{{ formatCotizacion(venta.cotizacion) }}</td>
              <td>
                <span class="badge" :class="badgeEstadoVenta(venta.estado)">
                  {{ venta.estado ?? '-' }}
                </span>
              </td>
              <td class="text-end pe-4">
                <router-link :to="{ name: 'VentaView', params: { ventaId: venta.id } }" class="btn btn-sm btn-outline-secondary">
                  Ver
                </router-link>
                <router-link :to="{ name: 'VentaEditorEdit', params: { ventaId: venta.id } }" class="btn btn-sm btn-outline-primary ms-2">
                  Editar
                </router-link>
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
import { useAlertService } from '@/shared/alert/alert.service';
import VentaService from '@/entities/venta/venta.service';
import type { IVenta } from '@/shared/model/venta.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';
import { useStore } from '@/store';
import { Authority } from '@/shared/jhipster/constants';

const ventaService = new VentaService();
const alertService = useAlertService();
const store = useStore();

const ventas = ref<IVenta[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const total = ref(0);
const isAdmin = computed(() => (store.account?.authorities ?? []).includes(Authority.ADMIN));

onMounted(cargar);

async function cargar() {
  loading.value = true;
  error.value = null;
  try {
    const res = await ventaService.retrieve({ page: 0, size: 50, sort: ['fecha,desc'] });
    ventas.value = res.data;
    total.value = Number(res.headers?.['x-total-count'] ?? res.data.length);
  } catch (e: any) {
    error.value = 'Error al cargar las ventas';
    alertService.showHttpError(e.response);
  } finally {
    loading.value = false;
  }
}

function badgeEstadoVenta(estado?: EstadoVenta | null) {
  const map: Record<string, string> = {
    [EstadoVenta.PENDIENTE]: 'bg-warning text-dark',
    [EstadoVenta.PAGADA]: 'bg-success',
    [EstadoVenta.CANCELADA]: 'bg-danger',
    [EstadoVenta.RESERVADA]: 'bg-info text-dark',
    [EstadoVenta.FINALIZADA]: 'bg-primary',
  };
  return map[estado ?? ''] ?? 'bg-light text-dark border';
}

function formatFecha(fecha?: Date | string) {
  if (!fecha) return '-';
  return new Date(fecha).toLocaleDateString('es-AR');
}

function formatPrecio(precio?: number | null) {
  return Number(precio ?? 0).toLocaleString('es-AR');
}

function formatCotizacion(cotizacion?: number | null) {
  if (cotizacion == null) return '-';
  return Number(cotizacion).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 8 });
}
</script>
