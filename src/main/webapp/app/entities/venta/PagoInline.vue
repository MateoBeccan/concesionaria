<template>
  <div>

    <!-- FORMULARIO NUEVO PAGO -->
    <div class="card mb-3 border-success" style="border-style:dashed!important">
      <div class="card-body">
        <p class="fw-semibold small text-success mb-3">Registrar pago</p>
        <div class="row g-2 align-items-end">

          <div class="col-md-3">
            <label class="form-label form-label-sm">Monto <span class="text-danger">*</span></label>
            <div class="input-group input-group-sm">
              <span class="input-group-text">$</span>
              <input
                type="number"
                class="form-control"
                v-model.number="nuevoPago.monto"
                min="0"
                step="0.01"
                placeholder="0.00"
              />
            </div>
            <button
              v-if="saldoPendiente > 0"
              type="button"
              class="btn btn-link btn-sm p-0 mt-1"
              @click="nuevoPago.monto = saldoPendiente"
            >
              Usar saldo ($ {{ fmt(saldoPendiente) }})
            </button>
          </div>

          <div class="col-md-3">
            <label class="form-label form-label-sm">Método de pago</label>
            <select class="form-select form-select-sm" v-model="nuevoPago.metodoPago">
              <option :value="null">— Seleccionar —</option>
              <option v-for="m in metodoPagos" :key="m.id" :value="m">
                {{ m.descripcion ?? m.codigo }}
              </option>
            </select>
          </div>

          <div class="col-md-2">
            <label class="form-label form-label-sm">Moneda</label>
            <select class="form-select form-select-sm" v-model="nuevoPago.moneda">
              <option :value="null">—</option>
              <option v-for="m in monedas" :key="m.id" :value="m">
                {{ m.simbolo ?? '' }} {{ m.codigo }}
              </option>
            </select>
          </div>

          <div class="col-md-3" v-if="nuevoPago.metodoPago?.requiereReferencia">
            <label class="form-label form-label-sm">Referencia</label>
            <input type="text" class="form-control form-control-sm" v-model="nuevoPago.referencia" placeholder="Nro. cheque / transferencia" />
          </div>

          <div class="col-md-1">
            <button
              class="btn btn-success btn-sm w-100"
              @click="agregarPago"
              :disabled="!nuevoPago.monto || nuevoPago.monto <= 0"
            >
              +
            </button>
          </div>

        </div>
      </div>
    </div>

    <!-- LISTA DE PAGOS -->
    <div v-if="pagos.length === 0" class="text-center py-3 text-muted small border rounded">
      No hay pagos registrados aún.
    </div>

    <div v-else class="table-responsive">
      <table class="table align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Método</th>
            <th>Moneda</th>
            <th>Referencia</th>
            <th>Fecha</th>
            <th class="text-end">Monto</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in pagos" :key="p._key">
            <td>{{ p.metodoPago?.descripcion ?? p.metodoPago?.codigo ?? '—' }}</td>
            <td>{{ p.moneda?.simbolo ?? '' }} {{ p.moneda?.codigo ?? '—' }}</td>
            <td class="text-muted small">{{ p.referencia || '—' }}</td>
            <td class="text-muted small">{{ formatFecha(p.fecha) }}</td>
            <td class="text-end fw-semibold text-success">$ {{ fmt(p.monto) }}</td>
            <td class="text-end">
              <button
                class="btn btn-sm btn-outline-danger"
                @click="emit('quitar', p._key)"
                :disabled="p.guardado"
                :title="p.guardado ? 'Pago ya persistido' : 'Quitar'"
              >
                ✕
              </button>
            </td>
          </tr>
        </tbody>
        <tfoot class="table-light">
          <tr>
            <td colspan="4" class="text-end fw-semibold">Total pagado:</td>
            <td class="text-end fw-bold text-success">$ {{ fmt(sumaPagos) }}</td>
            <td></td>
          </tr>
        </tfoot>
      </table>
    </div>

  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue';
import type { PagoLocal } from './useVentaEditor';
import type { IMetodoPago } from '@/shared/model/metodo-pago.model';
import type { IMoneda } from '@/shared/model/moneda.model';

const props = defineProps<{
  pagos: PagoLocal[];
  sumaPagos: number;
  saldoPendiente: number;
  metodoPagos: IMetodoPago[];
  monedas: IMoneda[];
}>();

const emit = defineEmits<{
  agregar: [monto: number, metodoPago: IMetodoPago | null, moneda: IMoneda | null, referencia: string];
  quitar: [key: string];
}>();

const nuevoPago = reactive({
  monto:      0,
  metodoPago: null as IMetodoPago | null,
  moneda:     null as IMoneda | null,
  referencia: '',
});

function agregarPago() {
  if (!nuevoPago.monto || nuevoPago.monto <= 0) return;
  emit('agregar', nuevoPago.monto, nuevoPago.metodoPago, nuevoPago.moneda, nuevoPago.referencia);
  nuevoPago.monto      = 0;
  nuevoPago.referencia = '';
}

function fmt(n?: number | null) {
  return Number(n ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2 });
}

function formatFecha(f?: Date) {
  return f ? new Date(f).toLocaleDateString('es-AR') : '—';
}
</script>
