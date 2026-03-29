import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import ModeloService from '@/entities/modelo/modelo.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type IModelo } from '@/shared/model/modelo.model';
import { type IVersion, Version } from '@/shared/model/version.model';

import VersionService from './version.service';

export default defineComponent({
  name: 'VersionUpdate',
  setup() {
    const versionService = inject('versionService', () => new VersionService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const version: Ref<IVersion> = ref(new Version());

    const modeloService = inject('modeloService', () => new ModeloService());

    const modelos: Ref<IModelo[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveVersion = async versionId => {
      try {
        const res = await versionService().find(versionId);
        version.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.versionId) {
      retrieveVersion(route.params.versionId);
    }

    const initRelationships = () => {
      modeloService()
        .retrieve()
        .then(res => {
          modelos.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 1 caracteres.', 1),
        maxLength: validations.maxLength('Este campo no puede superar más de 50 caracteres.', 50),
      },
      descripcion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 150 caracteres.', 150),
      },
      anioInicio: {
        required: validations.required('Este campo es obligatorio.'),
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 1950.', 1950),
        max: validations.maxValue('Este campo no puede ser mayor que 2100.', 2100),
      },
      anioFin: {
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 1950.', 1950),
        max: validations.maxValue('Este campo no puede ser mayor que 2100.', 2100),
      },
      modelo: {},
    };
    const v$ = useVuelidate(validationRules, version as any);
    v$.value.$validate();

    return {
      versionService,
      alertService,
      version,
      previousState,
      isSaving,
      currentLanguage,
      modelos,
      v$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.version.id) {
        this.versionService()
          .update(this.version)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Version is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.versionService()
          .create(this.version)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Version is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
