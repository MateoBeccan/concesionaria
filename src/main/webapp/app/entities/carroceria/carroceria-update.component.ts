import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { Carroceria, type ICarroceria } from '@/shared/model/carroceria.model';

import CarroceriaService from './carroceria.service';

export default defineComponent({
  name: 'CarroceriaUpdate',
  setup() {
    const carroceriaService = inject('carroceriaService', () => new CarroceriaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const carroceria: Ref<ICarroceria> = ref(new Carroceria());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveCarroceria = async carroceriaId => {
      try {
        const res = await carroceriaService().find(carroceriaId);
        carroceria.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.carroceriaId) {
      retrieveCarroceria(route.params.carroceriaId);
    }

    const validations = useValidation();
    const validationRules = {
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 3 caracteres.', 3),
        maxLength: validations.maxLength('Este campo no puede superar más de 50 caracteres.', 50),
      },
      descripcion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
    };
    const v$ = useVuelidate(validationRules, carroceria as any);
    v$.value.$validate();

    return {
      carroceriaService,
      alertService,
      carroceria,
      previousState,
      isSaving,
      currentLanguage,
      v$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.carroceria.id) {
        this.carroceriaService()
          .update(this.carroceria)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Carroceria is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.carroceriaService()
          .create(this.carroceria)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Carroceria is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
