import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type ITipoCaja, TipoCaja } from '@/shared/model/tipo-caja.model';

import TipoCajaService from './tipo-caja.service';

export default defineComponent({
  name: 'TipoCajaUpdate',
  setup() {
    const tipoCajaService = inject('tipoCajaService', () => new TipoCajaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const tipoCaja: Ref<ITipoCaja> = ref(new TipoCaja());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveTipoCaja = async tipoCajaId => {
      try {
        const res = await tipoCajaService().find(tipoCajaId);
        tipoCaja.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.tipoCajaId) {
      retrieveTipoCaja(route.params.tipoCajaId);
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
    const v$ = useVuelidate(validationRules, tipoCaja as any);
    v$.value.$validate();

    return {
      tipoCajaService,
      alertService,
      tipoCaja,
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
      if (this.tipoCaja.id) {
        this.tipoCajaService()
          .update(this.tipoCaja)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A TipoCaja is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.tipoCajaService()
          .create(this.tipoCaja)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A TipoCaja is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
