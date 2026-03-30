import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import ClienteService from '@/entities/cliente/cliente.service';
import MonedaService from '@/entities/moneda/moneda.service';
import UserService from '@/entities/user/user.service';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';

import { type ICliente } from '@/shared/model/cliente.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type IVenta, Venta } from '@/shared/model/venta.model';

import VentaService from './venta.service';

export default defineComponent({
  name: 'VentaUpdate',
  setup() {
    const ventaService = inject('ventaService', () => new VentaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const venta: Ref<IVenta> = ref(new Venta());

    const clienteService = inject('clienteService', () => new ClienteService());
    const clientes: Ref<ICliente[]> = ref([]);

    const estadoVentas = Object.values(EstadoVenta);

    const monedaService = inject('monedaService', () => new MonedaService());
    const monedas: Ref<IMoneda[]> = ref([]);

    const userService = inject('userService', () => new UserService());
    const users: Ref<Array<any>> = ref([]);

    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    // 🔥 FIX: tipado correcto + null safe
    const retrieveVenta = async (ventaId: number) => {
      try {
        const res = await ventaService().find(ventaId);

        res.fecha = res.fecha ? new Date(res.fecha) : null;
        res.createdDate = res.createdDate ? new Date(res.createdDate) : null;
        res.lastModifiedDate = res.lastModifiedDate ? new Date(res.lastModifiedDate) : null;

        venta.value = res;
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.ventaId) {
      retrieveVenta(Number(route.params.ventaId));
    }

    const initRelationships = () => {
      clienteService().retrieve().then(res => (clientes.value = res.data));
      monedaService().retrieve().then(res => (monedas.value = res.data));
      userService().retrieve().then(res => (users.value = res.data));
    };

    initRelationships();

    const validations = useValidation();

    const validationRules = {
      fecha: {
        required: validations.required('Este campo es obligatorio.'),
      },
      cotizacion: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Debe ser mayor que 0.', 0),
      },
      importeNeto: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Debe ser mayor que 0.', 0),
      },
      impuesto: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Debe ser mayor que 0.', 0),
      },
      total: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Debe ser mayor que 0.', 0),
      },
      porcentajeImpuesto: {
        min: validations.minValue('Debe ser mayor que 0.', 0),
        max: validations.maxValue('No puede ser mayor que 100.', 100),
      },
      totalPagado: {
        min: validations.minValue('Debe ser mayor que 0.', 0),
      },
      saldo: {
        min: validations.minValue('Debe ser mayor que 0.', 0),
      },
      observaciones: {
        maxLength: validations.maxLength('Máximo 500 caracteres.', 500),
      },
      cliente: {},
      estado: {},
      moneda: {},
      user: {},
    };

    const v$ = useVuelidate(validationRules, venta as any);
    v$.value.$validate();

    // 🔥 FIX CLAVE: extraer funciones correctamente
    const { convertDateTimeFromServer, updateInstantField } = useDateFormat({ entityRef: venta });

    return {
      ventaService,
      alertService,
      venta,
      previousState,
      isSaving,
      currentLanguage,
      clientes,
      estadoVentas,
      monedas,
      users,
      v$,
      convertDateTimeFromServer,
      updateInstantField,
    };
  },

  methods: {
    save(): void {
      this.isSaving = true;

      if (this.venta.id) {
        this.ventaService()
          .update(this.venta)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`Venta actualizada con ID ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.ventaService()
          .create(this.venta)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`Venta creada con ID ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
