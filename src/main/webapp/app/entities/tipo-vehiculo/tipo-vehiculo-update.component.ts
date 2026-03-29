import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { type ITipoVehiculo, TipoVehiculo } from '@/shared/model/tipo-vehiculo.model';

import TipoVehiculoService from './tipo-vehiculo.service';

export default defineComponent({
  name: 'TipoVehiculoUpdate',
  setup() {
    const tipoVehiculoService = inject('tipoVehiculoService', () => new TipoVehiculoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const tipoVehiculo: Ref<ITipoVehiculo> = ref(new TipoVehiculo());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveTipoVehiculo = async tipoVehiculoId => {
      try {
        const res = await tipoVehiculoService().find(tipoVehiculoId);
        tipoVehiculo.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.tipoVehiculoId) {
      retrieveTipoVehiculo(route.params.tipoVehiculoId);
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
    const v$ = useVuelidate(validationRules, tipoVehiculo as any);
    v$.value.$validate();

    return {
      tipoVehiculoService,
      alertService,
      tipoVehiculo,
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
      if (this.tipoVehiculo.id) {
        this.tipoVehiculoService()
          .update(this.tipoVehiculo)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A TipoVehiculo is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.tipoVehiculoService()
          .create(this.tipoVehiculo)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A TipoVehiculo is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
