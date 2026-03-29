import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { Combustible, type ICombustible } from '@/shared/model/combustible.model';

import CombustibleService from './combustible.service';

export default defineComponent({
  name: 'CombustibleUpdate',
  setup() {
    const combustibleService = inject('combustibleService', () => new CombustibleService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const combustible: Ref<ICombustible> = ref(new Combustible());
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
    const v$ = useVuelidate(validationRules, combustible as any);
    v$.value.$validate();

    return {
      combustibleService,
      alertService,
      combustible,
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
