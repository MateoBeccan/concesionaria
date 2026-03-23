import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import MonedaService from '@/entities/moneda/moneda.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { Cotizacion, type ICotizacion } from '@/shared/model/cotizacion.model';
import { type IMoneda } from '@/shared/model/moneda.model';

import CotizacionService from './cotizacion.service';

export default defineComponent({
  name: 'CotizacionUpdate',
  setup() {
    const cotizacionService = inject('cotizacionService', () => new CotizacionService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const cotizacion: Ref<ICotizacion> = ref(new Cotizacion());

    const monedaService = inject('monedaService', () => new MonedaService());

    const monedas: Ref<IMoneda[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveCotizacion = async cotizacionId => {
      try {
        const res = await cotizacionService().find(cotizacionId);
        res.fecha = new Date(res.fecha);
        cotizacion.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.cotizacionId) {
      retrieveCotizacion(route.params.cotizacionId);
    }

    const initRelationships = () => {
      monedaService()
        .retrieve()
        .then(res => {
          monedas.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      fecha: {
        required: validations.required('Este campo es obligatorio.'),
      },
      valorCompra: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      valorVenta: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      activo: {
        required: validations.required('Este campo es obligatorio.'),
      },
      moneda: {},
    };
    const v$ = useVuelidate(validationRules, cotizacion as any);
    v$.value.$validate();

    return {
      cotizacionService,
      alertService,
      cotizacion,
      previousState,
      isSaving,
      currentLanguage,
      monedas,
      v$,
      ...useDateFormat({ entityRef: cotizacion }),
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.cotizacion.id) {
        this.cotizacionService()
          .update(this.cotizacion)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Cotizacion is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.cotizacionService()
          .create(this.cotizacion)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Cotizacion is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
