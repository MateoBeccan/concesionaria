import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import ConfiguracionAutoService from '@/entities/configuracion-auto/configuracion-auto.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { Auto, type IAuto } from '@/shared/model/auto.model';
import { type IConfiguracionAuto } from '@/shared/model/configuracion-auto.model';
import { CondicionAuto } from '@/shared/model/enumerations/condicion-auto.model';
import { EstadoAuto } from '@/shared/model/enumerations/estado-auto.model';

import AutoService from './auto.service';

export default defineComponent({
  name: 'AutoUpdate',
  setup() {
    const autoService = inject('autoService', () => new AutoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const auto: Ref<IAuto> = ref(new Auto());

    const configuracionAutoService = inject('configuracionAutoService', () => new ConfiguracionAutoService());

    const configuracionAutos: Ref<IConfiguracionAuto[]> = ref([]);
    const estadoAutoValues: Ref<string[]> = ref(Object.keys(EstadoAuto));
    const condicionAutoValues: Ref<string[]> = ref(Object.keys(CondicionAuto));
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveAuto = async autoId => {
      try {
        const res = await autoService().find(autoId);
        auto.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.autoId) {
      retrieveAuto(route.params.autoId);
    }

    const initRelationships = () => {
      configuracionAutoService()
        .retrieve()
        .then(res => {
          configuracionAutos.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      estado: {
        required: validations.required('Este campo es obligatorio.'),
      },
      condicion: {
        required: validations.required('Este campo es obligatorio.'),
      },
      fechaFabricacion: {},
      km: {},
      patente: {},
      precio: {},
      configuracion: {},
    };
    const v$ = useVuelidate(validationRules, auto as any);
    v$.value.$validate();

    return {
      autoService,
      alertService,
      auto,
      previousState,
      estadoAutoValues,
      condicionAutoValues,
      isSaving,
      currentLanguage,
      configuracionAutos,
      v$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.auto.id) {
        this.autoService()
          .update(this.auto)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Auto is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.autoService()
          .create(this.auto)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Auto is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
