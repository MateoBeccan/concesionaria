import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import TipoComprobanteService from '@/entities/tipo-comprobante/tipo-comprobante.service';
import VentaService from '@/entities/venta/venta.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { Comprobante, type IComprobante } from '@/shared/model/comprobante.model';
import { type ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import { type IVenta } from '@/shared/model/venta.model';

import ComprobanteService from './comprobante.service';

export default defineComponent({
  name: 'ComprobanteUpdate',
  setup() {
    const comprobanteService = inject('comprobanteService', () => new ComprobanteService());
    const alertService = inject('alertService', () => useAlertService(), true);
    const ventaService = inject('ventaService', () => new VentaService());
    const tipoComprobanteService = inject('tipoComprobanteService', () => new TipoComprobanteService());

    const comprobante: Ref<IComprobante> = ref(new Comprobante());
    const ventas: Ref<IVenta[]> = ref([]);
    const tipoComprobantes: Ref<ITipoComprobante[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

    const retrieveComprobante = async comprobanteId => {
      try {
        const res = await comprobanteService().find(comprobanteId);
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
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      venta: { required: validations.required('Selecciona una venta.') },
      tipoComprobante: { required: validations.required('Selecciona un tipo de comprobante.') },
    };
    const v$ = useVuelidate(validationRules, comprobante as any);

    return {
      comprobanteService,
      alertService,
      comprobante,
      previousState,
      isSaving,
      currentLanguage,
      ventas,
      tipoComprobantes,
      v$,
    };
  },
  methods: {
    save(): void {
      this.v$.$touch();
      if (this.v$.$invalid) return;

      if (this.comprobante.id) {
        this.alertService.showInfo('El comprobante ya fue emitido y no puede editarse.');
        return;
      }

      const ventaId = this.comprobante.venta?.id;
      const tipoComprobanteId = this.comprobante.tipoComprobante?.id;
      if (!ventaId || !tipoComprobanteId) {
        this.alertService.showError('Selecciona venta y tipo de comprobante.');
        return;
      }

      this.isSaving = true;
      this.comprobanteService()
        .emitir(ventaId, tipoComprobanteId)
        .then(param => {
          this.isSaving = false;
          this.previousState();
          this.alertService.showSuccess(`Comprobante ${param.numeroComprobante} emitido correctamente.`);
        })
        .catch(error => {
          this.isSaving = false;
          this.alertService.showHttpError(error.response);
        });
    },
    formatFecha(value?: Date): string {
      if (!value) return 'Sin fecha';
      return new Date(value).toLocaleDateString('es-AR');
    },
    formatMoneda(value?: number): string {
      const parsed = Number(value ?? 0);
      return Number.isFinite(parsed)
        ? parsed.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
        : '0,00';
    },
    ventaLabel(venta: IVenta): string {
      const cliente = `${venta.cliente?.apellido ?? ''} ${venta.cliente?.nombre ?? ''}`.trim() || 'Cliente';
      return `Venta #${venta.id} - ${cliente}`;
    },
  },
});
