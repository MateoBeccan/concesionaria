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

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    // 🔥 CONVERSIÓN A ISO (CLAVE)
    const convertToInstant = (value: any) => {
      return value ? new Date(value).toISOString() : null;
    };

    // 🔥 CONVERTIR PARA INPUT (cuando viene del backend)
    const convertFromInstant = (value: any) => {
      if (!value) return null;
      const date = new Date(value);
      return date.toISOString().slice(0, 16); // formato datetime-local
    };

    const retrieveCotizacion = async cotizacionId => {
      try {
        const res = await cotizacionService().find(cotizacionId);

        // 🔥 FIX FECHA
        res.fecha = convertFromInstant(res.fecha);

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
        min: validations.minValue('Debe ser mayor a 0.', 0),
      },
      valorVenta: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Debe ser mayor a 0.', 0),
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
      monedas,
      v$,
      convertToInstant, // 🔥 IMPORTANTE
      ...useDateFormat({ entityRef: cotizacion }),
    };
  },

  methods: {
    save(): void {
      this.isSaving = true;

      // 🔥 CLON + FIX FECHA
      const entity = {
        ...this.cotizacion,
        fecha: this.convertToInstant(this.cotizacion.fecha),
      };

      if (entity.id) {
        this.cotizacionService()
          .update(entity)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`Cotización actualizada (ID: ${param.id})`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.cotizacionService()
          .create(entity)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`Cotización creada (ID: ${param.id})`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
