import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import ClienteService from '@/entities/cliente/cliente.service';
import EstadoVentaService from '@/entities/estado-venta/estado-venta.service';
import MonedaService from '@/entities/moneda/moneda.service';
import UserService from '@/entities/user/user.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { type ICliente } from '@/shared/model/cliente.model';
import { type IEstadoVenta } from '@/shared/model/estado-venta.model';
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

    const estadoVentaService = inject('estadoVentaService', () => new EstadoVentaService());

    const estadoVentas: Ref<IEstadoVenta[]> = ref([]);

    const monedaService = inject('monedaService', () => new MonedaService());

    const monedas: Ref<IMoneda[]> = ref([]);
    const userService = inject('userService', () => new UserService());
    const users: Ref<Array<any>> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveVenta = async ventaId => {
      try {
        const res = await ventaService().find(ventaId);
        res.fecha = new Date(res.fecha);
        res.createdDate = new Date(res.createdDate);
        res.lastModifiedDate = new Date(res.lastModifiedDate);
        venta.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.ventaId) {
      retrieveVenta(route.params.ventaId);
    }

    const initRelationships = () => {
      clienteService()
        .retrieve()
        .then(res => {
          clientes.value = res.data;
        });
      estadoVentaService()
        .retrieve()
        .then(res => {
          estadoVentas.value = res.data;
        });
      monedaService()
        .retrieve()
        .then(res => {
          monedas.value = res.data;
        });
      userService()
        .retrieve()
        .then(res => {
          users.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      fecha: {
        required: validations.required('Este campo es obligatorio.'),
      },
      cotizacion: {
        required: validations.required('Este campo es obligatorio.'),
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
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
      porcentajeImpuesto: {
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
        max: validations.maxValue('Este campo no puede ser mayor que 100.', 100),
      },
      totalPagado: {
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      saldo: {
        min: validations.minValue('Este campo debe ser mayor que 0.', 0),
      },
      observaciones: {
        maxLength: validations.maxLength('Este campo no puede superar más de 500 caracteres.', 500),
      },
      createdDate: {},
      lastModifiedDate: {},
      cliente: {},
      estadoVenta: {},
      moneda: {},
      user: {},
    };
    const v$ = useVuelidate(validationRules, venta as any);
    v$.value.$validate();

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
      ...useDateFormat({ entityRef: venta }),
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.venta.id) {
        this.ventaService()
          .update(this.venta)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Venta is updated with identifier ${param.id}`);
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
            this.alertService.showSuccess(`A Venta is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
