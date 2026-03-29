import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import MotorService from '@/entities/motor/motor.service';
import TipoVehiculoService from '@/entities/tipo-vehiculo/tipo-vehiculo.service';
import VersionService from '@/entities/version/version.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
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

    const vehiculo: Ref<IVehiculo> = ref(new Vehiculo());

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

    const retrieveVehiculo = async vehiculoId => {
      try {
        const res = await vehiculoService().find(vehiculoId);
        res.createdDate = new Date(res.createdDate);
        res.lastModifiedDate = new Date(res.lastModifiedDate);
        vehiculo.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.vehiculoId) {
      retrieveVehiculo(route.params.vehiculoId);
    }

    const initRelationships = () => {
      versionService()
        .retrieve()
        .then(res => {
          versions.value = res.data;
        });
      motorService()
        .retrieve()
        .then(res => {
          motors.value = res.data;
        });
      tipoVehiculoService()
        .retrieve()
        .then(res => {
          tipoVehiculos.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      estado: {
        required: validations.required('Este campo es obligatorio.'),
      },
      fechaFabricacion: {
        required: validations.required('Este campo es obligatorio.'),
      },
      km: {
        required: validations.required('Este campo es obligatorio.'),
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
        max: validations.maxValue('Este campo no puede ser mayor que 1000000.', 1000000),
      },
      patente: {
        required: validations.required('Este campo es obligatorio.'),
      },
      precio: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      createdDate: {},
      lastModifiedDate: {},
      version: {},
      motor: {},
      tipoVehiculo: {},
      inventario: {},
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
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.vehiculo.id) {
        this.vehiculoService()
          .update(this.vehiculo)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Vehiculo is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.vehiculoService()
          .create(this.vehiculo)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Vehiculo is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
