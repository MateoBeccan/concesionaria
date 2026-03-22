import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import MotorService from '@/entities/motor/motor.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { Combustible, type ICombustible } from '@/shared/model/combustible.model';
import { type IMotor } from '@/shared/model/motor.model';

import CombustibleService from './combustible.service';

export default defineComponent({
  name: 'CombustibleUpdate',
  setup() {
    const combustibleService = inject('combustibleService', () => new CombustibleService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const combustible: Ref<ICombustible> = ref(new Combustible());

    const motorService = inject('motorService', () => new MotorService());

    const motors: Ref<IMotor[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveCombustible = async combustibleId => {
      try {
        const res = await combustibleService().find(combustibleId);
        combustible.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.combustibleId) {
      retrieveCombustible(route.params.combustibleId);
    }

    const initRelationships = () => {
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
      motor: {},
    };
    const v$ = useVuelidate(validationRules, combustible as any);
    v$.value.$validate();

    return {
      combustibleService,
      alertService,
      combustible,
      previousState,
      isSaving,
      currentLanguage,
      motors,
      v$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.combustible.id) {
        this.combustibleService()
          .update(this.combustible)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Combustible is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.combustibleService()
          .create(this.combustible)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Combustible is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
