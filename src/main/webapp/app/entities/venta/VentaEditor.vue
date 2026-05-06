<template>
  <div class="container-fluid py-4" style="max-width: 1200px">
    <div class="page-header mb-4">
      <div>
        <h1 class="page-title mb-0">{{ venta.id ? `Editar venta #${venta.id}` : 'Nueva venta' }}</h1>
        <p class="page-subtitle">Registrá una venta o reserva</p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="router.push({ name: 'VentaList' })">Volver</button>
    </div>

    <div v-if="error" class="alert alert-danger d-flex justify-content-between align-items-center mb-3">
      <span>{{ error }}</span>
      <button class="btn-close btn-sm" @click="error = null" />
    </div>

    <section class="process-strip mb-4">
      <article v-for="step in flowSteps" :key="step.number" class="process-step" :class="{ done: step.done, current: step.current }">
        <div class="process-number">{{ step.number }}</div>
        <div>
          <div class="process-title">{{ step.title }}</div>
          <div class="process-copy">{{ step.copy }}</div>
        </div>
      </article>
    </section>

    <div class="row g-4">
      <div class="col-lg-8">
        <div class="card mb-3">
          <div class="card-header">
            <div>
              <div class="section-title">1. Cliente</div>
              <div class="section-copy">Selecciona al cliente para iniciar la operación.</div>
            </div>
          </div>
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
                <label class="form-label">% IVA</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="venta.porcentajeImpuesto" min="0" max="100" step="0.5" />
                  <span class="input-group-text">%</span>
                </div>
              </div>

              <div class="col-md-4">
                <label class="form-label">Estado</label>
                <div class="form-control d-flex align-items-center justify-content-between bg-light">
                  <span>{{ labelEstado(estadoCalculado) }}</span>
                  <span class="badge" :class="badgeEstado(estadoCalculado)">{{ labelEstado(estadoCalculado) }}</span>
                </div>
              </div>

              <div class="col-md-4">
                <label class="form-label">Reserva</label>
                <div class="form-control bg-light">La reserva requiere una seña mínima.</div>
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
            <div>
              <div class="section-title">2. Vehículo</div>
              <div class="section-copy">Selecciona la unidad y revisa el precio final.</div>
            </div>
            <span class="badge bg-primary">{{ detalles.length }} vehículo(s)</span>
          </div>
          <div class="card-body">
            <DetalleVentaInline
              :detalles="detalles"
              :suma-subtotales="sumaSubtotales"
              :moneda-venta="venta.moneda ?? null"
              @agregar="agregarVehiculo"
              @quitar="quitarDetalle"
              @actualizar-precio="actualizarPrecioDetalle"
            />
          </div>
        </div>

        <div class="card mb-3">
          <div class="card-header d-flex justify-content-between align-items-center">
            <div>
              <div class="section-title">3. Pagos</div>
              <div class="section-copy">Registra pagos y controla el saldo pendiente.</div>
            </div>
            <span class="badge" :class="Number(venta.saldo ?? 0) === 0 && detalles.length > 0 ? 'bg-success' : 'bg-warning text-dark'">
              {{ Number(venta.saldo ?? 0) === 0 && detalles.length > 0 ? 'Saldado' : `Saldo: ${venta.moneda?.simbolo ?? '$'} ${fmt(venta.saldo)}` }}
            </span>
          </div>
          <div class="card-body">
            <PagoInline
              :pagos="pagos"
              :suma-pagos="sumaPagos"
              :saldo-pendiente="Number(venta.saldo ?? 0)"
              :metodo-pagos="metodoPagos"
              :monedas="monedas"
              :tasaciones-usado="tasacionesUsadoDisponibles"
              :moneda-default="venta.moneda ?? null"
              :moneda-base-codigo="venta.moneda?.codigo ?? 'ARS'"
              @agregar="agregarPago"
              @quitar="quitarPago"
              @anular="anularPago"
              @crear-tasacion="crearTasacionDesdeVenta"
            />
          </div>
        </div>

        <div class="card mb-3">
          <div class="card-header">
            <div>
              <div class="section-title">4. Confirmación</div>
              <div class="section-copy">Confirma la reserva o cierra la venta.</div>
            </div>
          </div>
          <div class="card-body">
            <div class="row g-3 align-items-center">
              <div class="col-md-6">
                <label class="form-label">Tipo de comprobante</label>
                <select class="form-select" v-model="tipoComprobanteSeleccionado" :disabled="tieneComprobanteActivo">
                  <option :value="null">No generar comprobante</option>
                  <option v-for="tipo in tipoComprobantes" :key="tipo.id" :value="tipo">{{ tipo.codigo }} - {{ tipo.descripcion }}</option>
                </select>
              </div>
              <div class="col-md-6" v-if="tieneComprobanteActivo">
                <div class="alert alert-warning py-2 mb-0 small">La venta ya tiene un comprobante activo emitido.</div>
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
        >
          <template #acciones>
            <div class="summary-flow mb-3">
              <div class="summary-flow-item" :class="{ ready: !!venta.cliente?.id }">Cliente</div>
              <div class="summary-flow-item" :class="{ ready: detalles.length > 0 }">Vehículo</div>
              <div class="summary-flow-item" :class="{ ready: Number(venta.total ?? 0) > 0 }">Resumen</div>
              <div class="summary-flow-item" :class="{ ready: pagos.length > 0 }">Pagos</div>
            </div>
            <button class="btn btn-warning w-100" @click="confirmarReserva" :disabled="guardando || !puedeConfirmarReserva">
              <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
              Confirmar reserva
            </button>
            <button class="btn btn-success w-100" @click="confirmarVenta" :disabled="guardando || !puedeConfirmar">
              <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
              Confirmar venta
            </button>
            <p v-if="!puedeConfirmarReserva && !mensajeValidacion" class="text-danger small text-center mb-0">La reserva requiere una seña mínima.</p>
            <p v-if="!puedeConfirmar && !mensajeValidacion" class="text-danger small text-center mb-0">Confirmar venta requiere pago total (100%).</p>
            <p v-if="mensajeValidacion" class="text-muted small text-center mb-0">
              {{ mensajeValidacion }}
            </p>
          </template>
        </VentaResumen>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

import { useAlertService } from '@/shared/alert/alert.service';
import type { ICliente } from '@/shared/model/cliente.model';
import type { IInventario } from '@/shared/model/inventario.model';
import type { IReserva } from '@/shared/model/reserva.model';
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
  porcentajeMinimoReserva,
  tieneComprobanteActivo,
  cumpleMinimoReserva,
  estadoCalculado,
  sumaSubtotales,
  sumaPagos,
  tasacionesUsadoDisponibles,
  agregarVehiculo,
  actualizarPrecioDetalle,
  quitarDetalle,
  agregarPago,
  quitarPago,
  anularPago,
  confirmar,
  cargarVenta,
  cargarTasacionesUsadoCliente,
  fmt,
  setPorcentajeMinimoReserva,
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
  void cargarTasacionesUsadoCliente();
}

const mensajeValidacion = computed(() => {
  try {
    validarVentaAntesDeGuardar();
    return '';
  } catch (e: any) {
    return e.message ?? 'Revisa los datos de la venta';
  }
});

const puedeConfirmar = computed(() => !mensajeValidacion.value && Number(venta.value.total ?? 0) > 0 && Number(venta.value.saldo ?? 0) === 0);
const puedeConfirmarReserva = computed(
  () =>
    !mensajeValidacion.value &&
    Number(venta.value.total ?? 0) > 0 &&
    Number(venta.value.saldo ?? 0) > 0 &&
    Number(venta.value.totalPagado ?? 0) > 0 &&
    cumpleMinimoReserva.value,
);
const flowSteps = computed(() => [
  {
    number: '01',
    title: 'Cliente',
    copy: venta.value.cliente?.id ? 'Cliente seleccionado.' : 'Buscá o seleccioná el cliente.',
    done: !!venta.value.cliente?.id,
    current: !venta.value.cliente?.id,
  },
  {
    number: '02',
    title: 'Vehículo',
    copy: detalles.value.length > 0 ? `${detalles.value.length} unidad(es) agregada(s).` : 'Agregá al menos una unidad disponible.',
    done: detalles.value.length > 0,
    current: !!venta.value.cliente?.id && detalles.value.length === 0,
  },
  {
    number: '03',
    title: 'Resumen',
    copy: Number(venta.value.total ?? 0) > 0 ? `Total ${fmt(Number(venta.value.total ?? 0))}.` : 'Revisá subtotal e impuestos.',
    done: Number(venta.value.total ?? 0) > 0,
    current: detalles.value.length > 0 && Number(venta.value.total ?? 0) === 0,
  },
  {
    number: '04',
    title: 'Pagos',
    copy: Number(venta.value.totalPagado ?? 0) > 0 ? `${pagos.value.length} pago(s) cargado(s).` : 'Registrá pagos para continuar.',
    done: Number(venta.value.saldo ?? 0) === 0 && detalles.value.length > 0,
    current: detalles.value.length > 0,
  },
]);

onMounted(async () => {
  const [monedasRes, metodosRes, tiposRes, reservaConfigRes] = await Promise.all([
    axios.get('api/monedas?activo.equals=true&page=0&size=50'),
    axios.get('api/metodo-pagos?activo.equals=true&page=0&size=50'),
    axios.get('api/tipo-comprobantes?page=0&size=50'),
    axios.get('api/ventas/reserva-config'),
  ]);

  monedas.value = monedasRes.data;
  const normalizarMoneda = (value?: string | null) =>
    (value ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toUpperCase()
      .replace(/[^A-Z]/g, '');
  const esMonedaBase = (codigo?: string | null, descripcion?: string | null) => {
    const c = normalizarMoneda(codigo);
    const d = normalizarMoneda(descripcion);
    return c.includes('ARS') || c.includes('ARG') || c.includes('PESO') || d.includes('ARS') || d.includes('ARG') || d.includes('PESO');
  };
  const monedaBase = monedas.value.find(m => esMonedaBase(m.codigo, m.descripcion)) ?? null;
  if (monedaBase) venta.value.moneda = monedaBase;

  metodoPagos.value = metodosRes.data;
  tipoComprobantes.value = tiposRes.data;
  setPorcentajeMinimoReserva(Number(reservaConfigRes.data?.porcentajeMinimo ?? 0.1));

  if (route.params?.ventaId) {
    await cargarVenta(Number(route.params.ventaId));
    if (monedaBase) venta.value.moneda = monedaBase;
    return;
  }

  const reservaId = Number(route.query.reservaId);
  if (Number.isFinite(reservaId) && reservaId > 0) {
    try {
      const reservaRes = await axios.get<IReserva>(`api/reservas/${reservaId}`);
      const reserva = reservaRes.data;
      if (reserva.estado !== 'ACTIVA') throw new Error(`La reserva #${reservaId} no está activa`);

      venta.value.reserva = { id: reserva.id } as IReserva;
      if (!venta.value.cliente?.id && reserva.cliente?.id) venta.value.cliente = reserva.cliente;
      if (!venta.value.observaciones && reserva.observaciones) venta.value.observaciones = `Convertida desde reserva #${reserva.id}. ${reserva.observaciones}`;
      else if (!venta.value.observaciones) venta.value.observaciones = `Convertida desde reserva #${reserva.id}`;

      const vehiculoReservaId = reserva.inventario?.vehiculo?.id;
      if (Number.isFinite(vehiculoReservaId) && vehiculoReservaId > 0 && detalles.value.length === 0) {
        const vehiculo = await vehiculoService.find(Number(vehiculoReservaId));
        agregarVehiculo(vehiculo);
      }
    } catch (e: any) {
      const msg = e?.message ?? `No se pudo preparar la venta desde la reserva #${reservaId}`;
      alertService.showError(msg);
      await router.replace({ name: 'ReservaView', params: { reservaId } });
      return;
    }
  }

  const vehiculoId = Number(route.query.vehiculoId);
  if (Number.isFinite(vehiculoId) && vehiculoId > 0 && detalles.value.length === 0) {
    try {
      const vehiculo = await vehiculoService.find(vehiculoId);
      agregarVehiculo(vehiculo);

      const inventarioRes = await axios.get<IInventario>(`api/inventarios/vehiculo/${vehiculoId}`);
      if (inventarioRes.data?.id && inventarioRes.data?.estadoInventario === 'RESERVADO' && !venta.value.cliente?.id) {
        try {
          const reservaRes = await axios.get<IReserva>(`api/reservas/inventario/${inventarioRes.data.id}/activa`);
          if (reservaRes.data?.cliente?.id) venta.value.cliente = reservaRes.data.cliente;
        } catch (reservaError: any) {
          if (reservaError?.response?.status !== 404) alertService.showHttpError(reservaError.response);
        }
      }
    } catch (e: any) {
      if (e?.response?.status !== 404) alertService.showHttpError(e.response);
    }
  }

  const clienteId = Number(route.query.clienteId);
  if (Number.isFinite(clienteId) && clienteId > 0 && !venta.value.cliente?.id) {
    try {
      const clienteRes = await axios.get<ICliente>(`api/clientes/${clienteId}`);
      venta.value.cliente = clienteRes.data;
      await cargarTasacionesUsadoCliente();
    } catch {
      // noop
    }
  }
});

watch(
  () => venta.value.cliente?.id,
  () => {
    void cargarTasacionesUsadoCliente();
  },
);

watch(
  () => route.query.refreshTasaciones,
  async value => {
    if (!value) return;
    await cargarTasacionesUsadoCliente();
    const query = { ...route.query };
    delete query.refreshTasaciones;
    await router.replace({ query });
  },
  { immediate: true },
);

async function confirmarVenta() {
  try {
    validarVentaAntesDeGuardar();
    if (Number(venta.value.saldo ?? 0) > 0) throw new Error('Para vender la unidad se requiere pago total (100%).');
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

async function confirmarReserva() {
  try {
    validarVentaAntesDeGuardar();
    if (!cumpleMinimoReserva.value) throw new Error('La reserva requiere una seña mínima.');
    const { venta: ventaGuardada } = await confirmar();
    alertService.showSuccess(`Reserva confirmada para la venta #${ventaGuardada.id}`);
    if (!route.params?.ventaId) router.replace({ name: 'VentaEditorEdit', params: { ventaId: ventaGuardada.id } });
  } catch (e: any) {
    alertService.showError(e.message ?? 'Error al confirmar la reserva');
  }
}

async function crearTasacionDesdeVenta() {
  if (!venta.value.cliente?.id) {
    alertService.showError('Primero debes seleccionar un cliente para crear una tasación.');
    return;
  }
  await router.push({
    name: 'TasacionUsadoCreate',
    query: {
      clienteId: venta.value.cliente.id,
      returnTo: route.fullPath,
    },
  });
}

function labelEstado(estado: string) {
  return (
    {
      PENDIENTE: 'Pendiente',
      PAGADA: 'Pagada',
      CANCELADA: 'Cancelada',
      RESERVADA: 'Reservada',
      FINALIZADA: 'Finalizada',
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
      FINALIZADA: 'bg-primary',
    }[estado] ?? 'bg-light text-dark border'
  );
}
</script>

<style scoped>
.process-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
}

.process-step {
  display: flex;
  gap: 0.8rem;
  align-items: flex-start;
  padding: 0.9rem 1rem;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: #fff;
}

.process-step.current {
  border-color: #93c5fd;
  background: #f8fbff;
}

.process-step.done {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.process-number {
  width: 2rem;
  height: 2rem;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: #e2e8f0;
  color: #334155;
  font-size: 0.78rem;
  font-weight: 700;
  flex-shrink: 0;
}

.process-step.current .process-number {
  background: #2563eb;
  color: #fff;
}

.process-step.done .process-number {
  background: #16a34a;
  color: #fff;
}

.process-title,
.section-title {
  font-weight: 700;
  color: #0f172a;
}

.process-copy,
.section-copy {
  font-size: 0.84rem;
  color: #64748b;
}

.summary-flow {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.5rem;
}

.summary-flow-item {
  padding: 0.45rem 0.55rem;
  border-radius: 999px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 0.76rem;
  text-align: center;
  font-weight: 600;
}

.summary-flow-item.ready {
  background: #dcfce7;
  color: #166534;
}

@media (max-width: 991px) {
  .process-strip,
  .summary-flow {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 575px) {
  .process-strip,
  .summary-flow {
    grid-template-columns: 1fr;
  }
}
</style>
