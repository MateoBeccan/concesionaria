import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import MetodoPagoService from '@/entities/metodo-pago/metodo-pago.service';
import MonedaService from '@/entities/moneda/moneda.service';
import VentaService from '@/entities/venta/venta.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { type IMetodoPago } from '@/shared/model/metodo-pago.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type IPago, Pago } from '@/shared/model/pago.model';
import { type IVenta } from '@/shared/model/venta.model';

import PagoService from './pago.service';

export default defineComponent({
  name: 'PagoUpdate',
  setup() {
    const pagoService = inject('pagoService', () => new PagoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const pago: Ref<IPago> = ref(new Pago());

    const ventaService = inject('ventaService', () => new VentaService());

    const ventas: Ref<IVenta[]> = ref([]);

    const metodoPagoService = inject('metodoPagoService', () => new MetodoPagoService());

    const metodoPagos: Ref<IMetodoPago[]> = ref([]);

    const monedaService = inject('monedaService', () => new MonedaService());

    const monedas: Ref<IMoneda[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrievePago = async pagoId => {
      try {
        const res = await pagoService().find(pagoId);
        res.fecha = new Date(res.fecha);
        res.createdDate = new Date(res.createdDate);
        pago.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.pagoId) {
      retrievePago(route.params.pagoId);
    }

    const initRelationships = () => {
      ventaService()
        .retrieve()
        .then(res => {
          ventas.value = res.data;
        });
      metodoPagoService()
        .retrieve()
        .then(res => {
          metodoPagos.value = res.data;
        });
      monedaService()
        .retrieve()
        .then(res => {
          monedas.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      monto: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      fecha: {
        required: validations.required('Este campo es obligatorio.'),
      },
      referencia: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      createdDate: {},
      venta: {},
      metodoPago: {},
      moneda: {},
    };
    const v$ = useVuelidate(validationRules, pago as any);
    v$.value.$validate();

    return {
      pagoService,
      alertService,
      pago,
      previousState,
      isSaving,
      currentLanguage,
      ventas,
      metodoPagos,
      monedas,
      v$,
      ...useDateFormat({ entityRef: pago }),
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.pago.id) {
        this.pagoService()
          .update(this.pago)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Pago is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.pagoService()
          .create(this.pago)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Pago is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
