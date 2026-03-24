import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type IVersion, Version } from '@/shared/model/version.model';

import VersionService from './version.service';

export default defineComponent({
  name: 'VersionUpdate',
  setup() {
    const versionService = inject('versionService', () => new VersionService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const version: Ref<IVersion> = ref(new Version());
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

    const validations = useValidation();
    const validationRules = {
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
      },
      descripcion: {},
      anioInicio: {},
      anioFin: {},
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
