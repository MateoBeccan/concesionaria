import { computed, ref } from 'vue';

import MarcaService from '@/entities/marca/marca.service';
import ModeloService from '@/entities/modelo/modelo.service';
import TipoVehiculoService from '@/entities/tipo-vehiculo/tipo-vehiculo.service';
import VersionService from '@/entities/version/version.service';
import type { IMarca } from '@/shared/model/marca.model';
import type { IModelo } from '@/shared/model/modelo.model';
import type { IMotor } from '@/shared/model/motor.model';
import type { ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';
import type { IVersion } from '@/shared/model/version.model';

type VehiculoLike = {
  version?: IVersion | null;
  motor?: IMotor | null;
  tipoVehiculo?: ITipoVehiculo | null;
};

type WithId = {
  id?: number;
};

type ServiceOptions = {
  marcaService: MarcaService;
  modeloService: ModeloService;
  versionService: VersionService;
  tipoVehiculoService?: TipoVehiculoService;
  onError?: (error: any) => void;
};

export function inferirTipoVehiculo(modelo: IModelo | null | undefined, tipos: ITipoVehiculo[]): ITipoVehiculo | null {
  const carroceria = modelo?.carroceria?.nombre?.toLowerCase() ?? '';

  if (carroceria.includes('pickup')) {
    return tipos.find(tipo => tipo.nombre?.toLowerCase() === 'camioneta') ?? null;
  }

  if (carroceria.includes('suv') || carroceria.includes('crossover')) {
    return tipos.find(tipo => tipo.nombre?.toLowerCase() === 'suv') ?? null;
  }

  if (carroceria) {
    return tipos.find(tipo => tipo.nombre?.toLowerCase() === 'auto') ?? null;
  }

  return null;
}

export function useVehiculoForm({ marcaService, modeloService, versionService, tipoVehiculoService, onError }: ServiceOptions) {
  const marcas = ref<IMarca[]>([]);
  const modelos = ref<IModelo[]>([]);
  const versions = ref<IVersion[]>([]);
  const tipoVehiculos = ref<ITipoVehiculo[]>([]);
  const motoresCompatibles = ref<IMotor[]>([]);

  const selectedMarca = ref<IMarca | null>(null);
  const selectedModelo = ref<IModelo | null>(null);

  const loadingMotores = ref(false);
  const motorHint = ref('');

  const modelosFiltrados = computed(() => modelos.value.filter(modelo => modelo.marca?.id === selectedMarca.value?.id));
  const versionesFiltradas = computed(() => versions.value.filter(version => version.modelo?.id === selectedModelo.value?.id));

  const limpiarMotores = () => {
    motoresCompatibles.value = [];
    motorHint.value = '';
  };

  const findById = <T extends WithId>(items: T[], id?: number | null): T | null => {
    if (!id) {
      return null;
    }

    return items.find(item => item.id === id) ?? null;
  };

  const syncSelectionFromVehiculo = (vehiculo: VehiculoLike) => {
    const version = findById(versions.value, vehiculo.version?.id) ?? vehiculo.version ?? null;
    const modelo = findById(modelos.value, version?.modelo?.id) ?? version?.modelo ?? vehiculo.version?.modelo ?? null;
    const marca = findById(marcas.value, modelo?.marca?.id) ?? modelo?.marca ?? version?.modelo?.marca ?? null;

    vehiculo.version = version;
    selectedModelo.value = modelo;
    selectedMarca.value = marca;

    if (vehiculo.tipoVehiculo?.id) {
      vehiculo.tipoVehiculo = findById(tipoVehiculos.value, vehiculo.tipoVehiculo.id) ?? vehiculo.tipoVehiculo;
    }

    if (!vehiculo.tipoVehiculo && selectedModelo.value && tipoVehiculos.value.length > 0) {
      vehiculo.tipoVehiculo = inferirTipoVehiculo(selectedModelo.value, tipoVehiculos.value);
    }
  };

  const loadCompatibleMotors = async (versionId?: number | null, currentMotorId?: number | null) => {
    limpiarMotores();

    if (!versionId) {
      return;
    }

    loadingMotores.value = true;
    try {
      const motores = await versionService.retrieveMotors(versionId);
      motoresCompatibles.value = motores ?? [];

      if (motoresCompatibles.value.length === 0) {
        motorHint.value = 'La versión seleccionada no tiene motores configurados.';
      } else if (currentMotorId && !motoresCompatibles.value.some(motor => motor.id === currentMotorId)) {
        motorHint.value = 'El motor guardado ya no es válido para esta versión. Seleccioná uno compatible.';
      }
    } catch (error: any) {
      limpiarMotores();
      onError?.(error);
    } finally {
      loadingMotores.value = false;
    }
  };

  const syncVehicleVersion = async (vehiculo: VehiculoLike, preserveCurrentMotor = true) => {
    syncSelectionFromVehiculo(vehiculo);

    const currentMotorId = preserveCurrentMotor ? vehiculo.motor?.id : null;
    if (!preserveCurrentMotor) {
      vehiculo.motor = null;
    }

    await loadCompatibleMotors(vehiculo.version?.id ?? null, currentMotorId);

    if (currentMotorId) {
      vehiculo.motor = findById(motoresCompatibles.value, currentMotorId);
    }

    if (vehiculo.motor?.id && !motoresCompatibles.value.some(motor => motor.id === vehiculo.motor?.id)) {
      vehiculo.motor = null;
    }
  };

  const onMarcaChange = (vehiculo: VehiculoLike, resetTipoVehiculo = true) => {
    selectedModelo.value = null;
    vehiculo.version = null;
    vehiculo.motor = null;
    limpiarMotores();

    if (resetTipoVehiculo) {
      vehiculo.tipoVehiculo = null;
    }
  };

  const onModeloChange = (vehiculo: VehiculoLike, resetTipoVehiculo = true) => {
    vehiculo.version = null;
    vehiculo.motor = null;
    limpiarMotores();

    if (resetTipoVehiculo && tipoVehiculos.value.length > 0) {
      vehiculo.tipoVehiculo = inferirTipoVehiculo(selectedModelo.value, tipoVehiculos.value);
    }
  };

  const onVersionChange = async (vehiculo: VehiculoLike, preserveCurrentMotor = false) => {
    await syncVehicleVersion(vehiculo, preserveCurrentMotor);
  };

  const cargarCatalogos = async (vehiculo?: VehiculoLike) => {
    const requests: Promise<any>[] = [
      marcaService.retrieve({ page: 0, size: 200, sort: ['nombre,asc'] }),
      modeloService.retrieve({ page: 0, size: 300, sort: ['nombre,asc'] }),
      versionService.retrieve({ page: 0, size: 400, sort: ['nombre,asc'] }),
    ];

    if (tipoVehiculoService) {
      requests.push(tipoVehiculoService.retrieve({ page: 0, size: 100, sort: ['nombre,asc'] }));
    }

    const [marcasRes, modelosRes, versionsRes, tiposRes] = await Promise.all(requests);
    marcas.value = marcasRes.data ?? [];
    modelos.value = modelosRes.data ?? [];
    versions.value = versionsRes.data ?? [];
    tipoVehiculos.value = tiposRes?.data ?? [];

    if (vehiculo) {
      await syncVehicleVersion(vehiculo, true);
    }
  };

  const isMotorCompatible = (motorId?: number | null) => {
    if (!motorId) {
      return false;
    }

    return motoresCompatibles.value.some(motor => motor.id === motorId);
  };

  return {
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
    syncVehicleVersion,
    onMarcaChange,
    onModeloChange,
    onVersionChange,
    isMotorCompatible,
  };
}
