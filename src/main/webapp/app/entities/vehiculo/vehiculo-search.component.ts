import { defineComponent, ref } from 'vue';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import { type IVehiculo } from '@/shared/model/vehiculo.model';

export default defineComponent({
  name: 'VehiculoSearch',

  setup() {
    const vehiculoService = new VehiculoService();

    const patente = ref('');
    const vehiculo = ref<IVehiculo | null>(null);
    const modo = ref<'BUSCAR' | 'EXISTENTE' | 'CREAR'>('BUSCAR');
    const loading = ref(false);

    const buscar = async () => {
      if (!patente.value) return;

      loading.value = true;

      try {
        const res = await vehiculoService.findByPatente(patente.value);
        vehiculo.value = res;
        modo.value = 'EXISTENTE';
      } catch (err) {
        modo.value = 'CREAR';
        vehiculo.value = { patente: patente.value } as IVehiculo;
      } finally {
        loading.value = false;
      }
    };

    const onVehiculoCreado = (v: IVehiculo) => {
      vehiculo.value = v;
      modo.value = 'EXISTENTE';
    };

    return {
      patente,
      vehiculo,
      modo,
      buscar,
      loading,
      onVehiculoCreado,
    };
  },
});
