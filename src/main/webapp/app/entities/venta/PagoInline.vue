<template>
  <div>
    <div class="card mb-3 border-success" style="border-style: dashed !important">
      <div class="card-body">
        <p class="fw-semibold small text-success mb-3">Registrar pago</p>
        <div class="row g-2 align-items-end">
          <div class="col-md-3" v-if="!esEntregaUsado">
            <label class="form-label form-label-sm">Monto <span class="text-danger">*</span></label>
            <div class="input-group input-group-sm">
              <span class="input-group-text">$</span>
              <input type="number" class="form-control" v-model.number="nuevoPago.monto" min="0" step="0.01" placeholder="0.00" />
            </div>
            <button v-if="saldoPendiente > 0" type="button" class="btn btn-link btn-sm p-0 mt-1" @click="nuevoPago.monto = saldoPendiente">
              Usar saldo ($ {{ fmt(saldoPendiente) }})
            </button>
          </div>

          <div class="col-md-3">
            <label class="form-label form-label-sm">Metodo de pago</label>
            <select class="form-select form-select-sm" v-model="nuevoPago.metodoPago">
              <option :value="null">- Seleccionar -</option>
              <option v-for="m in metodoPagos" :key="m.id" :value="m">
                {{ m.descripcion ?? m.codigo }}
              </option>
            </select>
          </div>

          <div class="col-md-2" v-if="!esEntregaUsado">
            <label class="form-label form-label-sm">Moneda</label>
            <select class="form-select form-select-sm" v-model="nuevoPago.moneda">
              <option :value="null">-</option>
              <option v-for="m in monedas" :key="m.id" :value="m">{{ m.simbolo ?? '' }} {{ m.codigo }}</option>
            </select>
          </div>

          <div class="col-md-5" v-if="esEntregaUsado">
            <label class="form-label form-label-sm">Tasacion usada <span class="text-danger">*</span></label>
            <select class="form-select form-select-sm" v-model.number="nuevoPago.tasacionUsadoId">
              <option :value="null">- Seleccionar tasacion aceptada -</option>
              <option v-for="t in tasacionesUsado" :key="t.id" :value="t.id">
                #{{ t.id }} - {{ tasacionVehiculoLabel(t) }} - {{ t.patenteUsado ?? 'Sin patente' }} - {{ tasacionMonedaLabel(t) }} {{ fmt(t.montoTasacion) }}
              </option>
            </select>
            <div class="small text-muted mt-1">El monto se toma automaticamente desde la tasacion en {{ monedaBaseCodigo }}.</div>
            <div v-if="tasacionesUsado.length === 0" class="small mt-2">
              <span class="text-danger">No hay tasaciones aceptadas disponibles para este cliente.</span>
              <button class="btn btn-link btn-sm p-0 ms-2" @click="emit('crearTasacion')">Crear tasacion</button>
            </div>
          </div>

          <div class="col-md-3" v-if="!esEntregaUsado && nuevoPago.metodoPago?.requiereReferencia">
            <label class="form-label form-label-sm">Referencia</label>
            <input type="text" class="form-control form-control-sm" v-model="nuevoPago.referencia" placeholder="Nro. cheque / transferencia" />
          </div>

          <div class="col-md-1">
            <button class="btn btn-success btn-sm w-100" @click="agregarPago" :disabled="!!mensajeValidacionPago">+</button>
          </div>
        </div>
        <div class="mt-2 d-flex flex-wrap justify-content-between gap-2 small text-muted">
          <span>El sistema convierte pagos en moneda distinta usando Cotización (valor venta).</span>
          <span v-if="nuevoPago.metodoPago?.requiereReferencia">Este metodo requiere referencia obligatoria.</span>
        </div>
        <div v-if="mensajeValidacionPago" class="text-danger small mt-2">
          {{ mensajeValidacionPago }}
        </div>
      </div>
    </div>

    <div v-if="pagos.length === 0" class="text-center py-3 text-muted small border rounded">No hay pagos registrados aun.</div>

    <div v-else class="table-responsive">
      <table class="table align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Metodo</th>
            <th>Moneda</th>
            <th>Referencia</th>
            <th>Fecha</th>
            <th>Estado</th>
            <th class="text-end">Monto</th>
            <th class="text-end">Cotización</th>
            <th class="text-end">Aplicado ARS</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in pagos" :key="p._key">
            <td>{{ p.metodoPago?.descripcion ?? p.metodoPago?.codigo ?? '-' }}</td>
            <td>{{ p.moneda?.simbolo ?? '' }} {{ p.moneda?.codigo ?? '-' }}</td>
            <td class="text-muted small">{{ p.referencia || '-' }}</td>
            <td class="text-muted small">{{ formatFecha(p.fecha) }}</td>
            <td>
              <span class="badge" :class="p.estado === EstadoPago.ANULADO ? 'bg-secondary' : 'bg-success'">
                {{ p.estado === EstadoPago.ANULADO ? 'Anulado' : 'Registrado' }}
              </span>
            </td>
            <td class="text-end fw-semibold" :class="p.estado === EstadoPago.ANULADO ? 'text-muted text-decoration-line-through' : 'text-success'">
              $ {{ fmt(p.monto) }}
            </td>
            <td class="text-end">{{ fmt(p.cotizacionUsada) }}</td>
            <td class="text-end fw-semibold">{{ fmt(p.montoAplicadoMonedaVenta) }}</td>
            <td class="text-end">
              <button
                class="btn btn-sm btn-outline-danger"
                @click="p.guardado ? emit('anular', p._key) : emit('quitar', p._key)"
                :disabled="p.estado === EstadoPago.ANULADO"
                :title="p.guardado ? 'Anular pago' : 'Quitar'"
              >
                {{ p.guardado ? 'Anular' : 'Quitar' }}
              </button>
            </td>
          </tr>
        </tbody>
        <tfoot class="table-light">
          <tr>
            <td colspan="7" class="text-end fw-semibold">Total pagado aplicado (ARS):</td>
            <td class="text-end fw-bold text-success">$ {{ fmt(sumaPagos) }}</td>
            <td></td>
          </tr>
        </tfoot>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, reactive, watch } from 'vue';
import type { PagoLocal } from './useVentaEditor';
import type { IMetodoPago } from '@/shared/model/metodo-pago.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { ITasacionUsado } from '@/shared/model/tasacion-usado.model';
import { EstadoPago } from '@/shared/model/estado-pago.model';

const props = defineProps<{
  pagos: PagoLocal[];
  sumaPagos: number;
  saldoPendiente: number;
  metodoPagos: IMetodoPago[];
  monedas: IMoneda[];
  tasacionesUsado: ITasacionUsado[];
  monedaDefault?: IMoneda | null;
  monedaBaseCodigo?: string;
}>();

const emit = defineEmits<{
  agregar: [monto: number, metodoPago: IMetodoPago | null, moneda: IMoneda | null, referencia: string];
  quitar: [key: string];
  anular: [key: string];
  crearTasacion: [];
}>();

const nuevoPago = reactive({
  monto: 0,
  metodoPago: null as IMetodoPago | null,
  moneda: props.monedaDefault ?? null,
  referencia: '',
  tasacionUsadoId: null as number | null,
});

const esEntregaUsado = computed(() => (nuevoPago.metodoPago?.codigo ?? '').toUpperCase() === 'ENTREGA_USADO');
const monedaBaseCodigo = computed(() => props.monedaBaseCodigo ?? props.monedaDefault?.codigo ?? 'ARS');

watch(
  () => props.tasacionesUsado,
  tasaciones => {
    if (!nuevoPago.tasacionUsadoId) return;
    const sigueDisponible = tasaciones.some(t => t.id === nuevoPago.tasacionUsadoId);
    if (!sigueDisponible) {
      nuevoPago.tasacionUsadoId = null;
    }
  },
  { deep: true },
);

const mensajeValidacionPago = computed(() => {
  if (esEntregaUsado.value) {
    if (!nuevoPago.tasacionUsadoId) {
      return 'Debes seleccionar una tasacion aceptada';
    }
    return '';
  }

  if (!nuevoPago.monto || nuevoPago.monto <= 0) {
    return 'Ingresa un monto mayor a 0';
  }

  if (props.saldoPendiente <= 0) {
    return 'La venta no tiene saldo pendiente';
  }

  if (nuevoPago.metodoPago?.requiereReferencia && !nuevoPago.referencia.trim()) {
    return 'La referencia es obligatoria para ese metodo de pago';
  }

  return '';
});

function agregarPago() {
  if (mensajeValidacionPago.value) return;
  const referencia = esEntregaUsado.value ? String(nuevoPago.tasacionUsadoId ?? '') : nuevoPago.referencia.trim();
  emit('agregar', nuevoPago.monto, nuevoPago.metodoPago, nuevoPago.moneda, referencia);
  nuevoPago.monto = 0;
  nuevoPago.metodoPago = null;
  nuevoPago.moneda = props.monedaDefault ?? null;
  nuevoPago.referencia = '';
  nuevoPago.tasacionUsadoId = null;
}

function fmt(n?: number | null) {
  return Number(n ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2 });
}

function formatFecha(f?: Date) {
  return f ? new Date(f).toLocaleDateString('es-AR') : '-';
}

function tasacionVehiculoLabel(t: ITasacionUsado) {
  const marca = t.version?.modelo?.marca?.nombre;
  const modelo = t.version?.modelo?.nombre;
  const version = t.version?.nombre;
  return [marca, modelo, version].filter(Boolean).join(' ') || t.marcaModeloUsado || 'Usado';
}

function tasacionMonedaLabel(t: ITasacionUsado) {
  return t.moneda?.simbolo || t.moneda?.codigo || monedaBaseCodigo.value;
}
</script>
