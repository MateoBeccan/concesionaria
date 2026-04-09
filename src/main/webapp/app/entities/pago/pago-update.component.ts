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
    const pagoService    = inject('pagoService',    () => new PagoService());
    const alertService   = inject('alertService',   () => useAlertService(), true);
    const ventaService   = inject('ventaService',   () => new VentaService());
    const metodoPagoService = inject('metodoPagoService', () => new MetodoPagoService());
    const monedaService  = inject('monedaService',  () => new MonedaService());

    const pago: Ref<IPago>           = ref(new Pago());
    const ventas: Ref<IVenta[]>      = ref([]);
    const metodoPagos: Ref<IMetodoPago[]> = ref([]);
    const monedas: Ref<IMoneda[]>    = ref([]);
    const isSaving = ref(false);

    const route  = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

    const retrievePago = async (pagoId: any) => {
      try {
        const res = await pagoService().find(pagoId);
        res.fecha = new Date(res.fecha);
        pago.value = res;
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.pagoId) {
      retrievePago(route.params.pagoId);
    }

    // Pre-seleccionar venta desde query param ?ventaId=
    const ventaIdParam = route.query?.ventaId;

    const initRelationships = () => {
      ventaService().retrieve({ page: 0, size: 200 }).then(res => {
        ventas.value = res.data;
        if (ventaIdParam && !pago.value.id) {
          pago.value.venta = res.data.find(v => String(v.id) === String(ventaIdParam)) ?? null;
        }
      });
      metodoPagoService().retrieve().then(res => { metodoPagos.value = res.data; });
      monedaService().retrieve().then(res => { monedas.value = res.data; });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      monto:     { required: validations.required('El monto es obligatorio.'), min: validations.minValue('Debe ser mayor que 0.', 0) },
      fecha:     { required: validations.required('La fecha es obligatoria.') },
      referencia:{ maxLength: validations.maxLength('Máximo 100 caracteres.', 100) },
      venta:     {},
      metodoPago:{},
      moneda:    {},
    };

    const v$ = useVuelidate(validationRules, pago as any);
    v$.value.$validate();

    function usarSaldo() {
      if (pago.value.venta?.saldo) {
        pago.value.monto = Number(pago.value.venta.saldo);
      }
    }

    function formatPrecio(p?: number | null) {
      return Number(p ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2 });
    }

    return {
      pagoService, alertService, pago, previousState, isSaving,
      ventas, metodoPagos, monedas, v$, usarSaldo, formatPrecio,
      ...useDateFormat({ entityRef: pago }),
    };
  },
  methods: {
    save(): void {
      this.isSaving = true;
      const op = this.pago.id
        ? this.pagoService().update(this.pago)
        : this.pagoService().create(this.pago);

      op.then(param => {
        this.isSaving = false;
        this.previousState();
        this.alertService.showSuccess(
          this.pago.id ? `Pago actualizado correctamente` : `Pago de $ ${param.monto} registrado correctamente`,
        );
      }).catch(error => {
        this.isSaving = false;
        this.alertService.showHttpError(error.response);
      });
    },
  },
});
