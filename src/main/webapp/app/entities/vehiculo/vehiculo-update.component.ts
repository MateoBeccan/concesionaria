import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import MotorService from '@/entities/motor/motor.service';
import TipoVehiculoService from '@/entities/tipo-vehiculo/tipo-vehiculo.service';
import VersionService from '@/entities/version/version.service';
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
    const vehiculoService = inject('vehiculoService', () => new VehiculoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const vehiculo: Ref<IVehiculo> = ref({
      ...new Vehiculo(),
      condicion: 'EN_VENTA',
    });
    const versionService = inject('versionService', () => new VersionService());
    const versions: Ref<IVersion[]> = ref([]);
    const motorService = inject('motorService', () => new MotorService());
    const motors: Ref<IMotor[]> = ref([]);
    const tipoVehiculoService = inject('tipoVehiculoService', () => new TipoVehiculoService());
    const tipoVehiculos: Ref<ITipoVehiculo[]> = ref([]);
    const estadoVehiculoValues: Ref<string[]> = ref(Object.keys(EstadoVehiculo));
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

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

    const initRelationships = () => {
      versionService().retrieve().then(res => { versions.value = res.data; });
      motorService().retrieve().then(res => { motors.value = res.data; });
      tipoVehiculoService().retrieve().then(res => { tipoVehiculos.value = res.data; });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      estado:           { required: validations.required('El estado es obligatorio.') },
      fechaFabricacion: { required: validations.required('La fecha de fabricación es obligatoria.') },
      km: {
        required: validations.required('Los kilómetros son obligatorios.'),
        integer:  validations.integer('Debe ser un número entero.'),
        min:      validations.minValue('Debe ser mayor o igual a 0.', 0),
        max:      validations.maxValue('No puede superar 1.000.000 km.', 1000000),
      },
      patente:  { required: validations.required('La patente es obligatoria.') },
      precio:   { required: validations.required('El precio es obligatorio.'), min: validations.minValue('Debe ser mayor que 0.', 0) },
      version:  {},
      motor:    {},
      tipoVehiculo: {},
    };

    const v$ = useVuelidate(validationRules, vehiculo as any);
    v$.value.$validate();

    return {
      vehiculoService,
      alertService,
      vehiculo,
      previousState,
      estadoVehiculoValues,
      isSaving,
      currentLanguage,
      versions,
      motors,
      tipoVehiculos,
      v$,
      ...useDateFormat({ entityRef: vehiculo }),
    };
  },
  methods: {
    save(): void {
      this.isSaving = true;
      if (!this.vehiculo.condicion) {
        this.vehiculo.condicion = 'EN_VENTA';
      }

      const op = this.vehiculo.id
        ? this.vehiculoService().update(this.vehiculo)
        : this.vehiculoService().create(this.vehiculo);

      op.then(param => {
        this.isSaving = false;
        this.previousState();
        this.alertService.showSuccess(
          this.vehiculo.id
            ? `Vehículo ${param.patente} actualizado correctamente`
            : `Vehículo ${param.patente} creado correctamente`,
        );
      }).catch(error => {
        this.isSaving = false;
        this.alertService.showHttpError(error.response);
      });
    },
  },
});
