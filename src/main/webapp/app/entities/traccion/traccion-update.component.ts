import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type ITraccion, Traccion } from '@/shared/model/traccion.model';

import TraccionService from './traccion.service';

export default defineComponent({
  name: 'TraccionUpdate',
  setup() {
    const traccionService = inject('traccionService', () => new TraccionService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const traccion: Ref<ITraccion> = ref(new Traccion());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveTraccion = async traccionId => {
      try {
        const res = await traccionService().find(traccionId);
        traccion.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.traccionId) {
      retrieveTraccion(route.params.traccionId);
    }

    const validations = useValidation();
    const validationRules = {
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 2 caracteres.', 2),
        maxLength: validations.maxLength('Este campo no puede superar más de 50 caracteres.', 50),
      },
      descripcion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
    };
    const v$ = useVuelidate(validationRules, traccion as any);
    v$.value.$validate();

    return {
      traccionService,
      alertService,
      traccion,
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
      if (this.traccion.id) {
        this.traccionService()
          .update(this.traccion)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Traccion is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.traccionService()
          .create(this.traccion)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Traccion is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
