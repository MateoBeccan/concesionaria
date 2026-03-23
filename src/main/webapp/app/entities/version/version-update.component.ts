import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import ModeloService from '@/entities/modelo/modelo.service';
import MotorService from '@/entities/motor/motor.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type IModelo } from '@/shared/model/modelo.model';
import { type IMotor } from '@/shared/model/motor.model';
import { type IVersion, Version } from '@/shared/model/version.model';

import VersionService from './version.service';

export default defineComponent({
  name: 'VersionUpdate',
  setup() {
    const versionService = inject('versionService', () => new VersionService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const version: Ref<IVersion> = ref(new Version());

    const modeloService = inject('modeloService', () => new ModeloService());

    const modelos: Ref<IModelo[]> = ref([]);

    const motorService = inject('motorService', () => new MotorService());

    const motors: Ref<IMotor[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveVersion = async versionId => {
      try {
        const res = await versionService().find(versionId);
        version.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.versionId) {
      retrieveVersion(route.params.versionId);
    }

    const initRelationships = () => {
      modeloService()
        .retrieve()
        .then(res => {
          modelos.value = res.data;
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
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
      },
      descripcion: {},
      anioInicio: {},
      anioFin: {},
      modeloses: {},
      motoreses: {},
    };
    const v$ = useVuelidate(validationRules, version as any);
    v$.value.$validate();

    return {
      versionService,
      alertService,
      version,
      previousState,
      isSaving,
      currentLanguage,
      modelos,
      motors,
      v$,
    };
  },
  created(): void {
    this.version.modeloses = [];
    this.version.motoreses = [];
  },
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.version.id) {
        this.versionService()
          .update(this.version)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Version is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.versionService()
          .create(this.version)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Version is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },

    getSelected(selectedVals, option, pkField = 'id'): any {
      if (selectedVals) {
        return selectedVals.find(value => option[pkField] === value[pkField]) ?? option;
      }
      return option;
    },
  },
});
