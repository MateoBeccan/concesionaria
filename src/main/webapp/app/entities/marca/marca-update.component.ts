import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { type IMarca, Marca } from '@/shared/model/marca.model';

import MarcaService from './marca.service';

export default defineComponent({
  name: 'MarcaUpdate',
  setup() {
    const marcaService = inject('marcaService', () => new MarcaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const marca: Ref<IMarca> = ref(new Marca());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveMarca = async marcaId => {
      try {
        const res = await marcaService().find(marcaId);
        res.createdDate = new Date(res.createdDate);
        res.lastModifiedDate = new Date(res.lastModifiedDate);
        marca.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.marcaId) {
      retrieveMarca(route.params.marcaId);
    }

    const validations = useValidation();
    const validationRules = {
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 2 caracteres.', 2),
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      paisOrigen: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      createdDate: {},
      lastModifiedDate: {},
    };
    const v$ = useVuelidate(validationRules, marca as any);
    v$.value.$validate();

    return {
      marcaService,
      alertService,
      marca,
      previousState,
      isSaving,
      currentLanguage,
      v$,
      ...useDateFormat({ entityRef: marca }),
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.marca.id) {
        this.marcaService()
          .update(this.marca)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Marca is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.marcaService()
          .create(this.marca)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Marca is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
