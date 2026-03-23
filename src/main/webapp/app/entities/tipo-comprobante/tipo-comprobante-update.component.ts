import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type ITipoComprobante, TipoComprobante } from '@/shared/model/tipo-comprobante.model';

import TipoComprobanteService from './tipo-comprobante.service';

export default defineComponent({
  name: 'TipoComprobanteUpdate',
  setup() {
    const tipoComprobanteService = inject('tipoComprobanteService', () => new TipoComprobanteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const tipoComprobante: Ref<ITipoComprobante> = ref(new TipoComprobante());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveTipoComprobante = async tipoComprobanteId => {
      try {
        const res = await tipoComprobanteService().find(tipoComprobanteId);
        tipoComprobante.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.tipoComprobanteId) {
      retrieveTipoComprobante(route.params.tipoComprobanteId);
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
    };
    const v$ = useVuelidate(validationRules, tipoComprobante as any);
    v$.value.$validate();

    return {
      tipoComprobanteService,
      alertService,
      tipoComprobante,
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
      if (this.tipoComprobante.id) {
        this.tipoComprobanteService()
          .update(this.tipoComprobante)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A TipoComprobante is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.tipoComprobanteService()
          .create(this.tipoComprobante)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A TipoComprobante is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
