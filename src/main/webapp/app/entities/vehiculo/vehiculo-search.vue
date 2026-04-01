<template>
  <div class="container">

    <h2>Buscar Vehículo</h2>

    <input
      v-model="patente"
      placeholder="Ingrese patente"
      class="form-control"
    />

    <button @click="buscar" class="btn btn-primary mt-2">
      Buscar
    </button>

    <div v-if="loading">Buscando...</div>

    <hr />

    <!-- EXISTENTE -->
    <div v-if="modo === 'EXISTENTE' && vehiculo">
      <h4 class="mb-3">Vehículo encontrado</h4>

      <div class="card shadow p-4">

        <!-- HEADER -->
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h2>{{ vehiculo.patente }}</h2>

          <span
            class="badge"
            :class="{
              'bg-success': vehiculo.estado === 'NUEVO',
              'bg-secondary': vehiculo.estado === 'USADO',
              'bg-danger': vehiculo.condicion === 'VENDIDO'
            }"
          >
            {{ vehiculo.estado }}
          </span>
        </div>

        <!-- PRECIO -->
        <h3 class="text-primary mb-4">
          $ {{ Number(vehiculo.precio ?? 0).toLocaleString('es-AR') }}
        </h3>

        <!-- INFO -->
        <div class="row">

          <!-- IZQUIERDA -->
          <div class="col-md-6">
            <p><b>Kilómetros:</b> {{ vehiculo.km ?? 0 }} km</p>
            <p><b>Fecha fabricación:</b> {{ formatearFecha(vehiculo.fechaFabricacion) }}</p>
            <p><b>Tipo:</b> {{ vehiculo.tipoVehiculo?.nombre ?? '-' }}</p>
          </div>

          <!-- DERECHA -->
          <div class="col-md-6">
            <p><b>Motor:</b> {{ vehiculo.motor?.nombre ?? '-' }}</p>
            <p><b>Potencia:</b> {{ vehiculo.motor?.potenciaHp ?? '-' }} HP</p>
            <p><b>Versión:</b> {{ vehiculo.version?.nombre ?? '-' }}</p>
            <p><b>Modelo:</b> {{ vehiculo.version?.modelo?.nombre ?? '-' }}</p>
            <p><b>Marca:</b> {{ vehiculo.version?.modelo?.marca?.nombre ?? '-' }}</p>
          </div>

        </div>

      </div>

      <!-- ACCIONES -->
      <div class="mt-4 d-flex gap-2">

        <!-- VENDER -->
        <button
          class="btn btn-success"
          @click="irAVenta"
          v-if="vehiculo.condicion !== 'VENDIDO'"
        >
          💰 Vender
        </button>

        <!-- RESERVAR -->
        <button
          class="btn btn-warning"
          @click="reservar"
          v-if="vehiculo.condicion !== 'VENDIDO'"
        >
          🟡 Reservar
        </button>

        <!-- EDITAR -->
        <button
          class="btn btn-primary"
          @click="editar"
        >
          ✏️ Editar
        </button>

      </div>
    </div>

    <!-- CREAR -->
    <div v-if="modo === 'CREAR'">
      <h4>No existe → Crear vehículo</h4>

      <vehiculo-form
        :vehiculo="vehiculo"
        @guardado="onVehiculoCreado"
      />
    </div>

  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import { type IVehiculo } from '@/shared/model/vehiculo.model';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'VehiculoSearch',

  setup() {
    const vehiculoService = new VehiculoService();
    const router = useRouter();

    const patente = ref('');
    const vehiculo = ref<IVehiculo | null>(null);
    const modo = ref<'BUSCAR' | 'EXISTENTE' | 'CREAR'>('BUSCAR');
    const loading = ref(false);

    // 🔍 BUSCAR VEHICULO
    const buscar = async () => {
      if (!patente.value) return;

      const patenteNormalizada = patente.value.trim().toUpperCase();

      if (!/^[A-Z0-9]{6,7}$/.test(patenteNormalizada)) {
        console.warn('Patente inválida');
        return;
      }

      loading.value = true;

      try {
        const res = await vehiculoService.findByPatente(patenteNormalizada);
        vehiculo.value = res;
        modo.value = 'EXISTENTE';
      } catch (err) {
        modo.value = 'CREAR';
        vehiculo.value = { patente: patenteNormalizada } as IVehiculo;
      } finally {
        loading.value = false;
      }
    };

    const onVehiculoCreado = (v: IVehiculo) => {
      vehiculo.value = v;
      modo.value = 'EXISTENTE';
    };

    const irAVenta = () => {
      if (!vehiculo.value?.id) return;

      router.push({
        name: 'VentaProceso',
        params: { vehiculoId: vehiculo.value.id },
      });
    };

    const reservar = () => {
      console.log('Reservar vehículo', vehiculo.value?.id);
    };

    const editar = () => {
      if (!vehiculo.value?.id) return;

      router.push({
        name: 'VehiculoEdit',
        params: { vehiculoId: vehiculo.value.id },
      });
    };

    // ✅ CORREGIDO
    const formatearFecha = (fecha?: Date | string) => {
      if (!fecha) return '-';

      const f = typeof fecha === 'string' ? new Date(fecha) : fecha;
      return f.toLocaleDateString('es-AR');
    };

    return {
      patente,
      vehiculo,
      modo,
      buscar,
      loading,
      onVehiculoCreado,
      irAVenta,
      reservar,
      editar,
      formatearFecha,
    };
  },
});
</script>
