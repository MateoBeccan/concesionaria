import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { EstadoVenta, type IEstadoVenta } from '@/shared/model/estado-venta.model';

import EstadoVentaService from './estado-venta.service';

export default defineComponent({
  name: 'EstadoVentaUpdate',
  setup() {
    const estadoVentaService = inject('estadoVentaService', () => new EstadoVentaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const estadoVenta: Ref<IEstadoVenta> = ref(new EstadoVenta());
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveEstadoVenta = async estadoVentaId => {
      try {
        const res = await estadoVentaService().find(estadoVentaId);
        estadoVenta.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.estadoVentaId) {
      retrieveEstadoVenta(route.params.estadoVentaId);
    }

    const validations = useValidation();
    const validationRules = {
      codigo: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 3 caracteres.', 3),
        maxLength: validations.maxLength('Este campo no puede superar más de 50 caracteres.', 50),
      },
      descripcion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
    };
    const v$ = useVuelidate(validationRules, estadoVenta as any);
    v$.value.$validate();

    return {
      estadoVentaService,
      alertService,
      estadoVenta,
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
      if (this.estadoVenta.id) {
        this.estadoVentaService()
          .update(this.estadoVenta)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A EstadoVenta is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.estadoVentaService()
          .create(this.estadoVenta)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A EstadoVenta is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
