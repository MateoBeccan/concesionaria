<template>
  <div class="container py-4" style="max-width: 960px">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="fw-semibold mb-0">Tasacion #{{ tasacion?.id }}</h4>
      <div class="d-flex gap-2">
        <router-link :to="{ name: 'TasacionUsado' }" class="btn btn-outline-secondary">Volver</router-link>
        <router-link v-if="tasacion?.id" :to="{ name: 'TasacionUsadoEdit', params: { tasacionUsadoId: tasacion.id } }" class="btn btn-primary">
          Editar
        </router-link>
      </div>
    </div>

    <div v-if="loading" class="text-center py-4"><span class="spinner-border" /></div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-else-if="!tasacion" class="alert alert-warning">No se encontro la tasacion.</div>

    <div v-else class="card border-0 shadow-sm">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-6"><strong>Cliente:</strong> {{ clienteLabel }}</div>
          <div class="col-md-3"><strong>Estado:</strong> {{ tasacion.estado }}</div>
          <div class="col-md-3"><strong>Fecha:</strong> {{ formatDate(tasacion.fechaTasacion) }}</div>
          <div class="col-md-4"><strong>Monto:</strong> $ {{ fmt(tasacion.montoTasacion) }}</div>
          <div class="col-md-8"><strong>Vehiculo tecnico:</strong> {{ vehiculoTecnico }}</div>
          <div class="col-md-4"><strong>Patente:</strong> {{ tasacion.patenteUsado || '-' }}</div>
          <div class="col-md-4"><strong>VIN/Chasis:</strong> {{ tasacion.vinChasisUsado || '-' }}</div>
          <div class="col-md-4"><strong>Anio:</strong> {{ tasacion.anioUsado ?? '-' }}</div>
          <div class="col-md-4"><strong>KM:</strong> {{ tasacion.kmUsado ?? '-' }}</div>
          <div class="col-md-4"><strong>Color:</strong> {{ tasacion.colorUsado || '-' }}</div>
          <div class="col-md-4"><strong>Tasador:</strong> {{ tasadorLabel }}</div>
          <div class="col-md-4"><strong>Version:</strong> {{ tasacion.version?.nombre || '-' }}</div>
          <div class="col-md-4"><strong>Motor:</strong> {{ tasacion.motor?.nombre || '-' }}</div>
          <div class="col-md-4"><strong>Tipo vehiculo:</strong> {{ tasacion.tipoVehiculo?.nombre || '-' }}</div>
          <div class="col-md-4"><strong>Inventario generado:</strong> {{ tasacion.inventarioGenerado?.id ? `SI (#${tasacion.inventarioGenerado.id})` : 'NO' }}</div>
          <div class="col-md-4"><strong>Aplicada a venta:</strong> {{ tasacion.ventaAplicadaId ? `SI (#${tasacion.ventaAplicadaId})` : 'NO' }}</div>
          <div class="col-12"><strong>Observaciones:</strong><div>{{ tasacion.observaciones || '-' }}</div></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import type { ITasacionUsado } from '@/shared/model/tasacion-usado.model';
import TasacionUsadoService from './tasacion-usado.service';
import { useAlertService } from '@/shared/alert/alert.service';

const route = useRoute();
const alertService = useAlertService();
const service = new TasacionUsadoService();
const loading = ref(false);
const error = ref<string | null>(null);
const tasacion = ref<ITasacionUsado | null>(null);

const clienteLabel = computed(() => {
  const cliente = tasacion.value?.cliente;
  if (!cliente) return '-';
  const full = [cliente.nombre, cliente.apellido].filter(Boolean).join(' ');
  return full || `Cliente #${cliente.id ?? ''}`.trim();
});

const vehiculoTecnico = computed(() => {
  const item = tasacion.value;
  if (!item) return '-';
  const marca = item.version?.modelo?.marca?.nombre;
  const modelo = item.version?.modelo?.nombre;
  const version = item.version?.nombre;
  const compuesto = [marca, modelo, version].filter(Boolean).join(' ');
  return compuesto || item.marcaModeloUsado || '-';
});

const tasadorLabel = computed(() => {
  const item = tasacion.value;
  if (!item) return '-';
  const nombre = [item.tasadorUser?.firstName, item.tasadorUser?.lastName].filter(Boolean).join(' ').trim();
  if (nombre) return `${nombre} (${item.tasadorUser?.login ?? '-'})`;
  return item.tasadorUser?.login || item.usuarioTasador || '-';
});

onMounted(async () => {
  const id = Number(route.params.tasacionUsadoId);
  if (!Number.isFinite(id) || id <= 0) {
    error.value = 'ID de tasacion invalido';
    return;
  }
  loading.value = true;
  try {
    tasacion.value = await service.find(id);
  } catch (e: any) {
    error.value = 'No se pudo cargar la tasacion';
    alertService.showHttpError(e?.response);
  } finally {
    loading.value = false;
  }
});

function formatDate(value?: Date | string) {
  if (!value) return '-';
  return new Date(value).toLocaleString('es-AR');
}

function fmt(value?: number | null) {
  return Number(value ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
</script>
