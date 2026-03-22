import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type IPrueba1, Prueba1 } from '@/shared/model/prueba-1.model';

import Prueba1Service from './prueba-1.service';

export default defineComponent({
  name: 'Prueba1Update',
  setup() {
    const prueba1Service = inject('prueba1Service', () => new Prueba1Service());
    const alertService = inject('alertService', () => useAlertService(), true);

    const prueba1: Ref<IPrueba1> = ref(new Prueba1());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrievePrueba1 = async prueba1Id => {
      try {
        const res = await prueba1Service().find(prueba1Id);
        prueba1.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.prueba1Id) {
      retrievePrueba1(route.params.prueba1Id);
    }

    const validations = useValidation();
    const validationRules = {};
    const v$ = useVuelidate(validationRules, prueba1 as any);
    v$.value.$validate();

    return {
      prueba1Service,
      alertService,
      prueba1,
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
      if (this.prueba1.id) {
        this.prueba1Service()
          .update(this.prueba1)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Prueba1 is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.prueba1Service()
          .create(this.prueba1)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Prueba1 is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
