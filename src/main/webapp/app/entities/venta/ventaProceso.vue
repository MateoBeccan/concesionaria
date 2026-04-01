<template>
  <div class="container">

    <h2>Proceso de Venta</h2>

    <!-- LOADING -->
    <div v-if="loading">Cargando...</div>

    <!-- VEHICULO -->
    <div v-if="vehiculo" class="card p-3 mb-3">
      <h4>{{ vehiculo.patente }}</h4>
      <p>
        Precio: $
        {{ Number(vehiculo.precio ?? 0).toLocaleString('es-AR') }}
      </p>

      <span
        class="badge"
        :class="{
          'bg-success': vehiculo.estado === 'NUEVO',
          'bg-secondary': vehiculo.estado === 'USADO',
          'bg-danger': vehiculo.condicion === 'VENDIDO'
        }"
      >
        {{ vehiculo.condicion }}
      </span>
    </div>

    <!-- CLIENTE -->
    <div class="card p-3 mb-3">
      <h4>Cliente</h4>

      <input
        v-model="nroDocumento"
        placeholder="Buscar por Documento"
        class="form-control mb-2"
      />

      <button class="btn btn-secondary" @click="buscarCliente">
        Buscar Cliente
      </button>

      <div v-if="cliente" class="mt-2">
        <strong>{{ cliente.nombre }} {{ cliente.apellido }}</strong>
      </div>
    </div>

    <!-- CONFIRMAR -->
    <button
      class="btn btn-success"
      :disabled="!cliente || !vehiculo || vehiculo.condicion === 'VENDIDO'"
      @click="confirmarVenta"
    >
      Confirmar Venta
    </button>

  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import ClienteService from '@/entities/cliente/cliente.service';
import { type IVehiculo } from '@/shared/model/vehiculo.model';
import { type ICliente } from '@/shared/model/cliente.model';

export default defineComponent({
  name: 'VentaProceso',

  setup() {
    const route = useRoute();
    const router = useRouter();

    const vehiculoService = new VehiculoService();
    const clienteService = new ClienteService();

    const vehiculo = ref<IVehiculo | null>(null);
    const cliente = ref<ICliente | null>(null);
    const nroDocumento = ref('');
    const loading = ref(false);

    const vehiculoId = Number(route.params.vehiculoId);

    // 🔥 cargar vehículo
    onMounted(async () => {
      loading.value = true;

      try {
        const res = await vehiculoService.find(vehiculoId);
        vehiculo.value = res;
      } catch (e) {
        console.error('Error cargando vehículo', e);
      } finally {
        loading.value = false;
      }
    });

    // 🔍 buscar cliente
    const buscarCliente = async () => {
      if (!nroDocumento.value) return;

      try {
        const res = await clienteService.findByDocumento(nroDocumento.value);
        cliente.value = res;
      } catch (e) {
        console.warn('Cliente no encontrado');
        cliente.value = null;
      }
    };

    // 💰 confirmar venta
    const confirmarVenta = async () => {
      if (!vehiculo.value || !cliente.value) return;

      if (vehiculo.value.condicion === 'VENDIDO') {
        console.warn('El vehículo ya fue vendido');
        return;
      }

      try {
        await vehiculoService.vender(
          vehiculo.value.id!,
          cliente.value.id!
        );

        console.log('Venta realizada con éxito');

        router.push('/vehiculos');
      } catch (e) {
        console.error('Error al realizar la venta', e);
      }
    };

    return {
      vehiculo,
      cliente,
      nroDocumento,
      loading,
      buscarCliente,
      confirmarVenta,
    };
  },
});
</script>
