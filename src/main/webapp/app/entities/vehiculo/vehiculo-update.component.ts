import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useVuelidate } from '@vuelidate/core';

import MotorService from '@/entities/motor/motor.service';
import TipoVehiculoService from '@/entities/tipo-vehiculo/tipo-vehiculo.service';
import VersionService from '@/entities/version/version.service';
import MarcaService from '@/entities/marca/marca.service';
import ModeloService from '@/entities/modelo/modelo.service';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { useDateFormat } from '@/shared/composables';
import { EstadoVehiculo } from '@/shared/model/enumerations/estado-vehiculo.model';

import { type IMotor } from '@/shared/model/motor.model';
import { type ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';
import { type IVehiculo, Vehiculo } from '@/shared/model/vehiculo.model';
import { type IVersion } from '@/shared/model/version.model';

import VehiculoService from './vehiculo.service';

export default defineComponent({
  name: 'VehiculoUpdate',

  setup() {
    // SERVICES
    const vehiculoService = inject('vehiculoService', () => new VehiculoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const versionService = inject('versionService', () => new VersionService());
    const motorService = inject('motorService', () => new MotorService());
    const tipoVehiculoService = inject('tipoVehiculoService', () => new TipoVehiculoService());
    const marcaService = inject('marcaService', () => new MarcaService());
    const modeloService = inject('modeloService', () => new ModeloService());

    // STATE
    const vehiculo: Ref<IVehiculo> = ref({
      ...new Vehiculo(),
      condicion: 'EN_VENTA',
    });

    const versions: Ref<IVersion[]> = ref([]);
    const motors: Ref<IMotor[]> = ref([]);
    const tipoVehiculos: Ref<ITipoVehiculo[]> = ref([]);

    const marcas = ref<any[]>([]);
    const modelos = ref<any[]>([]);

    const selectedMarca = ref<any>(null);
    const selectedModelo = ref<any>(null);

    const estadoVehiculoValues: Ref<string[]> = ref(Object.keys(EstadoVehiculo));
    const isSaving = ref(false);

    const route = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

    // COMPUTED
    const modelosFiltrados = computed(() =>
      modelos.value.filter(m => m.marca?.id === selectedMarca.value?.id)
    );

    const versionesFiltradas = computed(() =>
      versions.value.filter(v => v.modelo?.id === selectedModelo.value?.id)
    );

    // EVENTS
    function onMarcaChange() {
      selectedModelo.value = null;
      vehiculo.value.version = null;
    }

    function onModeloChange() {
      vehiculo.value.version = null;

      // auto tipo vehículo
      if (selectedModelo.value?.carroceria) {
        const nombre = selectedModelo.value.carroceria.nombre;

        if (nombre.includes('Pickup')) {
          vehiculo.value.tipoVehiculo = tipoVehiculos.value.find(t => t.nombre === 'Camioneta');
        } else if (nombre.includes('SUV') || nombre.includes('Crossover')) {
          vehiculo.value.tipoVehiculo = tipoVehiculos.value.find(t => t.nombre === 'SUV');
        } else {
          vehiculo.value.tipoVehiculo = tipoVehiculos.value.find(t => t.nombre === 'Auto');
        }
      }
    }

    // LOAD DATA
    const initRelationships = async () => {
      const [v, m, t] = await Promise.all([
        versionService().retrieve(),
        motorService().retrieve(),
        tipoVehiculoService().retrieve(),
      ]);

      versions.value = v.data;
      motors.value = m.data;
      tipoVehiculos.value = t.data;
    };

    const initExtraData = async () => {
      const [marcasRes, modelosRes] = await Promise.all([
        marcaService().retrieve(),
        modeloService().retrieve(),
      ]);

      marcas.value = marcasRes.data;
      modelos.value = modelosRes.data;
    };

    const retrieveVehiculo = async (vehiculoId: any) => {
      try {
        vehiculo.value = await vehiculoService().find(vehiculoId);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.vehiculoId) {
      retrieveVehiculo(route.params.vehiculoId);
    }

    initRelationships();
    initExtraData();

    // 🔥 VALIDATION
    const validations = useValidation();

    const validationRules = {
      estado: { required: validations.required('El estado es obligatorio.') },
      fechaFabricacion: { required: validations.required('La fecha es obligatoria.') },
      km: {
        required: validations.required('KM requerido'),
        integer: validations.integer('Debe ser entero'),
        min: validations.minValue('Min 0', 0),
      },
      patente: { required: validations.required('Patente obligatoria') },
      precio: {
        required: validations.required('Precio obligatorio'),
        min: validations.minValue('Debe ser mayor a 0', 0),
      },
    };

    const v$ = useVuelidate(validationRules, vehiculo as any);

    // 🔥 RETURN
    return {
      vehiculo,
      vehiculoService,
      alertService,
      previousState,
      estadoVehiculoValues,
      isSaving,

      versions,
      motors,
      tipoVehiculos,

      marcas,
      modelos,
      selectedMarca,
      selectedModelo,
      modelosFiltrados,
      versionesFiltradas,

      onMarcaChange,
      onModeloChange,

      v$,
      ...useDateFormat({ entityRef: vehiculo }),
    };
  },

  methods: {
    async save(): Promise<void> {
      this.v$.$touch();
      if (this.v$.$invalid) return;

      this.isSaving = true;

      try {
        const payload = {
          ...this.vehiculo,
          version: this.vehiculo.version ? { id: this.vehiculo.version.id } : null,
          motor: this.vehiculo.motor ? { id: this.vehiculo.motor.id } : null,
          tipoVehiculo: this.vehiculo.tipoVehiculo ? { id: this.vehiculo.tipoVehiculo.id } : null,
          condicion: this.vehiculo.condicion || 'EN_VENTA',
        };

        const result = this.vehiculo.id
          ? await this.vehiculoService().update(payload)
          : await this.vehiculoService().create(payload);

        this.alertService.showSuccess(
          this.vehiculo.id
            ? `Vehículo ${result.patente} actualizado`
            : `Vehículo ${result.patente} creado`
        );

        this.previousState();
      } catch (error: any) {
        this.alertService.showHttpError(error.response);
      } finally {
        this.isSaving = false;
      }
    },
  },
});
