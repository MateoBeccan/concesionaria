import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type IMoneda, Moneda } from '@/shared/model/moneda.model';

import MonedaService from './moneda.service';

export default defineComponent({
  name: 'MonedaUpdate',
  setup() {
    const monedaService = inject('monedaService', () => new MonedaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const moneda: Ref<IMoneda> = ref(new Moneda());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveMoneda = async monedaId => {
      try {
        const res = await monedaService().find(monedaId);
        moneda.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.monedaId) {
      retrieveMoneda(route.params.monedaId);
    }

    const validations = useValidation();
    const validationRules = {
      codigo: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 2 caracteres.', 2),
        maxLength: validations.maxLength('Este campo no puede superar más de 10 caracteres.', 10),
      },
      descripcion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 50 caracteres.', 50),
      },
      simbolo: {
        maxLength: validations.maxLength('Este campo no puede superar más de 5 caracteres.', 5),
      },
      activo: {
        required: validations.required('Este campo es obligatorio.'),
      },
    };
    const v$ = useVuelidate(validationRules, moneda as any);
    v$.value.$validate();

    return {
      monedaService,
      alertService,
      moneda,
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
      if (this.moneda.id) {
        this.monedaService()
          .update(this.moneda)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Moneda is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.monedaService()
          .create(this.moneda)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Moneda is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
