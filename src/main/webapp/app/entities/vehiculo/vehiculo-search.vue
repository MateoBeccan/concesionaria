<template>
  <div class="container py-4" style="max-width: 680px">
    <h4 class="fw-semibold mb-1">Buscar vehículo</h4>
    <p class="text-muted small mb-4">Ingresá la patente para buscar o registrar un vehículo.</p>

    <div class="d-flex gap-2 mb-3">
      <div class="flex-grow-1">
        <EntitySearchInput
          v-model="patente"
          placeholder="Ej: ABC123 o AB123CD"
          :loading="loading"
          :error="error"
          :debounce="0"
          @clear="limpiar"
        />
      </div>
      <button class="btn btn-primary" @click="buscar" :disabled="loading || !patente">Buscar</button>
    </div>

    <!-- VEHÍCULO ENCONTRADO -->
    <div v-if="vehiculo && modo === 'EXISTENTE'" class="card border-0 shadow-sm">
      <div class="card-body p-4">
        <div class="d-flex justify-content-between align-items-start mb-3">
          <h3 class="fw-bold mb-0">{{ vehiculo.patente }}</h3>
          <div class="d-flex gap-2">
            <span class="badge fs-6" :class="vehiculo.estado === 'NUEVO' ? 'bg-success' : 'bg-secondary'">
              {{ vehiculo.estado }}
            </span>
            <span class="badge fs-6" :class="badgeCondicion(vehiculo.condicion)">
              {{ vehiculo.condicion }}
            </span>
          </div>
        </div>

        <h3 class="text-primary mb-4">$ {{ formatPrecio(vehiculo.precio) }}</h3>

        <div class="row small">
          <div class="col-md-6">
            <p class="mb-1"><strong>Kilómetros:</strong> {{ vehiculo.km?.toLocaleString('es-AR') ?? '-' }} km</p>
            <p class="mb-1"><strong>Fabricación:</strong> {{ formatFecha(vehiculo.fechaFabricacion) }}</p>
            <p class="mb-1"><strong>Tipo:</strong> {{ vehiculo.tipoVehiculo?.nombre ?? '-' }}</p>
          </div>
          <div class="col-md-6">
            <p class="mb-1"><strong>Motor:</strong> {{ vehiculo.motor?.nombre ?? '-' }}</p>
            <p class="mb-1"><strong>Potencia:</strong> {{ vehiculo.motor?.potenciaHp ?? '-' }} HP</p>
            <p class="mb-1"><strong>Versión:</strong> {{ vehiculo.version?.nombre ?? '-' }}</p>
            <p class="mb-1"><strong>Marca:</strong> {{ vehiculo.version?.modelo?.marca?.nombre ?? '-' }}</p>
          </div>
        </div>

        <div class="d-flex gap-2 mt-4 flex-wrap">
          <button v-if="vehiculo.condicion !== 'VENDIDO'" class="btn btn-success" @click="irAVenta">💰 Vender</button>
          <button v-if="vehiculo.condicion === 'EN_VENTA'" class="btn btn-warning" @click="reservar">🟡 Reservar</button>
          <button class="btn btn-outline-primary" @click="editar">✏️ Editar</button>
        </div>
      </div>
    </div>

    <!-- NO ENCONTRADO -->
    <div v-if="modo === 'NO_ENCONTRADO'" class="alert alert-warning d-flex justify-content-between align-items-center">
      <div>
        <strong>Vehículo no encontrado</strong>
        <p class="mb-0 small">No existe ningún vehículo con patente "{{ patente }}"</p>
      </div>
      <button class="btn btn-success btn-sm" @click="modo = 'CREAR'">+ Registrar vehículo</button>
    </div>

    <!-- CREAR -->
    <div v-if="modo === 'CREAR'">
      <VehiculoQuickCreate :patente-inicial="patente" @guardado="onVehiculoCreado" @cerrar="modo = 'BUSCAR'" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useVehiculo } from '@/shared/composables/useVehiculo';
import { useAlertService } from '@/shared/alert/alert.service';
import EntitySearchInput from '@/shared/composables/EntitySearchInput.vue';
import VehiculoQuickCreate from '@/entities/vehiculo/VehiculoQuickCreate.vue';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

const router = useRouter();
const alertService = useAlertService();
const { vehiculo, loading, error, notFound, buscarPorPatente, setVehiculo, limpiar: limpiarComposable } = useVehiculo();

const patente = ref('');
const modo = ref<'BUSCAR' | 'EXISTENTE' | 'NO_ENCONTRADO' | 'CREAR'>('BUSCAR');

watch(vehiculo, v => {
  if (v) modo.value = 'EXISTENTE';
});
watch(notFound, v => {
  if (v) modo.value = 'NO_ENCONTRADO';
});

async function buscar() {
  await buscarPorPatente(patente.value);
}

function limpiar() {
  limpiarComposable();
  modo.value = 'BUSCAR';
}

function onVehiculoCreado(v: IVehiculo) {
  setVehiculo(v);
  patente.value = v.patente ?? '';
  modo.value = 'EXISTENTE';
  alertService.showSuccess('Vehículo registrado correctamente');
}

function irAVenta() {
  if (!vehiculo.value?.id) return;

  router.push({
    name: 'VentaEditor',
    query: { vehiculoId: vehiculo.value.id },
  });
}
import VehiculoService from '@/entities/vehiculo/vehiculo.service';

const vehiculoService = new VehiculoService();
async function reservar() {
  if (!vehiculo.value?.id) return;

  const clienteId = window.prompt('Ingresá ID del cliente');

  if (!clienteId || isNaN(Number(clienteId))) {
    alertService.showError('ID de cliente inválido');
    return;
  }

  try {
    await vehiculoService.reservar(vehiculo.value.id, Number(clienteId));

    alertService.showSuccess('Vehículo reservado correctamente');

    await buscar(); // refresca el estado del vehículo
  } catch (e: any) {
    alertService.showHttpError(e.response);
  }
}

function editar() {
  if (!vehiculo.value?.id) return;
  router.push({ name: 'VehiculoEdit', params: { vehiculoId: vehiculo.value.id } });
}

function badgeCondicion(condicion?: string) {
  const map: Record<string, string> = { EN_VENTA: 'bg-primary', RESERVADO: 'bg-warning text-dark', VENDIDO: 'bg-danger' };
  return map[condicion ?? ''] ?? 'bg-light text-dark border';
}

function formatPrecio(precio?: number | null) {
  return Number(precio ?? 0).toLocaleString('es-AR');
}

function formatFecha(fecha?: Date | string) {
  if (!fecha) return '-';
  return new Date(fecha).toLocaleDateString('es-AR');
}
</script>
