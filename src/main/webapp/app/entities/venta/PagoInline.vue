<template>
  <div>
    <div class="card mb-3 border-success" style="border-style: dashed !important">
      <div class="card-body">
        <p class="fw-semibold small text-success mb-3">Registrar pago</p>
        <div class="row g-2 align-items-end">
          <div class="col-md-2" v-if="!esEntregaUsado">
            <label class="form-label form-label-sm">Monto <span class="text-danger">*</span></label>
            <input type="number" class="form-control form-control-sm" v-model.number="nuevoPago.monto" min="0" step="0.01" />
          </div>
          <div class="col-md-3">
            <label class="form-label form-label-sm">Metodo <span class="text-danger">*</span></label>
            <select class="form-select form-select-sm" v-model="nuevoPago.metodoPago">
              <option :value="null">- Seleccionar -</option>
              <option v-for="m in metodoPagos" :key="m.id" :value="m">{{ m.descripcion ?? m.codigo }}</option>
            </select>
          </div>
          <div class="col-md-2" v-if="!esEntregaUsado">
            <label class="form-label form-label-sm">Moneda <span class="text-danger">*</span></label>
            <select class="form-select form-select-sm" v-model="nuevoPago.moneda">
              <option :value="null">-</option>
              <option v-for="m in monedas" :key="m.id" :value="m">{{ m.simbolo ?? '' }} {{ m.codigo }}</option>
            </select>
          </div>
          <div class="col-md-3" v-if="requiereBancoEntidad">
            <label class="form-label form-label-sm">Entidad financiera <span class="text-danger">*</span></label>
            <select class="form-select form-select-sm" v-model="nuevoPago.entidadFinanciera">
              <option :value="null">- Seleccionar -</option>
              <option v-for="e in entidadesFinancieras" :key="e.id" :value="e">{{ e.nombre ?? e.codigo }}</option>
            </select>
          </div>
          <div class="col-md-3" v-if="requiereComprobanteExterno">
            <label class="form-label form-label-sm">Comprobante externo <span class="text-danger">*</span></label>
            <input type="text" class="form-control form-control-sm" v-model.trim="nuevoPago.comprobanteExterno" maxlength="100" />
          </div>
          <div class="col-md-3" v-if="admiteObservaciones">
            <label class="form-label form-label-sm">Observaciones</label>
            <input type="text" class="form-control form-control-sm" v-model.trim="nuevoPago.observaciones" maxlength="500" />
          </div>

          <div class="col-md-4" v-if="esEntregaUsado">
            <label class="form-label form-label-sm">Tasacion usada <span class="text-danger">*</span></label>
            <select class="form-select form-select-sm" v-model.number="nuevoPago.tasacionUsadoId">
              <option :value="null">- Seleccionar tasacion -</option>
              <option v-for="t in tasacionesUsado" :key="t.id" :value="t.id">
                #{{ t.id }} - {{ t.patenteUsado ?? 'Sin patente' }} - {{ tasacionMonedaLabel(t) }} {{ fmt(t.montoTasacion) }}
              </option>
            </select>
          </div>

          <div class="col-md-1">
            <button class="btn btn-success btn-sm w-100" @click="agregarPago" :disabled="!!mensajeValidacionPago">+</button>
          </div>
        </div>
        <div v-if="mensajeValidacionPago" class="text-danger small mt-2">{{ mensajeValidacionPago }}</div>
      </div>
    </div>

    <div v-if="pagos.length === 0" class="text-center py-3 text-muted small border rounded">No hay pagos registrados aun.</div>

    <div v-else class="table-responsive">
      <table class="table align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Fecha</th>
            <th>Metodo</th>
            <th class="text-end">Monto original</th>
            <th>Moneda</th>
            <th class="text-end">Cotizacion</th>
            <th class="text-end">Aplicado ARS</th>
            <th>Ref./Operacion</th>
            <th>Detalle</th>
            <th>Estado</th>
            <th>Comprobante</th>
            <th>Usuario</th>
            <th class="text-end">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in pagos" :key="p._key" :class="p.estado === EstadoPago.ANULADO ? 'table-secondary' : ''">
            <td>{{ formatFecha(p.fecha) }}</td>
            <td>{{ p.metodoPago?.descripcion ?? p.metodoPago?.codigo ?? '-' }}</td>
            <td class="text-end">{{ fmt(p.monto) }}</td>
            <td>{{ p.moneda?.simbolo ?? '' }} {{ p.moneda?.codigo ?? '-' }}</td>
            <td class="text-end">{{ fmt(p.cotizacionUsada) }}</td>
            <td class="text-end fw-semibold">{{ fmt(p.montoAplicadoMonedaVenta) }}</td>
            <td>{{ p.referencia || p.numeroOperacion || '-' }}</td>
            <td>{{ p.entidadFinanciera?.nombre || p.bancoEntidad || p.comprobanteExterno || p.observaciones || '-' }}</td>
            <td>
              <span class="badge" :class="p.estado === EstadoPago.ANULADO ? 'bg-secondary' : 'bg-success'">
                {{ p.estado === EstadoPago.ANULADO ? 'ANULADO' : 'REGISTRADO' }}
              </span>
            </td>
            <td>{{ p.comprobanteNumero || '-' }}</td>
            <td>{{ p.usuarioRegistro ?? '-' }}</td>
            <td class="text-end">
              <button class="btn btn-sm btn-outline-danger" @click="abrirModalAnulacion(p)" :disabled="p.estado === EstadoPago.ANULADO" v-if="p.guardado">
                Anular
              </button>
              <button class="btn btn-sm btn-outline-secondary" @click="emit('quitar', p._key)" v-else>Quitar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <b-modal id="modal-anular-pago" ref="modalAnularPago">
      <template #title>Anular pago</template>
      <div class="mb-2 small text-muted">Debes ingresar motivo de anulacion.</div>
      <textarea class="form-control" rows="3" v-model="motivoAnulacion" maxlength="500" />
      <template #footer>
        <button class="btn btn-secondary btn-sm" @click="cerrarModalAnulacion">Cancelar</button>
        <button class="btn btn-danger btn-sm" :disabled="!motivoAnulacion.trim()" @click="confirmarAnulacion">Confirmar anulacion</button>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from 'vue';
import type { PagoLocal } from './useVentaEditor';
import type { IMetodoPago } from '@/shared/model/metodo-pago.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { ITasacionUsado } from '@/shared/model/tasacion-usado.model';
import type { IEntidadFinanciera } from '@/shared/model/entidad-financiera.model';
import { EstadoPago } from '@/shared/model/estado-pago.model';

const props = defineProps<{
  pagos: PagoLocal[];
  sumaPagos: number;
  saldoPendiente: number;
  metodoPagos: IMetodoPago[];
  monedas: IMoneda[];
  tasacionesUsado: ITasacionUsado[];
  entidadesFinancieras: IEntidadFinanciera[];
  monedaDefault?: IMoneda | null;
  monedaBaseCodigo?: string;
}>();

const emit = defineEmits<{
  agregar: [
    monto: number,
    metodoPago: IMetodoPago | null,
    moneda: IMoneda | null,
    tasacionUsadoId?: number | null,
    entidadFinanciera?: IEntidadFinanciera | null,
    bancoEntidad?: string,
    comprobanteExterno?: string,
    observaciones?: string,
  ];
  quitar: [key: string];
  anular: [key: string, motivo: string];
  crearTasacion: [];
}>();

const nuevoPago = reactive({
  monto: 0,
  metodoPago: null as IMetodoPago | null,
  moneda: props.monedaDefault ?? null,
  tasacionUsadoId: null as number | null,
  bancoEntidad: '',
  entidadFinanciera: null as IEntidadFinanciera | null,
  comprobanteExterno: '',
  observaciones: '',
});

const pagoAAnular = ref<PagoLocal | null>(null);
const motivoAnulacion = ref('');
const modalAnularPago = ref<any>(null);

const esEntregaUsado = computed(() => (nuevoPago.metodoPago?.codigo ?? '').toUpperCase() === 'ENTREGA_USADO');
const codigoMetodo = computed(() => (nuevoPago.metodoPago?.codigo ?? '').toUpperCase());
const requiereBancoEntidad = computed(() => ['TRANSFERENCIA', 'DEPOSITO', 'CHEQUE'].includes(codigoMetodo.value));
const requiereComprobanteExterno = computed(() => codigoMetodo.value === 'CHEQUE');
const admiteObservaciones = computed(() => ['AJUSTE', 'BONIFICACION', 'FINANCIACION', 'PERMUTA'].includes(codigoMetodo.value));
const mensajeValidacionPago = computed(() => {
  if (!nuevoPago.metodoPago) return 'Selecciona un metodo de pago';
  if (!esEntregaUsado.value && (!nuevoPago.moneda || !nuevoPago.moneda.id)) return 'Selecciona una moneda';
  if (esEntregaUsado.value && !nuevoPago.tasacionUsadoId) return 'Debes seleccionar una tasacion aceptada';
  if (!esEntregaUsado.value && (!nuevoPago.monto || nuevoPago.monto <= 0)) return 'Ingresa un monto mayor a 0';
  if (props.saldoPendiente <= 0) return 'La venta no tiene saldo pendiente';
  if (requiereBancoEntidad.value && !nuevoPago.entidadFinanciera?.id && !nuevoPago.bancoEntidad.trim()) return 'Debes informar una entidad financiera';
  if (requiereComprobanteExterno.value && !nuevoPago.comprobanteExterno.trim()) return 'Debes informar comprobante externo';
  return '';
});

function agregarPago() {
  if (mensajeValidacionPago.value) return;
  emit(
    'agregar',
    nuevoPago.monto,
    nuevoPago.metodoPago,
    nuevoPago.moneda,
    nuevoPago.tasacionUsadoId,
    nuevoPago.entidadFinanciera,
    nuevoPago.bancoEntidad.trim(),
    nuevoPago.comprobanteExterno.trim(),
    nuevoPago.observaciones.trim(),
  );
  nuevoPago.monto = 0;
  nuevoPago.metodoPago = null;
  nuevoPago.moneda = props.monedaDefault ?? null;
  nuevoPago.tasacionUsadoId = null;
  nuevoPago.bancoEntidad = '';
  nuevoPago.entidadFinanciera = null;
  nuevoPago.comprobanteExterno = '';
  nuevoPago.observaciones = '';
}

function abrirModalAnulacion(pago: PagoLocal) {
  pagoAAnular.value = pago;
  motivoAnulacion.value = '';
  modalAnularPago.value?.show();
}

function cerrarModalAnulacion() {
  modalAnularPago.value?.hide();
  pagoAAnular.value = null;
  motivoAnulacion.value = '';
}

function confirmarAnulacion() {
  if (!pagoAAnular.value || !motivoAnulacion.value.trim()) return;
  emit('anular', pagoAAnular.value._key, motivoAnulacion.value.trim());
  cerrarModalAnulacion();
}

function fmt(n?: number | null) {
  return Number(n ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 8 });
}

function formatFecha(f?: Date) {
  return f ? new Date(f).toLocaleString('es-AR') : '-';
}

function tasacionMonedaLabel(t: ITasacionUsado) {
  return t.moneda?.simbolo || t.moneda?.codigo || props.monedaBaseCodigo || 'ARS';
}
</script>
