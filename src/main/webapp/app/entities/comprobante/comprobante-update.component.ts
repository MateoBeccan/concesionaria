import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import MonedaService from '@/entities/moneda/moneda.service';
import TipoComprobanteService from '@/entities/tipo-comprobante/tipo-comprobante.service';
import VentaService from '@/entities/venta/venta.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { Comprobante, type IComprobante } from '@/shared/model/comprobante.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import { type IVenta } from '@/shared/model/venta.model';

import ComprobanteService from './comprobante.service';

export default defineComponent({
  name: 'ComprobanteUpdate',
  setup() {
    const comprobanteService = inject('comprobanteService', () => new ComprobanteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const comprobante: Ref<IComprobante> = ref(new Comprobante());

    const ventaService = inject('ventaService', () => new VentaService());

    const ventas: Ref<IVenta[]> = ref([]);

    const tipoComprobanteService = inject('tipoComprobanteService', () => new TipoComprobanteService());

    const tipoComprobantes: Ref<ITipoComprobante[]> = ref([]);

    const monedaService = inject('monedaService', () => new MonedaService());

    const monedas: Ref<IMoneda[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveComprobante = async comprobanteId => {
      try {
        const res = await comprobanteService().find(comprobanteId);
        res.fechaEmision = new Date(res.fechaEmision);
        res.createdDate = new Date(res.createdDate);
        comprobante.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.comprobanteId) {
      retrieveComprobante(route.params.comprobanteId);
    }

    const initRelationships = () => {
      ventaService()
        .retrieve()
        .then(res => {
          ventas.value = res.data;
        });
      tipoComprobanteService()
        .retrieve()
        .then(res => {
          tipoComprobantes.value = res.data;
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
      numeroComprobante: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 3 caracteres.', 3),
        maxLength: validations.maxLength('Este campo no puede superar más de 50 caracteres.', 50),
      },
      fechaEmision: {
        required: validations.required('Este campo es obligatorio.'),
      },
      importeNeto: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      impuesto: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      total: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      createdDate: {},
      venta: {},
      tipoComprobante: {},
      moneda: {},
    };
    const v$ = useVuelidate(validationRules, comprobante as any);
    v$.value.$validate();

    return {
      comprobanteService,
      alertService,
      comprobante,
      previousState,
      isSaving,
      currentLanguage,
      ventas,
      tipoComprobantes,
      monedas,
      v$,
      ...useDateFormat({ entityRef: comprobante }),
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.comprobante.id) {
        this.comprobanteService()
          .update(this.comprobante)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Comprobante is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.comprobanteService()
          .create(this.comprobante)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Comprobante is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
