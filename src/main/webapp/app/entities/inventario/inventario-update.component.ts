import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import ClienteService from '@/entities/cliente/cliente.service';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { type ICliente } from '@/shared/model/cliente.model';
import { EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import { type IInventario, Inventario } from '@/shared/model/inventario.model';
import { type IVehiculo } from '@/shared/model/vehiculo.model';

import InventarioService from './inventario.service';

export default defineComponent({
  name: 'InventarioUpdate',
  setup() {
    const inventarioService = inject('inventarioService', () => new InventarioService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const inventario: Ref<IInventario> = ref(new Inventario());

    const vehiculoService = inject('vehiculoService', () => new VehiculoService());

    const vehiculos: Ref<IVehiculo[]> = ref([]);

    const clienteService = inject('clienteService', () => new ClienteService());

    const clientes: Ref<ICliente[]> = ref([]);
    const estadoInventarioValues: Ref<string[]> = ref(Object.keys(EstadoInventario));
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveInventario = async inventarioId => {
      try {
        const res = await inventarioService().find(inventarioId);
        res.fechaIngreso = new Date(res.fechaIngreso);
        res.createdDate = new Date(res.createdDate);
        res.lastModifiedDate = new Date(res.lastModifiedDate);
        res.fechaReserva = new Date(res.fechaReserva);
        res.fechaVencimientoReserva = new Date(res.fechaVencimientoReserva);
        inventario.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.inventarioId) {
      retrieveInventario(route.params.inventarioId);
    }

    const initRelationships = () => {
      vehiculoService()
        .retrieve()
        .then(res => {
          vehiculos.value = res.data;
        });
      clienteService()
        .retrieve()
        .then(res => {
          clientes.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      fechaIngreso: {
        required: validations.required('Este campo es obligatorio.'),
      },
      ubicacion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      estadoInventario: {
        required: validations.required('Este campo es obligatorio.'),
      },
      observaciones: {
        maxLength: validations.maxLength('Este campo no puede superar más de 255 caracteres.', 255),
      },
      disponible: {
        required: validations.required('Este campo es obligatorio.'),
      },
      createdDate: {},
      lastModifiedDate: {},
      fechaReserva: {},
      fechaVencimientoReserva: {},
      vehiculo: {},
      clienteReserva: {},
    };
    const v$ = useVuelidate(validationRules, inventario as any);
    v$.value.$validate();

    return {
      inventarioService,
      alertService,
      inventario,
      previousState,
      estadoInventarioValues,
      isSaving,
      currentLanguage,
      vehiculos,
      clientes,
      v$,
      ...useDateFormat({ entityRef: inventario }),
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.inventario.id) {
        this.inventarioService()
          .update(this.inventario)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Inventario is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.inventarioService()
          .create(this.inventario)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Inventario is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
