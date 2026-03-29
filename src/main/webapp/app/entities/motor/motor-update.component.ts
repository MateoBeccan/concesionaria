import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import CombustibleService from '@/entities/combustible/combustible.service';
import TipoCajaService from '@/entities/tipo-caja/tipo-caja.service';
import TraccionService from '@/entities/traccion/traccion.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type ICombustible } from '@/shared/model/combustible.model';
import { type IMotor, Motor } from '@/shared/model/motor.model';
import { type ITipoCaja } from '@/shared/model/tipo-caja.model';
import { type ITraccion } from '@/shared/model/traccion.model';

import MotorService from './motor.service';

export default defineComponent({
  name: 'MotorUpdate',
  setup() {
    const motorService = inject('motorService', () => new MotorService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const motor: Ref<IMotor> = ref(new Motor());

    const combustibleService = inject('combustibleService', () => new CombustibleService());

    const combustibles: Ref<ICombustible[]> = ref([]);

    const tipoCajaService = inject('tipoCajaService', () => new TipoCajaService());

    const tipoCajas: Ref<ITipoCaja[]> = ref([]);

    const traccionService = inject('traccionService', () => new TraccionService());

    const traccions: Ref<ITraccion[]> = ref([]);
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
      combustibleService()
        .retrieve()
        .then(res => {
          combustibles.value = res.data;
        });
      tipoCajaService()
        .retrieve()
        .then(res => {
          tipoCajas.value = res.data;
        });
      traccionService()
        .retrieve()
        .then(res => {
          traccions.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 2 caracteres.', 2),
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      cilindradaCc: {
        required: validations.required('Este campo es obligatorio.'),
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 50.', 50),
        max: validations.maxValue('Este campo no puede ser mayor que 10000.', 10000),
      },
      cilindroCant: {
        required: validations.required('Este campo es obligatorio.'),
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 1.', 1),
        max: validations.maxValue('Este campo no puede ser mayor que 16.', 16),
      },
      potenciaHp: {
        required: validations.required('Este campo es obligatorio.'),
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 1.', 1),
        max: validations.maxValue('Este campo no puede ser mayor que 2000.', 2000),
      },
      turbo: {
        required: validations.required('Este campo es obligatorio.'),
      },
      combustible: {},
      tipoCaja: {},
      traccion: {},
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
      combustibles,
      tipoCajas,
      traccions,
      v$,
    };
  },
  created(): void {},
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
  },
});
