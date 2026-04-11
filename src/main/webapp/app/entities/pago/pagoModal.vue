<template>
  <div class="modal-backdrop-custom" @click.self="emit('cerrar')">
    <div class="modal-dialog-custom bg-white p-4 rounded" style="max-width:500px">

      <h5>Registrar pago</h5>

      <div class="mb-3">
        <strong>Venta #{{ venta.id }}</strong><br />
        Cliente: {{ venta.cliente?.nombre }} {{ venta.cliente?.apellido }}
      </div>

      <div class="mb-3">
        <label>Monto</label>
        <input v-model.number="monto" type="number" class="form-control" />
        <small class="text-muted">
          Saldo: $ {{ venta.saldo }}
        </small>
      </div>

      <div class="mb-3">
        <label>Método</label>
        <select v-model="metodoPagoId" class="form-select">
          <option v-for="m in metodos" :key="m.id" :value="m.id">
            {{ m.descripcion }}
          </option>
        </select>
      </div>

      <div class="mb-3">
        <label>Referencia</label>
        <input v-model="referencia" class="form-control" />
      </div>

      <div class="d-flex justify-content-end gap-2">
        <button class="btn btn-secondary" @click="emit('cerrar')">Cancelar</button>
        <button class="btn btn-success" @click="pagar">Confirmar pago</button>
      </div>

    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import PagoService from '@/entities/pago/pago.service';

const props = defineProps({
  venta: Object
});

const emit = defineEmits(['cerrar', 'pagado']);

const pagoService = new PagoService();

const monto = ref(0);
const referencia = ref('');
const metodoPagoId = ref<number | null>(null);

const metodos = ref<any[]>([]); // cargar desde API si querés

async function pagar() {
  try {
    await pagoService.create({
      monto: monto.value,
      fecha: new Date(),
      referencia: referencia.value,
      venta: { id: props.venta.id },
      metodoPago: metodoPagoId.value ? { id: metodoPagoId.value } : null,
    });

    emit('pagado');
    emit('cerrar');
  } catch (e) {
    console.error(e);
  }
}
</script>
