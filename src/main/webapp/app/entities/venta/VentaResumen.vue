<template>
  <div class="venta-resumen">
    <div class="resumen-header">
      <span class="fw-semibold">Resumen de venta</span>
      <span class="badge ms-2" :class="badgeEstado(estado)">{{ labelEstado(estado) }}</span>
    </div>

    <div class="resumen-body">
      <div class="resumen-row" v-if="cliente">
        <span class="resumen-label">Cliente</span>
        <span class="resumen-value fw-semibold">{{ cliente.nombre }} {{ cliente.apellido }}</span>
      </div>
      <div class="resumen-row text-muted small" v-if="!cliente">
        <span>Sin cliente seleccionado</span>
      </div>

      <hr class="my-2" />

      <div class="resumen-row">
        <span class="resumen-label">Vehículo</span>
        <span class="resumen-value">{{ cantidadVehiculos }}</span>
      </div>

      <div class="resumen-row">
        <span class="resumen-label">Subtotal</span>
        <span class="resumen-value">{{ moneda?.simbolo ?? '$' }} {{ fmt(sumaSubtotales) }} {{ moneda?.codigo ?? '' }}</span>
      </div>

      <div class="resumen-row" v-if="porcentajeImpuesto">
        <span class="resumen-label">IVA ({{ porcentajeImpuesto }}%)</span>
        <span class="resumen-value">{{ moneda?.simbolo ?? '$' }} {{ fmt(impuesto) }} {{ moneda?.codigo ?? '' }}</span>
      </div>

      <hr class="my-2" />

      <div class="resumen-row resumen-total">
        <span>Total</span>
        <span>{{ moneda?.simbolo ?? '$' }} {{ fmt(total) }} {{ moneda?.codigo ?? '' }}</span>
      </div>

      <div class="resumen-row">
        <span class="resumen-label">Pagado</span>
        <span class="resumen-value text-success fw-semibold">{{ moneda?.simbolo ?? '$' }} {{ fmt(totalPagado) }} {{ moneda?.codigo ?? '' }}</span>
      </div>

      <div class="resumen-row resumen-saldo" :class="saldo > 0 ? 'saldo-pendiente' : 'saldo-ok'">
        <span>Saldo pendiente</span>
        <span>{{ moneda?.simbolo ?? '$' }} {{ fmt(saldo) }} {{ moneda?.codigo ?? '' }}</span>
      </div>
    </div>

    <div class="resumen-footer">
      <slot name="acciones" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { ICliente } from '@/shared/model/cliente.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';

const props = defineProps<{
  cliente: ICliente | null | undefined;
  cantidadVehiculos: number;
  sumaSubtotales: number;
  porcentajeImpuesto: number | null | undefined;
  impuesto: number | null | undefined;
  total: number | null | undefined;
  totalPagado: number | null | undefined;
  saldo: number | null | undefined;
  estado: string | null | undefined;
  moneda: IMoneda | null | undefined;
}>();

function fmt(n?: number | null) {
  return Number(n ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2 });
}

function badgeEstado(e?: string | null) {
  return {
    [EstadoVenta.PENDIENTE]: 'bg-warning text-dark',
    [EstadoVenta.PAGADA]: 'bg-success',
    [EstadoVenta.CANCELADA]: 'bg-danger',
    [EstadoVenta.RESERVADA]: 'bg-info',
    [EstadoVenta.FINALIZADA]: 'bg-primary',
  }[e ?? ''] ?? 'bg-light text-dark border';
}

function labelEstado(e?: string | null) {
  return {
    [EstadoVenta.PENDIENTE]: 'Pendiente',
    [EstadoVenta.PAGADA]: 'Pagada',
    [EstadoVenta.CANCELADA]: 'Cancelada',
    [EstadoVenta.RESERVADA]: 'Reservada',
    [EstadoVenta.FINALIZADA]: 'Finalizada',
  }[e ?? ''] ?? e ?? '-';
}
</script>

<style scoped>
.venta-resumen {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  position: sticky;
  top: 80px;
  overflow: hidden;
}

.resumen-header {
  background: var(--color-primary);
  color: #fff;
  padding: 0.85rem 1.25rem;
  font-size: 0.9rem;
}

.resumen-body {
  padding: 1rem 1.25rem;
}

.resumen-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.25rem 0;
  font-size: 0.85rem;
}

.resumen-label {
  color: var(--color-text-muted);
}

.resumen-value {
  font-weight: 500;
}

.resumen-total {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-primary);
  padding: 0.4rem 0;
}

.resumen-saldo {
  font-size: 0.9rem;
  font-weight: 600;
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-sm);
  margin-top: 0.25rem;
}

.saldo-pendiente {
  background: #fef2f2;
  color: var(--color-danger);
}

.saldo-ok {
  background: #f0fdf4;
  color: var(--color-success);
}

.resumen-footer {
  padding: 1rem 1.25rem;
  border-top: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
</style>
