<template>
  <div class="container-fluid py-4" style="max-width: 1200px">
    <div class="page-header mb-4">
      <div>
        <h1 class="page-title mb-0">
          {{ venta.id ? `Editar venta #${venta.id}` : 'Nueva venta' }}
        </h1>
        <p class="page-subtitle">Completa los datos para registrar la operacion en un unico flujo.</p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="router.push({ name: 'VentaList' })">Volver</button>
    </div>

    <div v-if="error" class="alert alert-danger d-flex justify-content-between align-items-center mb-3">
      <span>{{ error }}</span>
      <button class="btn-close btn-sm" @click="error = null" />
    </div>

    <div class="row g-4">
      <div class="col-lg-8">
        <div class="card mb-3">
          <div class="card-header">1. Datos de la venta</div>
          <div class="card-body">
            <div class="row g-3">
              <div class="col-12">
                <label class="form-label">Cliente <span class="text-danger">*</span></label>
                <div v-if="venta.cliente" class="d-flex align-items-center gap-2 rounded border bg-light p-2">
                  <div class="flex-grow-1">
                    <span class="fw-semibold">{{ venta.cliente.nombre }} {{ venta.cliente.apellido }}</span>
                    <span class="text-muted ms-2 small">{{ venta.cliente.nroDocumento }}</span>
                  </div>
                  <button class="btn btn-sm btn-outline-secondary" @click="venta.cliente = null">Cambiar</button>
                </div>
                <div v-else>
                  <div class="d-flex gap-2">
                    <input
                      v-model="busquedaCliente"
                      class="form-control"
                      placeholder="Buscar por nombre, DNI o email..."
                      @input="buscarClientes"
                    />
                  </div>
                  <ul v-if="resultadosCliente.length" class="list-group mt-1 shadow-sm">
                    <li
                      v-for="clienteItem in resultadosCliente"
                      :key="clienteItem.id"
                      class="list-group-item list-group-item-action d-flex justify-content-between"
                      style="cursor: pointer"
                      @click="seleccionarCliente(clienteItem)"
                    >
                      <span class="fw-semibold">{{ clienteItem.nombre }} {{ clienteItem.apellido }}</span>
                      <span class="text-muted small">{{ clienteItem.nroDocumento }}</span>
                    </li>
                  </ul>
                </div>
              </div>

              <div class="col-md-4">
                <label class="form-label">Fecha <span class="text-danger">*</span></label>
                <input type="datetime-local" class="form-control" v-model="fechaLocal" />
              </div>

              <div class="col-md-4">
                <label class="form-label">Moneda</label>
                <select class="form-select" v-model="venta.moneda">
                  <option :value="null">Seleccionar</option>
                  <option v-for="moneda in monedas" :key="moneda.id" :value="moneda">
                    {{ moneda.simbolo ?? '' }} {{ moneda.codigo }} - {{ moneda.descripcion }}
                  </option>
                </select>
                <small v-if="cotizacionActiva" class="text-muted">Cotizacion activa: {{ cotizacionActiva.valorVenta }}</small>
              </div>

              <div class="col-md-4">
                <label class="form-label">% IVA</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="venta.porcentajeImpuesto" min="0" max="100" step="0.5" />
                  <span class="input-group-text">%</span>
                </div>
              </div>

              <div class="col-md-6">
                <label class="form-label">Estado de la operacion</label>
                <div class="form-control d-flex align-items-center justify-content-between bg-light">
                  <span>{{ labelEstado(estadoCalculado) }}</span>
                  <span class="badge" :class="badgeEstado(estadoCalculado)">{{ labelEstado(estadoCalculado) }}</span>
                </div>
                <small class="text-muted">El estado se calcula automaticamente segun el saldo y los pagos cargados.</small>
              </div>

              <div class="col-12">
                <label class="form-label">Observaciones</label>
                <textarea class="form-control" v-model="venta.observaciones" rows="2" placeholder="Notas adicionales..." />
              </div>
            </div>
          </div>
        </div>

        <div class="card mb-3">
          <div class="card-header d-flex justify-content-between align-items-center">
            <span>2. Vehiculos</span>
            <span class="badge bg-primary">{{ detalles.length }} vehiculo(s)</span>
          </div>
          <div class="card-body">
            <DetalleVentaInline
              :detalles="detalles"
              :suma-subtotales="sumaSubtotales"
              @agregar="agregarVehiculo"
              @quitar="quitarDetalle"
              @actualizar-precio="actualizarPrecioDetalle"
            />
          </div>
        </div>

        <div class="card mb-3">
          <div class="card-header d-flex justify-content-between align-items-center">
            <span>3. Pagos</span>
            <span class="badge" :class="Number(venta.saldo ?? 0) === 0 && detalles.length > 0 ? 'bg-success' : 'bg-warning text-dark'">
              {{ Number(venta.saldo ?? 0) === 0 && detalles.length > 0 ? 'Saldado' : `Saldo: $ ${fmt(venta.saldo)}` }}
            </span>
          </div>
          <div class="card-body">
            <PagoInline
              :pagos="pagos"
              :suma-pagos="sumaPagos"
              :saldo-pendiente="Number(venta.saldo ?? 0)"
              :metodo-pagos="metodoPagos"
              :monedas="monedas"
              :moneda-default="venta.moneda ?? null"
              @agregar="agregarPago"
              @quitar="quitarPago"
            />
          </div>
        </div>

        <div class="card mb-3">
          <div class="card-header">4. Comprobante (opcional)</div>
          <div class="card-body">
            <div class="row g-3 align-items-center">
              <div class="col-md-6">
                <label class="form-label">Tipo de comprobante</label>
                <select class="form-select" v-model="tipoComprobanteSeleccionado">
                  <option :value="null">No generar comprobante</option>
                  <option v-for="tipo in tipoComprobantes" :key="tipo.id" :value="tipo">{{ tipo.codigo }} - {{ tipo.descripcion }}</option>
                </select>
              </div>
              <div class="col-md-6" v-if="tipoComprobanteSeleccionado">
                <div class="alert alert-info py-2 mb-0 small">
                  Se generara el comprobante <strong>{{ tipoComprobanteSeleccionado.codigo }}</strong> al confirmar la venta.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-lg-4">
        <VentaResumen
          :cliente="venta.cliente"
          :cantidad-vehiculos="detalles.length"
          :suma-subtotales="sumaSubtotales"
          :porcentaje-impuesto="venta.porcentajeImpuesto"
          :impuesto="venta.impuesto"
          :total="venta.total"
          :total-pagado="venta.totalPagado"
          :saldo="venta.saldo"
          :estado="estadoCalculado"
          :moneda="venta.moneda"
          :cotizacion="venta.cotizacion"
        >
          <template #acciones>
            <button class="btn btn-primary w-100" @click="guardar" :disabled="guardando || !puedeGuardar">
              <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
              {{ venta.id ? 'Guardar cambios' : 'Guardar borrador' }}
            </button>
            <button class="btn btn-success w-100" @click="confirmarVenta" :disabled="guardando || !puedeConfirmar">
              <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
              Confirmar venta
            </button>
            <p v-if="!puedeConfirmar" class="text-muted small text-center mb-0">
              {{ mensajeValidacion }}
            </p>
          </template>
        </VentaResumen>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

import { useAlertService } from '@/shared/alert/alert.service';
import type { ICliente } from '@/shared/model/cliente.model';
import type { ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';

import DetalleVentaInline from './DetalleVentaInline.vue';
import PagoInline from './PagoInline.vue';
import { useVentaEditor } from './useVentaEditor';
import VentaResumen from './VentaResumen.vue';

const router = useRouter();
const route = useRoute();
const alertService = useAlertService();
const vehiculoService = new VehiculoService();

const {
  venta,
  detalles,
  pagos,
  guardando,
  error,
  monedas,
  metodoPagos,
  tipoComprobantes,
  cotizacionActiva,
  estadoCalculado,
  sumaSubtotales,
  sumaPagos,
  agregarVehiculo,
  actualizarPrecioDetalle,
  quitarDetalle,
  agregarPago,
  quitarPago,
  confirmar,
  cargarVenta,
  fmt,
  validarVentaAntesDeGuardar,
} = useVentaEditor();

const tipoComprobanteSeleccionado = ref<ITipoComprobante | null>(null);
const busquedaCliente = ref('');
const resultadosCliente = ref<ICliente[]>([]);
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

function buscarClientes() {
  if (debounceTimer) clearTimeout(debounceTimer);

  debounceTimer = setTimeout(async () => {
    const q = busquedaCliente.value.trim();
    if (q.length < 2) {
      resultadosCliente.value = [];
      return;
    }

    try {
      const res = await axios.get<ICliente[]>('api/clientes/buscar', { params: { q } });
      resultadosCliente.value = res.data;
    } catch {
      resultadosCliente.value = [];
    }
  }, 300);
}

function seleccionarCliente(cliente: ICliente) {
  venta.value.cliente = cliente;
  busquedaCliente.value = '';
  resultadosCliente.value = [];
}

const fechaLocal = computed({
  get: () => {
    const fecha = venta.value.fecha;
    if (!fecha) return '';
    return new Date(fecha).toISOString().slice(0, 16);
  },
  set: value => {
    venta.value.fecha = value ? new Date(value) : new Date();
  },
});

const mensajeValidacion = computed(() => {
  try {
    validarVentaAntesDeGuardar();
    return '';
  } catch (e: any) {
    return e.message ?? 'Revisa los datos de la venta';
  }
});

const puedeGuardar = computed(() => !mensajeValidacion.value);
const puedeConfirmar = computed(() => !mensajeValidacion.value);

onMounted(async () => {
  const [monedasRes, metodosRes, tiposRes] = await Promise.all([
    axios.get('api/monedas?activo.equals=true&page=0&size=50'),
    axios.get('api/metodo-pagos?activo.equals=true&page=0&size=50'),
    axios.get('api/tipo-comprobantes?page=0&size=50'),
  ]);

  monedas.value = monedasRes.data;
  metodoPagos.value = metodosRes.data;
  tipoComprobantes.value = tiposRes.data;

  if (route.params?.ventaId) {
    await cargarVenta(Number(route.params.ventaId));
    return;
  }

  const vehiculoId = Number(route.query.vehiculoId);
  if (Number.isFinite(vehiculoId) && vehiculoId > 0 && detalles.value.length === 0) {
    try {
      const vehiculo = await vehiculoService.find(vehiculoId);
      agregarVehiculo(vehiculo);
    } catch (e: any) {
      alertService.showHttpError(e.response);
    }
  }
});

async function guardar() {
  try {
    validarVentaAntesDeGuardar();
    const { venta: ventaGuardada } = await confirmar();
    alertService.showSuccess(`Venta #${ventaGuardada.id} guardada correctamente`);

    if (!route.params?.ventaId) {
      router.replace({ name: 'VentaEditorEdit', params: { ventaId: ventaGuardada.id } });
    }
  } catch (e: any) {
    alertService.showError(e.message ?? 'Error al guardar la venta');
  }
}

async function confirmarVenta() {
  try {
    validarVentaAntesDeGuardar();
    const { venta: ventaGuardada, comprobante } = await confirmar(tipoComprobanteSeleccionado.value ?? undefined);
    const message = comprobante
      ? `Venta #${ventaGuardada.id} confirmada. Comprobante ${comprobante.numeroComprobante} generado.`
      : `Venta #${ventaGuardada.id} confirmada correctamente`;

    alertService.showSuccess(message);
    router.push({ name: 'VentaView', params: { ventaId: ventaGuardada.id } });
  } catch (e: any) {
    alertService.showError(e.message ?? 'Error al confirmar la venta');
  }
}

function labelEstado(estado: string) {
  return (
    {
      PENDIENTE: 'Pendiente',
      PAGADA: 'Pagada',
      CANCELADA: 'Cancelada',
      RESERVADA: 'Reservada',
    }[estado] ?? estado
  );
}

function badgeEstado(estado: string) {
  return (
    {
      PENDIENTE: 'bg-warning text-dark',
      PAGADA: 'bg-success',
      CANCELADA: 'bg-danger',
      RESERVADA: 'bg-info text-dark',
    }[estado] ?? 'bg-light text-dark border'
  );
}
</script>
