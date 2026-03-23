import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import VersionService from '@/entities/version/version.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type IMotor, Motor } from '@/shared/model/motor.model';
import { type IVersion } from '@/shared/model/version.model';

import MotorService from './motor.service';

export default defineComponent({
  name: 'MotorUpdate',
  setup() {
    const motorService = inject('motorService', () => new MotorService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const motor: Ref<IMotor> = ref(new Motor());

    const versionService = inject('versionService', () => new VersionService());

    const versions: Ref<IVersion[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveMotor = async motorId => {
      try {
        const res = await motorService().find(motorId);
        motor.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.motorId) {
      retrieveMotor(route.params.motorId);
    }

    const initRelationships = () => {
      versionService()
        .retrieve()
        .then(res => {
          versions.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
      },
      cilindradaCc: {
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      cilindroCant: {
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      potenciaHp: {
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      turbo: {},
      versioneses: {},
      combustibleses: {},
    };
    const v$ = useVuelidate(validationRules, motor as any);
    v$.value.$validate();

    return {
      motorService,
      alertService,
      motor,
      previousState,
      isSaving,
      currentLanguage,
      versions,
      v$,
    };
  },
  created(): void {
    this.motor.versioneses = [];
  },
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.motor.id) {
        this.motorService()
          .update(this.motor)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Motor is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.motorService()
          .create(this.motor)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Motor is created with identifier ${param.id}`);
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
