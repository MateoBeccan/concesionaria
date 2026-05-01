import { computed, defineComponent, inject, ref, type Ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { helpers } from '@vuelidate/validators';
import { useVuelidate } from '@vuelidate/core';
import dayjs from 'dayjs';

import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import type { IInventario } from '@/shared/model/inventario.model';
import { Inventario } from '@/shared/model/inventario.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

import InventarioService from './inventario.service';

export default defineComponent({
  name: 'InventarioUpdate',
  setup() {
    const inventarioService = inject('inventarioService', () => new InventarioService());
    const vehiculoService = inject('vehiculoService', () => new VehiculoService());
    const alertService = inject('alertService', () => useAlertService(), true);
    const validations = useValidation();

    const route = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

    const inventario: Ref<IInventario> = ref({
      ...new Inventario(),
      fechaIngreso: dayjs(),
      estadoInventario: EstadoInventario.DISPONIBLE,
    });
    const vehiculos: Ref<IVehiculo[]> = ref([]);
    const isSaving = ref(false);
    const loadingRelations = ref(false);

    const estadoInventarioValues: Ref<string[]> = ref(
      Object.values(EstadoInventario).filter(estado => estado !== EstadoInventario.RESERVADO),
    );

    const selectedVehiculo = computed(
      () => vehiculos.value.find(item => item.id === inventario.value.vehiculo?.id) ?? inventario.value.vehiculo ?? null,
    );

    const businessErrors = computed(() => {
      const errors: string[] = [];
      if (!inventario.value.vehiculo?.id) {
        errors.push('Selecciona un vehiculo para administrar su inventario.');
      }
      if (inventario.value.estadoInventario === EstadoInventario.RESERVADO) {
        errors.push('El estado RESERVADO se administra unicamente desde la entidad Reserva.');
      }
      return errors;
    });

    const canSubmit = computed(() => !loadingRelations.value && businessErrors.value.length === 0);

    const procesoInventario = computed(() => [
      {
        number: '01',
        title: 'Estado',
        copy: inventario.value.estadoInventario ? `Estado ${inventario.value.estadoInventario} definido.` : 'Define la situacion del stock.',
        done: !!inventario.value.estadoInventario,
        current: !inventario.value.vehiculo?.id,
      },
      {
        number: '02',
        title: 'Unidad',
        copy: selectedVehiculo.value ? 'Vehiculo asociado al inventario.' : 'Selecciona la unidad que quieres administrar.',
        done: !!selectedVehiculo.value,
        current: !!inventario.value.estadoInventario && !selectedVehiculo.value,
      },
      {
        number: '03',
        title: 'Validacion',
        copy: businessErrors.value.length === 0 ? 'Listo para guardar.' : `${businessErrors.value.length} validacion(es) pendiente(s).`,
        done: businessErrors.value.length === 0,
        current: businessErrors.value.length > 0,
      },
    ]);

    const validationRules = {
      fechaIngreso: {
        required: validations.required('La fecha de ingreso es obligatoria.'),
      },
      codigoInternoStock: {
        maxLength: validations.maxLength('El codigo interno no puede superar los 30 caracteres.', 30),
      },
      ubicacion: {
        maxLength: validations.maxLength('La ubicacion no puede superar los 100 caracteres.', 100),
      },
      estadoInventario: {
        required: validations.required('Selecciona el estado del inventario.'),
      },
      observaciones: {
        maxLength: validations.maxLength('Las observaciones no pueden superar los 255 caracteres.', 255),
      },
      vehiculo: {
        required: helpers.withMessage('Selecciona un vehiculo.', value => !!value?.id),
      },
    };

    const v$ = useVuelidate(validationRules, inventario as any);

    async function retrieveInventario(inventarioId: number) {
      try {
        const res = await inventarioService().find(inventarioId);
        inventario.value = {
          ...res,
          fechaIngreso: res.fechaIngreso ? dayjs(res.fechaIngreso) : dayjs(),
          createdDate: res.createdDate ? dayjs(res.createdDate) : null,
          lastModifiedDate: res.lastModifiedDate ? dayjs(res.lastModifiedDate) : null,
        };
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    }

    async function initRelationships() {
      loadingRelations.value = true;
      try {
        const vehiculosRes = await vehiculoService().retrieve({ page: 0, size: 500, sort: ['createdDate,desc'] });
        vehiculos.value = [...vehiculosRes.data].sort((a, b) => `${a.patente ?? 'ZZZ'}-${a.id ?? 0}`.localeCompare(`${b.patente ?? 'ZZZ'}-${b.id ?? 0}`));
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        loadingRelations.value = false;
      }
    }

    if (route.params?.inventarioId) {
      retrieveInventario(Number(route.params.inventarioId));
    }
    initRelationships();

    return {
      inventarioService,
      alertService,
      inventario,
      previousState,
      estadoInventarioValues,
      isSaving,
      vehiculos,
      selectedVehiculo,
      loadingRelations,
      businessErrors,
      canSubmit,
      procesoInventario,
      v$,
      ...useDateFormat({ entityRef: inventario }),
    };
  },
  methods: {
    vehiculoLabel(vehiculo): string {
      if (!vehiculo) return 'Sin vehiculo';
      const patente = vehiculo.patente ?? 'Sin patente';
      return `${patente} · ID ${vehiculo.id}`;
    },
    async save(): Promise<void> {
      this.v$.$touch();
      if (this.v$.$invalid || !this.canSubmit) return;

      this.isSaving = true;
      try {
        const payload = {
          ...this.inventario,
          codigoInternoStock: this.inventario.codigoInternoStock?.trim().toUpperCase() || null,
          vehiculo: this.inventario.vehiculo ? { id: this.inventario.vehiculo.id } : null,
        };

        const result = this.inventario.id ? await this.inventarioService().update(payload) : await this.inventarioService().create(payload);
        this.alertService.showSuccess(this.inventario.id ? `Inventario ${result.id} actualizado.` : `Inventario ${result.id} creado.`);
        this.previousState();
      } catch (error: any) {
        this.alertService.showHttpError(error.response);
      } finally {
        this.isSaving = false;
      }
    },
  },
});
