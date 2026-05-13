<template>
  <div class="container-fluid py-4 venta-wizard">
    <div class="wizard-header">
      <div>
        <h1 class="wizard-title">{{ venta.id ? `Editar venta #${venta.id}` : 'Nueva venta' }}</h1>
        <p class="wizard-subtitle">Flujo secuencial: cliente, vehiculo, pagos y cierre.</p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="requestExitToVentas">Volver</button>
    </div>

    <div v-if="error" class="alert alert-danger d-flex justify-content-between align-items-center mb-3">
      <span>{{ error }}</span>
      <button class="btn-close btn-sm" @click="error = null" />
    </div>

    <section class="wizard-steps mb-4">
      <button
        v-for="step in stepsUi"
        :key="step.number"
        type="button"
        class="wizard-step"
        :class="wizardStepClass(step.number)"
        :disabled="!canJumpTo(step.number)"
        @click="goToStep(step.number)"
      >
        <span class="wizard-step-number">{{ step.number }}</span>
        <span class="wizard-step-text">
          <strong>{{ step.title }}</strong>
          <small>{{ step.copy }}</small>
        </span>
      </button>
    </section>

    <div class="row g-4">
      <div class="col-lg-8">
        <div v-if="currentStep === 1" class="card">
          <div class="card-header">
            <div class="section-title">1. Cliente</div>
            <div class="section-copy">Selecciona y confirma el cliente.</div>
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
                  <button class="btn btn-sm btn-outline-secondary" @click="iniciarCambioCliente">Cambiar</button>
                </div>
                <div v-else>
                  <input v-model="busquedaCliente" class="form-control" placeholder="Buscar por nombre, DNI o email..." @input="buscarClientes" />
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
              <div class="col-12">
                <label class="form-label">Observaciones</label>
                <textarea class="form-control" v-model="venta.observaciones" rows="2" placeholder="Notas de la operacion..." />
              </div>
            </div>

            <div class="wizard-actions mt-3">
              <button class="btn btn-primary" :disabled="!venta.cliente?.id" @click="confirmarPasoCliente">Confirmar cliente y continuar</button>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 2" class="card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <div>
              <div class="section-title">2. Vehiculo</div>
              <div class="section-copy">Agrega la unidad y confirma para avanzar.</div>
            </div>
            <span class="badge bg-primary">{{ detalles.length }} unidad(es)</span>
          </div>
          <div class="card-body">
            <DetalleVentaInline
              :detalles="detalles"
              :suma-subtotales="sumaSubtotales"
              :moneda-venta="venta.moneda ?? null"
              @agregar="onAgregarVehiculo"
              @quitar="quitarDetalle"
              @actualizar-precio="actualizarPrecioDetalle"
            />

            <div class="wizard-actions mt-3">
              <button class="btn btn-outline-secondary" @click="goToStep(1)">Atras</button>
              <button class="btn btn-primary" :disabled="detalles.length === 0" @click="confirmarPasoVehiculo">Confirmar vehiculo y continuar</button>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 3" class="card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <div>
              <div class="section-title">3. Pagos</div>
              <div class="section-copy">Registra los pagos necesarios para reserva o venta.</div>
            </div>
            <span class="badge" :class="Number(venta.saldo ?? 0) <= 0 ? 'bg-success' : 'bg-warning text-dark'">
              {{ Number(venta.saldo ?? 0) <= 0 ? 'Saldado' : `Saldo ${venta.moneda?.simbolo ?? '$'} ${fmt(venta.saldo)}` }}
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
              :entidades-financieras="entidadesFinancieras"
              :moneda-default="venta.moneda ?? null"
              :moneda-base-codigo="venta.moneda?.codigo ?? 'ARS'"
              @agregar="agregarPago"
              @quitar="quitarPago"
              @anular="anularPago"
              @crear-tasacion="crearTasacionDesdeVenta"
            />

            <div class="wizard-actions mt-3">
              <button class="btn btn-outline-secondary" @click="goToStep(2)">Atras</button>
              <button class="btn btn-primary" :disabled="!canConfirmPagoStep" @click="confirmarPasoPagos">Confirmar pagos y continuar</button>
            </div>
            <p v-if="!canConfirmPagoStep" class="text-muted small mt-2 mb-0">Para continuar registra al menos un pago valido.</p>
          </div>
        </div>

        <div v-if="currentStep === 4" class="card">
          <div class="card-header">
            <div class="section-title">4. Comprobantes y cierre</div>
            <div class="section-copy">Emite comprobante opcional y confirma reserva o venta.</div>
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

            <div class="wizard-actions mt-3">
              <button class="btn btn-outline-secondary" @click="goToStep(3)">Atras</button>
              <button class="btn btn-warning" @click="confirmarReserva" :disabled="guardando || !puedeConfirmarReserva">
                <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
                Confirmar reserva
              </button>
              <button class="btn btn-success" @click="confirmarVenta" :disabled="guardando || !puedeConfirmarVenta">
                <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
                Confirmar venta
              </button>
            </div>

            <p v-if="!puedeConfirmarVenta" class="text-muted small mt-2 mb-0">Para confirmar venta, el saldo debe quedar en cero.</p>
            <p v-if="!puedeConfirmarReserva && Number(venta.saldo ?? 0) > 0" class="text-muted small mt-1 mb-0">La reserva requiere una seña minima.</p>
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
        />
      </div>
    </div>

    <b-modal id="wizard-warning-modal" ref="warningModalRef" title="Confirmar cambios">
      <p class="mb-0">{{ warningMessage }}</p>
      <template #footer>
        <button class="btn btn-secondary btn-sm" @click="cancelWarningAction">Cancelar</button>
        <button class="btn btn-danger btn-sm" @click="acceptWarningAction">Continuar</button>
      </template>
    </b-modal>

    <b-modal id="wizard-final-confirm-modal" ref="finalConfirmModalRef" title="Confirmacion final">
      <div class="small text-muted mb-2">Revisa el resumen antes de confirmar.</div>
      <div class="final-summary">
        <div><strong>Accion:</strong> {{ pendingFinalActionLabel }}</div>
        <div><strong>Cliente:</strong> {{ venta.cliente?.nombre }} {{ venta.cliente?.apellido }}</div>
        <div><strong>Vehiculo:</strong> {{ detalles[0]?.vehiculo?.patente ?? '-' }}</div>
        <div><strong>Total:</strong> {{ venta.moneda?.simbolo ?? '$' }} {{ fmt(venta.total) }}</div>
        <div><strong>Pagado:</strong> {{ venta.moneda?.simbolo ?? '$' }} {{ fmt(venta.totalPagado) }}</div>
        <div><strong>Saldo:</strong> {{ venta.moneda?.simbolo ?? '$' }} {{ fmt(venta.saldo) }}</div>
        <div><strong>Comprobante:</strong> {{ tipoComprobanteSeleccionado ? `${tipoComprobanteSeleccionado.codigo} - ${tipoComprobanteSeleccionado.descripcion}` : 'No generar' }}</div>
      </div>
      <template #footer>
        <button class="btn btn-secondary btn-sm" @click="closeFinalConfirmModal">Cancelar</button>
        <button class="btn btn-success btn-sm" :disabled="guardando" @click="executeFinalAction">
          <span v-if="guardando" class="spinner-border spinner-border-sm me-1" />
          Confirmar
        </button>
      </template>
    </b-modal>

    <b-modal id="wizard-exit-confirm-modal" ref="exitConfirmModalRef" title="Confirmar salida">
      <p class="mb-0">Hay datos cargados que todavía no fueron confirmados. ¿Querés salir y perder los cambios?</p>
      <template #footer>
        <button class="btn btn-secondary btn-sm" @click="cancelExit">Cancelar</button>
        <button class="btn btn-danger btn-sm" @click="confirmExit">Salir</button>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter, type RouteLocationRaw } from 'vue-router';
import axios from 'axios';

import { useAlertService } from '@/shared/alert/alert.service';
import type { ICliente } from '@/shared/model/cliente.model';
import type { IInventario } from '@/shared/model/inventario.model';
import type { IReserva } from '@/shared/model/reserva.model';
import type { ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';

import DetalleVentaInline from './DetalleVentaInline.vue';
import PagoInline from './PagoInline.vue';
import VentaResumen from './VentaResumen.vue';
import { useVentaEditor } from './useVentaEditor';

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
  entidadesFinancieras,
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
const currentStep = ref(1);
const stepConfirmed = ref({ cliente: false, vehiculo: false, pagos: false });
const warningModalRef = ref<any>(null);
const warningMessage = ref('');
const warningAction = ref<null | (() => void)>(null);
const finalConfirmModalRef = ref<any>(null);
const pendingFinalAction = ref<'venta' | 'reserva' | null>(null);
const exitConfirmModalRef = ref<any>(null);
const pendingExitTarget = ref<RouteLocationRaw | null>(null);
const allowNavigationWithoutPrompt = ref(false);
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

const canConfirmPagoStep = computed(() => pagosActivos.value > 0);
const pagosActivos = computed(() => pagos.value.filter(item => item.estado !== 'ANULADO').length);
const puedeConfirmarVenta = computed(() => Number(venta.value.total ?? 0) > 0 && Number(venta.value.saldo ?? 0) === 0);
const puedeConfirmarReserva = computed(
  () =>
    Number(venta.value.total ?? 0) > 0 &&
    Number(venta.value.saldo ?? 0) > 0 &&
    Number(venta.value.totalPagado ?? 0) > 0 &&
    cumpleMinimoReserva.value,
);
const pendingFinalActionLabel = computed(() => (pendingFinalAction.value === 'reserva' ? 'Confirmar reserva' : 'Confirmar venta'));
const hasUnsavedData = computed(() => {
  const hasCliente = Boolean(venta.value.cliente?.id);
  const hasVehiculo = detalles.value.length > 0 || Boolean(venta.value.vehiculo?.id);
  const hasPagos = pagos.value.length > 0;
  const hasComprobanteSeleccionado = Boolean(tipoComprobanteSeleccionado.value?.id);
  const hasObservaciones = Boolean(String(venta.value.observaciones ?? '').trim());
  const hasStepConfirmations = stepConfirmed.value.cliente || stepConfirmed.value.vehiculo || stepConfirmed.value.pagos;
  return hasCliente || hasVehiculo || hasPagos || hasComprobanteSeleccionado || hasObservaciones || hasStepConfirmations;
});

const stepsUi = computed(() => [
  {
    number: 1,
    title: 'Cliente',
    copy: stepConfirmed.value.cliente ? 'Confirmado' : 'Seleccionar',
    done: stepConfirmed.value.cliente,
  },
  {
    number: 2,
    title: 'Vehiculo',
    copy: stepConfirmed.value.vehiculo ? 'Confirmado' : 'Seleccionar',
    done: stepConfirmed.value.vehiculo,
  },
  {
    number: 3,
    title: 'Pagos',
    copy: stepConfirmed.value.pagos ? 'Confirmado' : 'Registrar',
    done: stepConfirmed.value.pagos,
  },
  {
    number: 4,
    title: 'Cierre',
    copy: 'Reserva o venta',
    done: false,
  },
]);

function canJumpTo(step: number) {
  if (step <= 1) return true;
  if (step === 2) return stepConfirmed.value.cliente;
  if (step === 3) return stepConfirmed.value.cliente && stepConfirmed.value.vehiculo;
  return stepConfirmed.value.cliente && stepConfirmed.value.vehiculo && stepConfirmed.value.pagos;
}

function wizardStepClass(step: number) {
  return {
    active: currentStep.value === step,
    done: step < currentStep.value || stepsUi.value.find(item => item.number === step)?.done,
    blocked: !canJumpTo(step),
    pending: step > currentStep.value && canJumpTo(step),
  };
}

function goToStep(step: number) {
  if (!canJumpTo(step)) return;
  currentStep.value = step;
}

function confirmarPasoCliente() {
  if (!venta.value.cliente?.id) return;
  stepConfirmed.value.cliente = true;
  goToStep(2);
}

function confirmarPasoVehiculo() {
  if (!detalles.value.length) return;
  stepConfirmed.value.vehiculo = true;
  goToStep(3);
}

function confirmarPasoPagos() {
  if (!canConfirmPagoStep.value) return;
  stepConfirmed.value.pagos = true;
  goToStep(4);
}

function clearPagosAndStepState() {
  pagos.value = [];
  stepConfirmed.value.pagos = false;
  if (currentStep.value > 3) currentStep.value = 3;
}

function iniciarCambioCliente() {
  if (!venta.value.cliente?.id) {
    venta.value.cliente = null;
    return;
  }
  if (detalles.value.length === 0 && pagosActivos.value === 0) {
    venta.value.cliente = null;
    return;
  }
  warningMessage.value =
    'Al cambiar el cliente se limpiaran pagos/tasaciones asociados y deberas reconfirmar los pasos posteriores. ¿Deseas continuar?';
  warningAction.value = () => {
    venta.value.cliente = null;
    clearPagosAndStepState();
    stepConfirmed.value.vehiculo = detalles.value.length > 0;
    if (currentStep.value > 1) currentStep.value = 1;
  };
  warningModalRef.value?.show();
}

function onAgregarVehiculo(vehiculo: IVehiculo) {
  const currentVehiculoId = detalles.value[0]?.vehiculo?.id;
  const sameVehiculo = currentVehiculoId && vehiculo.id && currentVehiculoId === vehiculo.id;
  if (!sameVehiculo && currentVehiculoId && pagosActivos.value > 0) {
    warningMessage.value = 'Cambiar el vehiculo puede invalidar pagos registrados. Se limpiaran los pagos para recalcular la venta. ¿Continuar?';
    warningAction.value = () => {
      clearPagosAndStepState();
      agregarVehiculo(vehiculo);
      stepConfirmed.value.vehiculo = true;
      if (currentStep.value > 2) currentStep.value = 2;
    };
    warningModalRef.value?.show();
    return;
  }
  agregarVehiculo(vehiculo);
}

function cancelWarningAction() {
  warningAction.value = null;
  warningMessage.value = '';
  warningModalRef.value?.hide();
}

function acceptWarningAction() {
  const action = warningAction.value;
  cancelWarningAction();
  action?.();
}

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

watch(
  () => venta.value.cliente?.id,
  value => {
    if (!value) {
      stepConfirmed.value.cliente = false;
      stepConfirmed.value.vehiculo = false;
      stepConfirmed.value.pagos = false;
      if (currentStep.value > 1) currentStep.value = 1;
    }
  },
);

watch(
  () => detalles.value.length,
  value => {
    if (!value) {
      stepConfirmed.value.vehiculo = false;
      stepConfirmed.value.pagos = false;
      if (currentStep.value > 2) currentStep.value = 2;
    }
  },
);

watch(pagosActivos, value => {
  if (!value) {
    stepConfirmed.value.pagos = false;
    if (currentStep.value > 3) currentStep.value = 3;
  }
});

onMounted(async () => {
  const [monedasRes, metodosRes, tiposRes, reservaConfigRes, entidadesRes] = await Promise.all([
    axios.get('api/monedas?activo.equals=true&page=0&size=50'),
    axios.get('api/metodo-pagos?activo.equals=true&page=0&size=50'),
    axios.get('api/tipo-comprobantes?page=0&size=50'),
    axios.get('api/ventas/reserva-config'),
    axios.get('api/entidades-financieras'),
  ]);

  monedas.value = monedasRes.data;
  metodoPagos.value = metodosRes.data;
  tipoComprobantes.value = tiposRes.data;
  entidadesFinancieras.value = entidadesRes.data ?? [];
  setPorcentajeMinimoReserva(Number(reservaConfigRes.data?.porcentajeMinimo ?? 0.1));

  const monedaBase = (monedas.value ?? []).find(m => (m.codigo ?? '').toUpperCase().includes('ARS')) ?? null;
  if (monedaBase) venta.value.moneda = monedaBase;

  if (route.params?.ventaId) {
    await cargarVenta(Number(route.params.ventaId));
    stepConfirmed.value = {
      cliente: Boolean(venta.value.cliente?.id),
      vehiculo: detalles.value.length > 0,
      pagos: pagosActivos.value > 0,
    };
    currentStep.value = stepConfirmed.value.pagos ? 4 : stepConfirmed.value.vehiculo ? 3 : stepConfirmed.value.cliente ? 2 : 1;
    return;
  }

  const reservaId = Number(route.query.reservaId);
  if (Number.isFinite(reservaId) && reservaId > 0) {
    try {
      const reservaRes = await axios.get<IReserva>(`api/reservas/${reservaId}`);
      const reserva = reservaRes.data;
      if (reserva.estado !== 'ACTIVA') throw new Error(`La reserva #${reservaId} no esta activa`);
      venta.value.reserva = { id: reserva.id } as IReserva;
      if (!venta.value.cliente?.id && reserva.cliente?.id) venta.value.cliente = reserva.cliente;
      const vehiculoReservaId = reserva.inventario?.vehiculo?.id;
      if (vehiculoReservaId && detalles.value.length === 0) {
        const vehiculo = await vehiculoService.find(Number(vehiculoReservaId));
        agregarVehiculo(vehiculo);
      }
    } catch {
      // noop
    }
  }

  const vehiculoId = Number(route.query.vehiculoId);
  if (Number.isFinite(vehiculoId) && vehiculoId > 0 && detalles.value.length === 0) {
    try {
      const vehiculo = await vehiculoService.find(vehiculoId);
      agregarVehiculo(vehiculo);
      const inventarioRes = await axios.get<IInventario>(`api/inventarios/vehiculo/${vehiculoId}`);
      if (inventarioRes.data?.id && inventarioRes.data?.estadoInventario === 'RESERVADO' && !venta.value.cliente?.id) {
        const reservaRes = await axios.get<IReserva>(`api/reservas/inventario/${inventarioRes.data.id}/activa`);
        if (reservaRes.data?.cliente?.id) venta.value.cliente = reservaRes.data.cliente;
      }
    } catch {
      // noop
    }
  }

  stepConfirmed.value.cliente = Boolean(venta.value.cliente?.id);
  stepConfirmed.value.vehiculo = detalles.value.length > 0;
  currentStep.value = stepConfirmed.value.vehiculo ? 3 : stepConfirmed.value.cliente ? 2 : 1;
});

watch(
  () => venta.value.cliente?.id,
  () => {
    void cargarTasacionesUsadoCliente();
  },
);

async function confirmarVenta() {
  pendingFinalAction.value = 'venta';
  finalConfirmModalRef.value?.show();
}

async function confirmarReserva() {
  pendingFinalAction.value = 'reserva';
  finalConfirmModalRef.value?.show();
}

function closeFinalConfirmModal() {
  finalConfirmModalRef.value?.hide();
  pendingFinalAction.value = null;
}

async function executeFinalAction() {
  if (pendingFinalAction.value === 'reserva') {
    await doConfirmarReserva();
    return;
  }
  await doConfirmarVenta();
}

async function doConfirmarVenta() {
  try {
    validarVentaAntesDeGuardar();
    if (Number(venta.value.saldo ?? 0) > 0) throw new Error('Para vender la unidad se requiere pago total.');
    const { venta: ventaGuardada, comprobante } = await confirmar(tipoComprobanteSeleccionado.value ?? undefined);
    const message = comprobante
      ? `Venta #${ventaGuardada.id} confirmada con exito. Comprobante ${comprobante.numeroComprobante} emitido.`
      : `Venta #${ventaGuardada.id} confirmada con exito.`;
    alertService.showSuccess(message);
    allowNavigationWithoutPrompt.value = true;
    closeFinalConfirmModal();
    await router.push({ name: 'VentaView', params: { ventaId: ventaGuardada.id } });
  } catch (e: any) {
    alertService.showError(e.message ?? 'Error al confirmar la venta');
  }
}

async function doConfirmarReserva() {
  try {
    validarVentaAntesDeGuardar();
    if (!cumpleMinimoReserva.value) throw new Error('La reserva requiere una seña minima.');
    const { venta: ventaGuardada } = await confirmar();
    alertService.showSuccess(`Reserva confirmada con exito para la venta #${ventaGuardada.id}.`);
    allowNavigationWithoutPrompt.value = true;
    closeFinalConfirmModal();
    await router.push({ name: 'VentaView', params: { ventaId: ventaGuardada.id } });
  } catch (e: any) {
    alertService.showError(e.message ?? 'Error al confirmar la reserva');
  }
}

async function crearTasacionDesdeVenta() {
  if (!venta.value.cliente?.id) {
    alertService.showError('Primero debes seleccionar un cliente.');
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

function requestExitToVentas() {
  if (!hasUnsavedData.value || allowNavigationWithoutPrompt.value) {
    void router.push({ name: 'Venta' });
    return;
  }
  pendingExitTarget.value = { name: 'Venta' };
  exitConfirmModalRef.value?.show();
}

function cancelExit() {
  pendingExitTarget.value = null;
  exitConfirmModalRef.value?.hide();
}

function confirmExit() {
  const target = pendingExitTarget.value;
  allowNavigationWithoutPrompt.value = true;
  pendingExitTarget.value = null;
  exitConfirmModalRef.value?.hide();
  if (target) {
    void router.push(target);
  }
}

function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (allowNavigationWithoutPrompt.value || !hasUnsavedData.value) return;
  event.preventDefault();
  event.returnValue = '';
}

onBeforeRouteLeave(to => {
  if (allowNavigationWithoutPrompt.value || !hasUnsavedData.value) {
    return true;
  }
  pendingExitTarget.value = to.fullPath;
  exitConfirmModalRef.value?.show();
  return false;
});

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload);
});

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);
});
</script>

<style scoped>
.venta-wizard {
  max-width: 1240px;
}

.wizard-header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 1rem;
  margin-bottom: 1rem;
}

.wizard-title {
  margin: 0;
  color: #0f172a;
  font-size: 1.55rem;
  font-weight: 700;
}

.wizard-subtitle {
  margin: 0.25rem 0 0;
  color: #64748b;
}

.wizard-steps {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.7rem;
}

.wizard-step {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  padding: 0.65rem 0.75rem;
  text-align: left;
  display: flex;
  gap: 0.65rem;
  align-items: center;
}

.wizard-step:disabled {
  opacity: 0.55;
}

.wizard-step.active {
  border-color: #93c5fd;
  background: #eff6ff;
}

.wizard-step.done {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.wizard-step.pending {
  border-color: #cbd5e1;
}

.wizard-step.blocked {
  border-style: dashed;
}

.wizard-step-number {
  width: 1.8rem;
  height: 1.8rem;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: #e2e8f0;
  font-size: 0.78rem;
  font-weight: 700;
}

.wizard-step.active .wizard-step-number {
  background: #2563eb;
  color: #fff;
}

.wizard-step.done .wizard-step-number {
  background: #16a34a;
  color: #fff;
}

.wizard-step-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.wizard-step-text small {
  color: #64748b;
}

.section-title {
  font-weight: 700;
  color: #0f172a;
}

.section-copy {
  font-size: 0.86rem;
  color: #64748b;
}

.wizard-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.55rem;
}

.final-summary {
  display: grid;
  gap: 0.35rem;
}

@media (max-width: 991px) {
  .wizard-steps {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 575px) {
  .wizard-steps {
    grid-template-columns: 1fr;
  }
}
</style>
