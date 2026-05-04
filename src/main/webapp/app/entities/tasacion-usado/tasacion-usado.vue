<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4 class="fw-semibold mb-0">Tasaciones</h4>
        <p class="text-muted small mb-0" v-if="!loading">{{ filtered.length }} tasaciones</p>
      </div>
      <router-link :to="{ name: 'TasacionUsadoCreate' }" class="btn btn-primary">Nueva tasacion</router-link>
    </div>

    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <input v-model="search" class="form-control" placeholder="Buscar por cliente, patente o VIN" />
          </div>
          <div class="col-md-3">
            <select v-model="estadoFiltro" class="form-select">
              <option value="">Todos los estados</option>
              <option value="PENDIENTE">PENDIENTE</option>
              <option value="ACEPTADA">ACEPTADA</option>
              <option value="RECHAZADA">RECHAZADA</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-4"><span class="spinner-border" /></div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-else-if="filtered.length === 0" class="alert alert-warning">No hay tasaciones para mostrar.</div>

    <div v-else class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th>#</th>
              <th>Cliente</th>
              <th>Vehiculo usado</th>
              <th>Patente/VIN</th>
              <th class="text-end">Monto</th>
              <th>Estado</th>
              <th>Tasador</th>
              <th>Inventario</th>
              <th>Venta aplicada</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filtered" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ clienteLabel(item) }}</td>
              <td>{{ vehiculoTecnico(item) }}</td>
              <td>{{ item.patenteUsado || item.vinChasisUsado || '-' }}</td>
              <td class="text-end">$ {{ fmt(item.montoTasacion) }}</td>
              <td>
                <span class="badge" :class="estadoBadge(item.estado)">{{ item.estado }}</span>
              </td>
              <td>{{ tasadorLabel(item) }}</td>
              <td>
                <span class="badge" :class="item.inventarioGenerado?.id ? 'bg-success' : 'bg-secondary'">
                  {{ item.inventarioGenerado?.id ? `SI (#${item.inventarioGenerado.id})` : 'NO' }}
                </span>
              </td>
              <td>
                <span class="badge" :class="item.ventaAplicadaId ? 'bg-info text-dark' : 'bg-secondary'">
                  {{ item.ventaAplicadaId ? `SI (#${item.ventaAplicadaId})` : 'NO' }}
                </span>
              </td>
              <td class="text-end">
                <router-link :to="{ name: 'TasacionUsadoView', params: { tasacionUsadoId: item.id } }" class="btn btn-sm btn-outline-secondary">
                  Ver
                </router-link>
                <router-link :to="{ name: 'TasacionUsadoEdit', params: { tasacionUsadoId: item.id } }" class="btn btn-sm btn-outline-primary ms-2">
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
import type { ITasacionUsado } from '@/shared/model/tasacion-usado.model';
import TasacionUsadoService from './tasacion-usado.service';

const service = new TasacionUsadoService();
const alertService = useAlertService();

const items = ref<ITasacionUsado[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const search = ref('');
const estadoFiltro = ref('');

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase();
  return items.value.filter(item => {
    const byEstado = !estadoFiltro.value || item.estado === estadoFiltro.value;
    const text = [item.cliente?.nombre, item.cliente?.apellido, item.patenteUsado, item.vinChasisUsado, item.marcaModeloUsado, vehiculoTecnico(item)]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();
    return byEstado && (!q || text.includes(q));
  });
});

onMounted(cargar);

async function cargar() {
  loading.value = true;
  error.value = null;
  try {
    const res = await service.retrieve({ page: 0, size: 200, sort: ['fechaTasacion,desc'] });
    items.value = res.data ?? [];
  } catch (e: any) {
    error.value = 'No se pudieron cargar las tasaciones';
    alertService.showHttpError(e?.response);
  } finally {
    loading.value = false;
  }
}

function clienteLabel(item: ITasacionUsado) {
  const nombre = [item.cliente?.nombre, item.cliente?.apellido].filter(Boolean).join(' ');
  if (nombre) return nombre;
  return item.cliente?.id ? `Cliente #${item.cliente.id}` : '-';
}

function fmt(value?: number | null) {
  return Number(value ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function estadoBadge(estado?: string) {
  if (estado === 'ACEPTADA') return 'bg-success';
  if (estado === 'RECHAZADA') return 'bg-danger';
  return 'bg-warning text-dark';
}

function vehiculoTecnico(item: ITasacionUsado) {
  const marca = item.version?.modelo?.marca?.nombre;
  const modelo = item.version?.modelo?.nombre;
  const version = item.version?.nombre;
  const compuesto = [marca, modelo, version].filter(Boolean).join(' ');
  return compuesto || item.marcaModeloUsado || '-';
}

function tasadorLabel(item: ITasacionUsado) {
  const nombre = [item.tasadorUser?.firstName, item.tasadorUser?.lastName].filter(Boolean).join(' ').trim();
  if (nombre) {
    return `${nombre} (${item.tasadorUser?.login ?? '-'})`;
  }
  return item.tasadorUser?.login || item.usuarioTasador || '-';
}
</script>
