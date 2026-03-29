import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type IMetodoPago, MetodoPago } from '@/shared/model/metodo-pago.model';

import MetodoPagoService from './metodo-pago.service';

export default defineComponent({
  name: 'MetodoPagoUpdate',
  setup() {
    const metodoPagoService = inject('metodoPagoService', () => new MetodoPagoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const metodoPago: Ref<IMetodoPago> = ref(new MetodoPago());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveMetodoPago = async metodoPagoId => {
      try {
        const res = await metodoPagoService().find(metodoPagoId);
        metodoPago.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.metodoPagoId) {
      retrieveMetodoPago(route.params.metodoPagoId);
    }

    const validations = useValidation();
    const validationRules = {
      codigo: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 2 caracteres.', 2),
        maxLength: validations.maxLength('Este campo no puede superar más de 30 caracteres.', 30),
      },
      descripcion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      activo: {
        required: validations.required('Este campo es obligatorio.'),
      },
      requiereReferencia: {
        required: validations.required('Este campo es obligatorio.'),
      },
    };
    const v$ = useVuelidate(validationRules, metodoPago as any);
    v$.value.$validate();

    return {
      metodoPagoService,
      alertService,
      metodoPago,
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
      if (this.metodoPago.id) {
        this.metodoPagoService()
          .update(this.metodoPago)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A MetodoPago is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.metodoPagoService()
          .create(this.metodoPago)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A MetodoPago is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
