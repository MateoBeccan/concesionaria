import { computed, defineComponent, inject, ref, type Ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { helpers } from '@vuelidate/validators';
import { useVuelidate } from '@vuelidate/core';

import InventarioService from '@/entities/inventario/inventario.service';
import MarcaService from '@/entities/marca/marca.service';
import MonedaService from '@/entities/moneda/moneda.service';
import ModeloService from '@/entities/modelo/modelo.service';
import TipoVehiculoService from '@/entities/tipo-vehiculo/tipo-vehiculo.service';
import VersionService from '@/entities/version/version.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import type { IInventario } from '@/shared/model/inventario.model';
import type { IMotor } from '@/shared/model/motor.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';
import { EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import { EstadoVehiculo } from '@/shared/model/enumerations/estado-vehiculo.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import { Vehiculo } from '@/shared/model/vehiculo.model';
import type { IVersion } from '@/shared/model/version.model';

import VehiculoService from './vehiculo.service';
import { useVehiculoForm } from './useVehiculoForm';

const PATENTE_REGEX = /^(?:[A-Z]{3}\d{3}|[A-Z]{2}\d{3}[A-Z]{2})$/;
const VIN_CHASIS_MIN_LENGTH = 8;
const VIN_CHASIS_MAX_LENGTH = 30;
const VIN_STANDARD_LENGTH = 17;
const VIN_ALPHANUM_REGEX = /^[A-Z0-9]+$/;
const VIN_STANDARD_REGEX = /^[A-HJ-NPR-Z0-9]{17}$/;
const VIN_WEIGHTS = [8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2];

function normalizarPatente(value?: string | null): string | null {
  const patente = (value ?? '').trim().toUpperCase();
  return patente.length > 0 ? patente : null;
}

function normalizarVinChasis(value?: string | null): string | null {
  const vinChasis = (value ?? '')
    .trim()
    .toUpperCase()
    .replace(/[\s-]/g, '');
  return vinChasis.length > 0 ? vinChasis : null;
}

function transliterarVin(ch: string): number {
  if (/^\d$/.test(ch)) return Number(ch);
  switch (ch) {
    case 'A':
    case 'J':
      return 1;
    case 'B':
    case 'K':
    case 'S':
      return 2;
    case 'C':
    case 'L':
    case 'T':
      return 3;
    case 'D':
    case 'M':
    case 'U':
      return 4;
    case 'E':
    case 'N':
    case 'V':
      return 5;
    case 'F':
    case 'W':
      return 6;
    case 'G':
    case 'P':
    case 'X':
      return 7;
    case 'H':
    case 'Y':
      return 8;
    case 'R':
    case 'Z':
      return 9;
    default:
      return -1;
  }
}

function vinCheckDigitValido(vin: string): boolean {
  if (vin.length !== VIN_STANDARD_LENGTH) return true;
  let suma = 0;
  for (let i = 0; i < VIN_STANDARD_LENGTH; i++) {
    const valor = transliterarVin(vin[i]);
    if (valor < 0) return false;
    suma += valor * VIN_WEIGHTS[i];
  }
  const resto = suma % 11;
  const esperado = resto === 10 ? 'X' : String(resto);
  return vin[8] === esperado;
}

function patenteEsObligatoria(estado?: keyof typeof EstadoVehiculo | null): boolean {
  return estado === EstadoVehiculo.USADO;
}

export default defineComponent({
  name: 'VehiculoUpdate',

  setup() {
    const vehiculoService = inject('vehiculoService', () => new VehiculoService());
    const inventarioService = inject('inventarioService', () => new InventarioService());
    const marcaService = inject('marcaService', () => new MarcaService());
    const monedaService = inject('monedaService', () => new MonedaService());
    const modeloService = inject('modeloService', () => new ModeloService());
    const versionService = inject('versionService', () => new VersionService());
    const tipoVehiculoService = inject('tipoVehiculoService', () => new TipoVehiculoService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();
    const previousState = () => router.go(-1);

    const vehiculo: Ref<IVehiculo> = ref({
      ...new Vehiculo(),
      km: 0,
    });

    const inventarioAsociado = ref<IInventario | null>(null);
    const patenteDuplicada = ref('');
    const chequeandoPatente = ref(false);
    const isSaving = ref(false);

    const estadoVehiculoValues: Ref<string[]> = ref(Object.values(EstadoVehiculo));
    const monedas: Ref<IMoneda[]> = ref([]);

    const formCatalog = useVehiculoForm({
      marcaService: marcaService(),
      modeloService: modeloService(),
      versionService: versionService(),
      tipoVehiculoService: tipoVehiculoService(),
      onError: (error: any) => alertService.showHttpError(error.response),
    });

    const {
      marcas,
      modelos,
      versions,
      tipoVehiculos,
      selectedMarca,
      selectedModelo,
      modelosFiltrados,
      versionesFiltradas,
      motoresCompatibles,
      loadingMotores,
      motorHint,
      cargarCatalogos,
      onMarcaChange,
      onModeloChange,
      onVersionChange,
      isMotorCompatible,
    } = formCatalog;

    const inventarioWarning = computed(() => {
      if (!inventarioAsociado.value) return '';

      if (inventarioAsociado.value.estadoInventario && inventarioAsociado.value.estadoInventario !== EstadoInventario.DISPONIBLE) {
        return `La unidad ya tiene estado comercial ${inventarioAsociado.value.estadoInventario} en inventario. Verificalo antes de operar.`;
      }

      return '';
    });

    const motorValidationMessage = computed(() => {
      if (!vehiculo.value.version) {
        return '';
      }

      if (loadingMotores.value) {
        return '';
      }

      if (motoresCompatibles.value.length === 0) {
        return 'La version seleccionada no tiene motores configurados.';
      }

      if (!vehiculo.value.motor) {
        return '';
      }

      if (!isMotorCompatible(vehiculo.value.motor.id)) {
        return 'El motor seleccionado no corresponde a la version elegida.';
      }

      return '';
    });

    const canSaveVehiculo = computed(() => {
      if (!vehiculo.value.version) {
        return true;
      }

      if (loadingMotores.value || motoresCompatibles.value.length === 0) {
        return false;
      }

      return !!vehiculo.value.motor?.id && isMotorCompatible(vehiculo.value.motor.id);
    });

    const catalogoCompleto = computed(
      () => !!selectedMarca.value?.id && !!selectedModelo.value?.id && !!vehiculo.value.version?.id && !!vehiculo.value.motor?.id,
    );

    const datosComercialesCompletos = computed(() => {
      const patenteOk = patenteRequerida.value ? !!normalizarPatente(vehiculo.value.patente) : true;
      return !!vehiculo.value.estado && !!vehiculo.value.fechaFabricacion && Number(vehiculo.value.precio ?? 0) > 0 && patenteOk;
    });

    const procesoVehiculo = computed(() => [
      {
        number: '01',
        title: 'Catalogo tecnico',
        copy: catalogoCompleto.value ? 'Marca, modelo, version y motor definidos.' : 'Defini la configuracion tecnica de la unidad.',
        done: catalogoCompleto.value,
        current: !catalogoCompleto.value,
      },
      {
        number: '02',
        title: 'Datos comerciales',
        copy: datosComercialesCompletos.value ? 'Patente, estado, km y precio listos.' : 'Completa identificacion, estado y precio.',
        done: datosComercialesCompletos.value,
        current: catalogoCompleto.value && !datosComercialesCompletos.value,
      },
      {
        number: '03',
        title: 'Inventario',
        copy: inventarioAsociado.value ? 'La unidad ya tiene seguimiento en inventario.' : 'Podras asociarla a inventario en el siguiente paso.',
        done: !!inventarioAsociado.value,
        current: catalogoCompleto.value && datosComercialesCompletos.value,
      },
    ]);

    const patenteRequerida = computed(() => patenteEsObligatoria(vehiculo.value.estado));

    const patenteHint = computed(() => {
      if (patenteRequerida.value) {
        return 'Para unidades usadas la patente es obligatoria.';
      }

      return 'La patente puede cargarse más adelante.';
    });

    const validation = useValidation();
    const validationRules = {
      patente: {
        requiredWhenUsed: helpers.withMessage('La patente es obligatoria para vehiculos usados.', value => {
          if (!patenteRequerida.value) {
            return true;
          }

          return !!normalizarPatente(value);
        }),
        validFormat: helpers.withMessage('Usa formato ABC123 o AB123CD.', value => {
          const patente = normalizarPatente(value);
          return patente ? PATENTE_REGEX.test(patente) : !patenteRequerida.value;
        }),
      },
      estado: {
        required: validation.required('Selecciona el estado fisico del vehiculo.'),
      },
      fechaFabricacion: {
        required: validation.required('La fecha de fabricacion es obligatoria.'),
        notFuture: helpers.withMessage('La fecha de fabricacion no puede ser futura.', value => {
          if (!value) return false;
          const fecha = new Date(value as string);
          if (Number.isNaN(fecha.getTime())) return false;

          const hoy = new Date();
          hoy.setHours(0, 0, 0, 0);
          fecha.setHours(0, 0, 0, 0);
          return fecha <= hoy;
        }),
      },
      precio: {
        required: validation.required('El precio es obligatorio.'),
        min: validation.minValue('El precio debe ser mayor a 0.', 0.01),
      },
      moneda: {
        required: validation.required('Selecciona la moneda del precio del vehiculo.'),
      },
      vinChasis: {
        validCharsAndLength: helpers.withMessage('El numero de chasis debe tener entre 8 y 30 caracteres alfanumericos.', value => {
          const vinChasis = normalizarVinChasis(value);
          if (!vinChasis) {
            return true;
          }
          return (
            vinChasis.length >= VIN_CHASIS_MIN_LENGTH &&
            vinChasis.length <= VIN_CHASIS_MAX_LENGTH &&
            VIN_ALPHANUM_REGEX.test(vinChasis)
          );
        }),
        validVin17: helpers.withMessage('El VIN de 17 caracteres no es valido (revisa digito verificador y caracteres permitidos).', value => {
          const vinChasis = normalizarVinChasis(value);
          if (!vinChasis || vinChasis.length !== VIN_STANDARD_LENGTH) {
            return true;
          }
          return VIN_STANDARD_REGEX.test(vinChasis) && vinCheckDigitValido(vinChasis);
        }),
      },
      color: {
        maxLength: validation.maxLength('El color no puede superar 50 caracteres.', 50),
      },
      observaciones: {
        maxLength: validation.maxLength('Las observaciones no pueden superar 500 caracteres.', 500),
      },
      km: {
        required: validation.required('Los kilometros son obligatorios.'),
        integer: validation.integer('Los kilometros deben ser enteros.'),
        min: validation.minValue('Los kilometros no pueden ser negativos.', 0),
      },
      version: {
        required: validation.required('Selecciona una version para el vehiculo.'),
      },
      motor: {
        required: helpers.withMessage('Selecciona un motor compatible para continuar.', () => {
          if (!vehiculo.value.version) {
            return true;
          }

          if (loadingMotores.value || motoresCompatibles.value.length === 0) {
            return false;
          }

          return !!vehiculo.value.motor;
        }),
        compatible: helpers.withMessage('El motor seleccionado no corresponde a la version elegida.', () => {
          if (!vehiculo.value.version || !vehiculo.value.motor) {
            return true;
          }

          return isMotorCompatible(vehiculo.value.motor.id);
        }),
      },
      tipoVehiculo: {
        required: validation.required('Selecciona el tipo de vehiculo.'),
      },
    };

    const v$ = useVuelidate(validationRules, vehiculo as any);

    async function cargarInventarioAsociado(vehiculoId: number) {
      try {
        const res = await inventarioService().retrieve({ page: 0, size: 200, sort: ['fechaIngreso,desc'] });
        inventarioAsociado.value = (res.data as IInventario[]).find(item => item.vehiculo?.id === vehiculoId) ?? null;
      } catch {
        inventarioAsociado.value = null;
      }
    }

    async function retrieveVehiculo(vehiculoId: number) {
      try {
        vehiculo.value = await vehiculoService().find(vehiculoId);
        vehiculo.value.patente = normalizarPatente(vehiculo.value.patente) ?? '';
        await onVersionChange(vehiculo.value, true);
        await cargarInventarioAsociado(vehiculoId);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    }

    async function handleMarcaChange() {
      onMarcaChange(vehiculo.value);
      await v$.value.version.$reset();
      await v$.value.motor.$reset();
    }

    async function handleModeloChange() {
      onModeloChange(vehiculo.value);
      await v$.value.version.$reset();
      await v$.value.motor.$reset();
    }

    async function handleVersionChange() {
      await onVersionChange(vehiculo.value);
      v$.value.motor.$touch();
    }

    async function validarPatenteUnica() {
      patenteDuplicada.value = '';

      const patenteNormalizada = normalizarPatente(vehiculo.value.patente);
      vehiculo.value.patente = patenteNormalizada ?? '';

      if (!patenteNormalizada) {
        return !patenteRequerida.value;
      }

      if (!PATENTE_REGEX.test(patenteNormalizada)) {
        return false;
      }

      chequeandoPatente.value = true;
      try {
        const existente = await vehiculoService().findByPatente(patenteNormalizada);
        if (existente && (!vehiculo.value.id || existente.id !== vehiculo.value.id)) {
          patenteDuplicada.value = `La patente ${patenteNormalizada} ya esta registrada.`;
          return false;
        }

        return true;
      } catch (error: any) {
        if (error.response?.status === 404) {
          return true;
        }

        alertService.showHttpError(error.response);
        return false;
      } finally {
        chequeandoPatente.value = false;
      }
    }

    function buildPayload(): IVehiculo {
      return {
        id: vehiculo.value.id,
        patente: normalizarPatente(vehiculo.value.patente) ?? undefined,
        estado: vehiculo.value.estado,
        fechaFabricacion: vehiculo.value.fechaFabricacion,
        km: vehiculo.value.km,
        vinChasis: normalizarVinChasis(vehiculo.value.vinChasis) ?? null,
        color: vehiculo.value.color?.trim() || null,
        observaciones: vehiculo.value.observaciones?.trim() || null,
        precio: vehiculo.value.precio,
        moneda: vehiculo.value.moneda ? ({ id: vehiculo.value.moneda.id } as IMoneda) : null,
        version: vehiculo.value.version ? ({ id: vehiculo.value.version.id } as IVersion) : null,
        motor: vehiculo.value.motor ? ({ id: vehiculo.value.motor.id } as IMotor) : null,
        tipoVehiculo: vehiculo.value.tipoVehiculo ? ({ id: vehiculo.value.tipoVehiculo.id } as ITipoVehiculo) : null,
      };
    }

    async function initializeForm() {
      const [, monedasResult] = await Promise.all([
        cargarCatalogos(),
        monedaService().retrieve({ 'activo.equals': true, page: 0, size: 100, sort: ['codigo,asc'] }),
      ]);
      monedas.value = monedasResult.data ?? [];

      if (route.params?.vehiculoId) {
        await retrieveVehiculo(Number(route.params.vehiculoId));
        return;
      }

      if (!vehiculo.value.moneda && monedas.value.length > 0) {
        vehiculo.value.moneda = monedas.value[0];
      }

      await onVersionChange(vehiculo.value, true);
    }

    initializeForm();

    return {
      vehiculoService,
      alertService,
      vehiculo,
      inventarioAsociado,
      inventarioWarning,
      previousState,
      marcas,
      tipoVehiculos,
      selectedMarca,
      selectedModelo,
      modelosFiltrados,
      versionesFiltradas,
      motoresCompatibles,
      loadingMotores,
      motorHint,
      estadoVehiculoValues,
      monedas,
      patenteDuplicada,
      chequeandoPatente,
      isSaving,
      v$,
      motorValidationMessage,
      canSaveVehiculo,
      procesoVehiculo,
      catalogoCompleto,
      datosComercialesCompletos,
      patenteRequerida,
      patenteHint,
      handleMarcaChange,
      handleModeloChange,
      handleVersionChange,
      validarPatenteUnica,
      buildPayload,
      ...useDateFormat({ entityRef: vehiculo }),
    };
  },

  methods: {
    async save(): Promise<void> {
      this.v$.$touch();
      if (this.v$.$invalid || !this.canSaveVehiculo) return;

      const patenteDisponible = await this.validarPatenteUnica();
      if (!patenteDisponible) return;

      this.isSaving = true;

      try {
        const payload = this.buildPayload();
        const result = this.vehiculo.id ? await this.vehiculoService().update(payload) : await this.vehiculoService().create(payload);
        const identificador = result.patente || 'sin patente asignada';

        this.alertService.showSuccess(this.vehiculo.id ? `Vehiculo ${identificador} actualizado.` : `Vehiculo ${identificador} creado.`);

        this.previousState();
      } catch (error: any) {
        this.alertService.showHttpError(error.response);
      } finally {
        this.isSaving = false;
      }
    },
  },
});
