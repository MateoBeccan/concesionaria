import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { CondicionIva, type ICondicionIva } from '@/shared/model/condicion-iva.model';

import CondicionIvaService from './condicion-iva.service';

export default defineComponent({
  name: 'CondicionIvaUpdate',
  setup() {
    const condicionIvaService = inject('condicionIvaService', () => new CondicionIvaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const condicionIva: Ref<ICondicionIva> = ref(new CondicionIva());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveCondicionIva = async condicionIvaId => {
      try {
        const res = await condicionIvaService().find(condicionIvaId);
        condicionIva.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.condicionIvaId) {
      retrieveCondicionIva(route.params.condicionIvaId);
    }

    const validations = useValidation();
    const validationRules = {
      codigo: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 3 caracteres.', 3),
        maxLength: validations.maxLength('Este campo no puede superar más de 50 caracteres.', 50),
      },
      descripcion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
    };
    const v$ = useVuelidate(validationRules, condicionIva as any);
    v$.value.$validate();

    return {
      condicionIvaService,
      alertService,
      condicionIva,
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
      if (this.condicionIva.id) {
        this.condicionIvaService()
          .update(this.condicionIva)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A CondicionIva is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.condicionIvaService()
          .create(this.condicionIva)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A CondicionIva is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
