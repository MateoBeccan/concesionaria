<template>
  <div class="container py-4">
    <div v-if="loading" class="text-center py-4">
      <span class="spinner-border" />
    </div>

    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

    <div v-else-if="reserva" class="card border-0 shadow-sm">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-start mb-3">
          <div>
            <h4 class="mb-0">Reserva #{{ reserva.id }}</h4>
            <small class="text-muted">Gestion comercial de unidad reservada</small>
          </div>
          <span class="badge" :class="estadoBadge(reserva.estado)">{{ reserva.estado }}</span>
        </div>

        <dl class="row mb-0">
          <dt class="col-sm-3">Cliente</dt>
          <dd class="col-sm-9">{{ clienteLabel(reserva) }}</dd>

          <dt class="col-sm-3">Vehiculo</dt>
          <dd class="col-sm-9">{{ vehiculoLabel(reserva) }}</dd>

          <dt class="col-sm-3">Venta asociada</dt>
          <dd class="col-sm-9">
            <router-link v-if="reserva.ventaAsociada?.id" :to="{ name: 'VentaView', params: { ventaId: reserva.ventaAsociada.id } }">
              #{{ reserva.ventaAsociada.id }}
            </router-link>
            <span v-else>-</span>
          </dd>

          <dt class="col-sm-3">Fecha reserva</dt>
          <dd class="col-sm-9">{{ formatDate(reserva.fechaReserva) }}</dd>

          <dt class="col-sm-3">Vencimiento</dt>
          <dd class="col-sm-9">{{ formatDate(reserva.fechaVencimiento) }}</dd>

          <dt class="col-sm-3">Seña</dt>
          <dd class="col-sm-9">{{ montoLabel(reserva) }}</dd>

          <dt class="col-sm-3">Observaciones</dt>
          <dd class="col-sm-9">{{ reserva.observaciones || '-' }}</dd>
        </dl>
      </div>
      <div class="card-footer bg-white d-flex justify-content-between">
        <router-link :to="{ name: 'Reserva' }" class="btn btn-outline-secondary">Volver</router-link>
        <div class="d-flex gap-2">
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
            class="btn btn-success"
          >
            Convertir en venta
          </router-link>
          <button v-if="reserva.estado === 'ACTIVA'" class="btn btn-outline-danger" @click="cancelar(reserva.id)">Cancelar reserva</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useAlertService } from '@/shared/alert/alert.service';
import type { IInventario } from '@/shared/model/inventario.model';
import type { IReserva } from '@/shared/model/reserva.model';
import InventarioService from '@/entities/inventario/inventario.service';
import ReservaService from './reserva.service';

const reservaService = new ReservaService();
const inventarioService = new InventarioService();
const alertService = useAlertService();
const route = useRoute();

const reserva = ref<IReserva | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

onMounted(cargar);

async function cargar() {
  loading.value = true;
  error.value = null;
  try {
    const id = Number(route.params.reservaId);
    const fetched = await reservaService.find(id);
    reserva.value = await enriquecerReserva(fetched);
  } catch (e: any) {
    error.value = 'No se pudo cargar la reserva';
    alertService.showHttpError(e.response);
  } finally {
    loading.value = false;
  }
}

function formatDate(value?: Date | string) {
  if (!value) return '-';
  return new Date(value).toLocaleString('es-AR');
}

function formatImporte(value?: number | null) {
  return Number(value).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function clienteLabel(r: IReserva) {
  const c = r.cliente;
  if (!c) return '-';
  return [c.nombre, c.apellido].filter(Boolean).join(' ') || `Cliente #${c.id ?? ''}`.trim();
}

function vehiculoLabel(r: IReserva) {
  const v = r.inventario?.vehiculo;
  if (!v) return '-';
  return v.patente || `Vehiculo #${v.id ?? ''}`.trim();
}

function monedaLabel(r: IReserva) {
  return r.moneda?.codigo || '$';
}

function montoLabel(r: IReserva) {
  if (r.montoSenia === null || r.montoSenia === undefined) return '-';
  return `${monedaLabel(r)} ${formatImporte(safeNumber(r.montoSenia))}`;
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
    await reservaService.cancelar(id, 'Cancelada desde detalle de reserva');
    alertService.showInfo(`Reserva ${id} cancelada`);
    await cargar();
  } catch (e: any) {
    alertService.showHttpError(e.response);
  }
}

async function enriquecerReserva(data: IReserva): Promise<IReserva> {
  let enriched = data;
  const inventarioId = data.inventario?.id;
  if (inventarioId && !data.inventario?.vehiculo) {
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

  if (enriched.id && safeNumber(enriched.montoSenia) <= 0) {
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

function safeNumber(value: unknown): number {
  const parsed = Number(value ?? 0);
  return Number.isFinite(parsed) ? parsed : 0;
}
</script>
