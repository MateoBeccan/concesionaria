<template>
  <div style="max-width: 700px; margin: 0 auto">
    <!-- PAGE HEADER -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Nueva Venta</h1>
        <p class="page-subtitle">Completá los pasos para registrar la operación</p>
      </div>
      <router-link :to="{ name: 'VentaList' }" class="btn btn-sm btn-outline-secondary"> ← Volver </router-link>
    </div>

    <!-- STEPPER -->
    <div class="wizard-stepper">
      <div class="wizard-step" :class="stepClass(1)">
        <div class="step-circle">
          <span v-if="paso > 1">✓</span>
          <span v-else>1</span>
        </div>
        <span class="step-label">Cliente</span>
      </div>

      <div class="wizard-connector" :class="{ done: paso > 1 }" />

      <div class="wizard-step" :class="stepClass(2)">
        <div class="step-circle">
          <span v-if="paso > 2">✓</span>
          <span v-else>2</span>
        </div>
        <span class="step-label">Vehículo</span>
      </div>

      <div class="wizard-connector" :class="{ done: paso > 2 }" />

      <div class="wizard-step" :class="stepClass(3)">
        <div class="step-circle">3</div>
        <span class="step-label">Confirmar</span>
      </div>
    </div>

    <!-- CONTENIDO DEL PASO -->
    <div class="card">
      <div class="card-body p-4">
        <div class="d-flex align-items-center gap-2 mb-4">
          <div
            class="rounded d-flex align-items-center justify-content-center fw-bold text-white"
            style="width: 28px; height: 28px; font-size: 0.8rem; background: var(--color-primary)"
          >
            {{ paso }}
          </div>
          <h6 class="mb-0 fw-semibold" style="font-size: 0.9rem">{{ tituloPaso }}</h6>
        </div>

        <StepCliente v-if="paso === 1" @seleccionado="onClienteSeleccionado" />
        <StepVehiculo
          v-if="paso === 2"
          :initial-vehiculo-id="vehiculoIdFromQuery"
          @seleccionado="onVehiculoSeleccionado"
          @atras="paso = 1"
        />
        <StepConfirmar
          v-if="paso === 3"
          :cliente="cliente!"
          :vehiculo="vehiculo!"
          :guardando="guardando"
          @confirmar="confirmarVenta"
          @atras="paso = 2"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAlertService } from '@/shared/alert/alert.service';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import StepCliente from '@/entities/venta/StepCliente.vue';
import StepVehiculo from '@/entities/venta/StepVehiculo.vue';
import StepConfirmar from '@/entities/venta/StepConfirmar.vue';
import type { ICliente } from '@/shared/model/cliente.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

const route = useRoute();
const router = useRouter();
const alertService = useAlertService();
const vehiculoService = new VehiculoService();

const paso = ref(1);
const cliente = ref<ICliente | null>(null);
const vehiculo = ref<IVehiculo | null>(null);
const guardando = ref(false);
const vehiculoIdFromQuery = computed(() => {
  const raw = route.query.vehiculoId;
  const value = Array.isArray(raw) ? raw[0] : raw;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
});

const tituloPaso = computed(
  () =>
    ({
      1: 'Seleccionar cliente',
      2: 'Seleccionar vehículo',
      3: 'Confirmar y registrar',
    })[paso.value],
);

function stepClass(n: number) {
  if (n < paso.value) return 'done';
  if (n === paso.value) return 'active';
  return '';
}

function onClienteSeleccionado(c: ICliente) {
  cliente.value = c;
  paso.value = 2;
}
function onVehiculoSeleccionado(v: IVehiculo) {
  vehiculo.value = v;
  paso.value = 3;
}

async function confirmarVenta() {
  if (!cliente.value?.id || !vehiculo.value?.id) return;
  guardando.value = true;
  try {
    await vehiculoService.vender(vehiculo.value.id, cliente.value.id);
    alertService.showSuccess('Venta registrada correctamente');
    router.push({ name: 'VentaList' });
  } catch (e: any) {
    alertService.showHttpError(e.response);
  } finally {
    guardando.value = false;
  }
}
</script>
