import { computed, defineComponent, inject, ref, type Ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { helpers } from '@vuelidate/validators';
import { useVuelidate } from '@vuelidate/core';
import dayjs from 'dayjs';

import ClienteService from '@/entities/cliente/cliente.service';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import type { ICliente } from '@/shared/model/cliente.model';
import { CondicionVehiculo } from '@/shared/model/enumerations/condicion-vehiculo.model';
import { EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import type { IInventario } from '@/shared/model/inventario.model';
import { Inventario } from '@/shared/model/inventario.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';

import InventarioService from './inventario.service';

function mapEstadoToCondicion(estado?: keyof typeof EstadoInventario | null): keyof typeof CondicionVehiculo | null {
  if (estado === EstadoInventario.DISPONIBLE) return CondicionVehiculo.EN_VENTA;
  if (estado === EstadoInventario.RESERVADO) return CondicionVehiculo.RESERVADO;
  if (estado === EstadoInventario.VENDIDO) return CondicionVehiculo.VENDIDO;
  return null;
}

function isAfterDate(start?: any, end?: any): boolean {
  if (!start || !end) return false;
  const startDate = dayjs(start);
  const endDate = dayjs(end);
  return startDate.isValid() && endDate.isValid() && endDate.isAfter(startDate);
}

export default defineComponent({
  name: 'InventarioUpdate',
  setup() {
    const inventarioService = inject('inventarioService', () => new InventarioService());
    const vehiculoService = inject('vehiculoService', () => new VehiculoService());
    const clienteService = inject('clienteService', () => new ClienteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

    const inventario: Ref<IInventario> = ref({
      ...new Inventario(),
      fechaIngreso: dayjs(),
      estadoInventario: EstadoInventario.DISPONIBLE,
      disponible: true,
    });

    const vehiculos: Ref<IVehiculo[]> = ref([]);
    const clientes: Ref<ICliente[]> = ref([]);
    const isSaving = ref(false);
    const loadingRelations = ref(false);
    const estadoInventarioValues: Ref<string[]> = ref(Object.values(EstadoInventario));

    const validations = useValidation();

    const isReservado = computed(() => inventario.value.estadoInventario === EstadoInventario.RESERVADO);
    const isVendido = computed(() => inventario.value.estadoInventario === EstadoInventario.VENDIDO);
    const selectedVehiculo = computed(
      () => vehiculos.value.find(item => item.id === inventario.value.vehiculo?.id) ?? inventario.value.vehiculo ?? null,
    );
    const selectedCliente = computed(
      () => clientes.value.find(item => item.id === inventario.value.clienteReserva?.id) ?? inventario.value.clienteReserva ?? null,
    );
    const expectedCondicion = computed(() => mapEstadoToCondicion(inventario.value.estadoInventario));
    const reservaVencida = computed(() => {
      if (!isReservado.value || !inventario.value.fechaVencimientoReserva) return false;
      return dayjs(inventario.value.fechaVencimientoReserva).isBefore(dayjs());
    });

    const stateSummaryLabel = computed(() => {
      if (inventario.value.estadoInventario === EstadoInventario.RESERVADO) return 'Reserva activa';
      if (inventario.value.estadoInventario === EstadoInventario.VENDIDO) return 'Unidad vendida';
      return 'Disponible para operar';
    });

    const stateSummaryClass = computed(() => {
      if (inventario.value.estadoInventario === EstadoInventario.RESERVADO) return 'bg-warning text-dark';
      if (inventario.value.estadoInventario === EstadoInventario.VENDIDO) return 'bg-danger';
      return 'bg-success';
    });

    const businessErrors = computed(() => {
      const errors: string[] = [];

      if (!inventario.value.vehiculo?.id) {
        errors.push('Seleccioná un vehículo para administrar su inventario.');
      }

      if (selectedVehiculo.value?.condicion === CondicionVehiculo.VENDIDO && inventario.value.estadoInventario !== EstadoInventario.VENDIDO) {
        errors.push('No se puede volver a marcar disponible o reservado un vehículo ya vendido.');
      }

      if (isReservado.value) {
        if (!inventario.value.clienteReserva?.id) {
          errors.push('Debés seleccionar el cliente que toma la reserva.');
        }
        if (!inventario.value.fechaReserva) {
          errors.push('La fecha de reserva es obligatoria.');
        }
        if (!inventario.value.fechaVencimientoReserva) {
          errors.push('La fecha de vencimiento de la reserva es obligatoria.');
        }
        if (
          inventario.value.fechaReserva &&
          inventario.value.fechaVencimientoReserva &&
          !isAfterDate(inventario.value.fechaReserva, inventario.value.fechaVencimientoReserva)
        ) {
          errors.push('El vencimiento de la reserva debe ser posterior a la fecha de reserva.');
        }
      }

      return errors;
    });

    const businessWarnings = computed(() => {
      const warnings: string[] = [];

      if (selectedVehiculo.value?.condicion && expectedCondicion.value && selectedVehiculo.value.condicion !== expectedCondicion.value) {
        warnings.push(
          `La condición comercial del vehículo se sincronizará de ${selectedVehiculo.value.condicion} a ${expectedCondicion.value} al guardar.`,
        );
      }

      if (reservaVencida.value) {
        warnings.push('La reserva está vencida. Conviene liberarla o renovarla antes de seguir operando.');
      }

      return warnings;
    });

    const canSubmit = computed(() => !loadingRelations.value && businessErrors.value.length === 0);

    const validationRules = {
      fechaIngreso: {
        required: validations.required('La fecha de ingreso es obligatoria.'),
      },
      ubicacion: {
        maxLength: validations.maxLength('La ubicación no puede superar los 100 caracteres.', 100),
      },
      estadoInventario: {
        required: validations.required('Seleccioná el estado del inventario.'),
      },
      observaciones: {
        maxLength: validations.maxLength('Las observaciones no pueden superar los 255 caracteres.', 255),
      },
      vehiculo: {
        required: helpers.withMessage('Seleccioná un vehículo.', value => !!value?.id),
      },
      clienteReserva: {
        requiredWhenReserved: helpers.withMessage('Seleccioná un cliente para la reserva.', value => {
          if (!isReservado.value) return true;
          return !!value?.id;
        }),
      },
      fechaReserva: {
        requiredWhenReserved: helpers.withMessage('Ingresá la fecha de reserva.', value => {
          if (!isReservado.value) return true;
          return !!value;
        }),
      },
      fechaVencimientoReserva: {
        requiredWhenReserved: helpers.withMessage('Ingresá el vencimiento de la reserva.', value => {
          if (!isReservado.value) return true;
          return !!value;
        }),
        afterReserva: helpers.withMessage('El vencimiento debe ser posterior a la fecha de reserva.', value => {
          if (!isReservado.value) return true;
          return isAfterDate(inventario.value.fechaReserva, value);
        }),
      },
    };

    const v$ = useVuelidate(validationRules, inventario as any);

    function sortVehiculos(items: IVehiculo[]) {
      return [...items].sort((a, b) => {
        const labelA = `${a.patente ?? 'ZZZ'}-${a.id ?? 0}`;
        const labelB = `${b.patente ?? 'ZZZ'}-${b.id ?? 0}`;
        return labelA.localeCompare(labelB);
      });
    }

    function sortClientes(items: ICliente[]) {
      return [...items].sort((a, b) => `${a.apellido} ${a.nombre}`.localeCompare(`${b.apellido} ${b.nombre}`));
    }

    function reconcileSelections() {
      if (inventario.value.vehiculo?.id) {
        inventario.value.vehiculo = vehiculos.value.find(item => item.id === inventario.value.vehiculo?.id) ?? inventario.value.vehiculo;
      }

      if (inventario.value.clienteReserva?.id) {
        inventario.value.clienteReserva =
          clientes.value.find(item => item.id === inventario.value.clienteReserva?.id) ?? inventario.value.clienteReserva;
      }
    }

    async function retrieveInventario(inventarioId: number) {
      try {
        const res = await inventarioService().find(inventarioId);
        inventario.value = {
          ...res,
          fechaIngreso: res.fechaIngreso ? dayjs(res.fechaIngreso) : dayjs(),
          createdDate: res.createdDate ? dayjs(res.createdDate) : null,
          lastModifiedDate: res.lastModifiedDate ? dayjs(res.lastModifiedDate) : null,
          fechaReserva: res.fechaReserva ? dayjs(res.fechaReserva) : null,
          fechaVencimientoReserva: res.fechaVencimientoReserva ? dayjs(res.fechaVencimientoReserva) : null,
        };
        reconcileSelections();
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    }

    async function initRelationships() {
      loadingRelations.value = true;
      try {
        const [vehiculosRes, clientesRes] = await Promise.all([
          vehiculoService().retrieve({ page: 0, size: 500, sort: ['createdDate,desc'] }),
          clienteService().retrieve({ page: 0, size: 500, sort: ['apellido,asc'] }),
        ]);

        vehiculos.value = sortVehiculos(vehiculosRes.data);
        clientes.value = sortClientes(clientesRes.data);
        reconcileSelections();
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        loadingRelations.value = false;
      }
    }

    watch(
      () => inventario.value.estadoInventario,
      estado => {
        inventario.value.disponible = estado === EstadoInventario.DISPONIBLE;

        if (estado !== EstadoInventario.RESERVADO) {
          inventario.value.clienteReserva = null;
          inventario.value.fechaReserva = null;
          inventario.value.fechaVencimientoReserva = null;
        } else {
          inventario.value.fechaReserva = inventario.value.fechaReserva ?? dayjs();
          inventario.value.fechaVencimientoReserva = inventario.value.fechaVencimientoReserva ?? dayjs().add(3, 'day');
        }
      },
      { immediate: true },
    );

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
      clientes,
      selectedVehiculo,
      selectedCliente,
      loadingRelations,
      isReservado,
      isVendido,
      reservaVencida,
      stateSummaryLabel,
      stateSummaryClass,
      businessErrors,
      businessWarnings,
      canSubmit,
      expectedCondicion,
      v$,
      ...useDateFormat({ entityRef: inventario }),
    };
  },
  methods: {
    vehiculoLabel(vehiculo): string {
      if (!vehiculo) return 'Sin vehículo';
      const patente = vehiculo.patente ?? 'Sin patente';
      return `${patente} · ${vehiculo.condicion ?? 'SIN_CONDICION'} · ID ${vehiculo.id}`;
    },
    clienteLabel(cliente): string {
      if (!cliente) return 'Sin cliente';
      return `${cliente.apellido ?? ''}, ${cliente.nombre ?? ''}`.trim().replace(/^,|,$/g, '') || `Cliente ${cliente.id}`;
    },
    async save(): Promise<void> {
      this.v$.$touch();
      if (this.v$.$invalid || !this.canSubmit) return;

      this.isSaving = true;

      try {
        const payload = {
          ...this.inventario,
          disponible: this.inventario.estadoInventario === EstadoInventario.DISPONIBLE,
          vehiculo: this.inventario.vehiculo ? { id: this.inventario.vehiculo.id } : null,
          clienteReserva: this.inventario.clienteReserva ? { id: this.inventario.clienteReserva.id } : null,
        };

        const result = this.inventario.id ? await this.inventarioService().update(payload) : await this.inventarioService().create(payload);

        this.alertService.showSuccess(
          this.inventario.id ? `Inventario ${result.id} actualizado.` : `Inventario ${result.id} creado.`,
        );
        this.previousState();
      } catch (error: any) {
        this.alertService.showHttpError(error.response);
      } finally {
        this.isSaving = false;
      }
    },
  },
});
