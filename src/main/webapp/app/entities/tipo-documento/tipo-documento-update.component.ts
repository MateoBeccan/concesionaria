import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type ITipoDocumento, TipoDocumento } from '@/shared/model/tipo-documento.model';

import TipoDocumentoService from './tipo-documento.service';

export default defineComponent({
  name: 'TipoDocumentoUpdate',
  setup() {
    const tipoDocumentoService = inject('tipoDocumentoService', () => new TipoDocumentoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const tipoDocumento: Ref<ITipoDocumento> = ref(new TipoDocumento());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveTipoDocumento = async tipoDocumentoId => {
      try {
        const res = await tipoDocumentoService().find(tipoDocumentoId);
        tipoDocumento.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.tipoDocumentoId) {
      retrieveTipoDocumento(route.params.tipoDocumentoId);
    }

    const validations = useValidation();
    const validationRules = {
      codigo: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 2 caracteres.', 2),
        maxLength: validations.maxLength('Este campo no puede superar más de 20 caracteres.', 20),
      },
      descripcion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
    };
    const v$ = useVuelidate(validationRules, tipoDocumento as any);
    v$.value.$validate();

    return {
      tipoDocumentoService,
      alertService,
      tipoDocumento,
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
      if (this.tipoDocumento.id) {
        this.tipoDocumentoService()
          .update(this.tipoDocumento)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A TipoDocumento is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.tipoDocumentoService()
          .create(this.tipoDocumento)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A TipoDocumento is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
