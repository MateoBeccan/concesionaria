import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import MarcaService from '@/entities/marca/marca.service';
import ModeloService from '@/entities/modelo/modelo.service';
import MotorService from '@/entities/motor/motor.service';
import VersionService from '@/entities/version/version.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { Auto, type IAuto } from '@/shared/model/auto.model';
import { CondicionAuto } from '@/shared/model/enumerations/condicion-auto.model';
import { EstadoAuto } from '@/shared/model/enumerations/estado-auto.model';
import { type IMarca } from '@/shared/model/marca.model';
import { type IModelo } from '@/shared/model/modelo.model';
import { type IMotor } from '@/shared/model/motor.model';
import { type IVersion } from '@/shared/model/version.model';

import AutoService from './auto.service';

export default defineComponent({
  name: 'AutoUpdate',
  setup() {
    const autoService = inject('autoService', () => new AutoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const auto: Ref<IAuto> = ref(new Auto());

    const marcaService = inject('marcaService', () => new MarcaService());

    const marcas: Ref<IMarca[]> = ref([]);

    const modeloService = inject('modeloService', () => new ModeloService());

    const modelos: Ref<IModelo[]> = ref([]);

    const versionService = inject('versionService', () => new VersionService());

    const versions: Ref<IVersion[]> = ref([]);

    const motorService = inject('motorService', () => new MotorService());

    const motors: Ref<IMotor[]> = ref([]);
    const estadoAutoValues: Ref<string[]> = ref(Object.keys(EstadoAuto));
    const condicionAutoValues: Ref<string[]> = ref(Object.keys(CondicionAuto));
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveAuto = async autoId => {
      try {
        const res = await autoService().find(autoId);
        auto.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.autoId) {
      retrieveAuto(route.params.autoId);
    }

    const initRelationships = () => {
      marcaService()
        .retrieve()
        .then(res => {
          marcas.value = res.data;
        });
      modeloService()
        .retrieve()
        .then(res => {
          modelos.value = res.data;
        });
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
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      estado: {
        required: validations.required('Este campo es obligatorio.'),
      },
      condicion: {
        required: validations.required('Este campo es obligatorio.'),
      },
      fechaFabricacion: {},
      km: {},
      patente: {},
      precio: {},
      marca: {},
      modelo: {},
      version: {},
      motor: {},
    };
    const v$ = useVuelidate(validationRules, auto as any);
    v$.value.$validate();

    return {
      autoService,
      alertService,
      auto,
      previousState,
      estadoAutoValues,
      condicionAutoValues,
      isSaving,
      currentLanguage,
      marcas,
      modelos,
      versions,
      motors,
      v$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.auto.id) {
        this.autoService()
          .update(this.auto)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Auto is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.autoService()
          .create(this.auto)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Auto is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
