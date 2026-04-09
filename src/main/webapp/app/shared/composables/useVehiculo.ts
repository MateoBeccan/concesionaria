import { ref } from 'vue';
import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import { CondicionVehiculo } from '@/shared/model/enumerations/condicion-vehiculo.model';

const PATENTE_REGEX = /^[A-Z]{3}\d{3}$|^[A-Z]{2}\d{3}[A-Z]{2}$/;

const service = new VehiculoService();

export function useVehiculo() {
  const vehiculo = ref<IVehiculo | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const notFound = ref(false);

  function validarPatente(patente: string): string | null {
    if (!patente.trim()) return 'Ingrese una patente';
    if (!PATENTE_REGEX.test(patente.trim().toUpperCase())) {
      return 'Formato inválido. Ejemplos: ABC123 o AB123CD';
    }
    return null;
  }

  async function buscarPorPatente(patente: string) {
    const p = patente.trim().toUpperCase();
    const validationError = validarPatente(p);
    if (validationError) {
      error.value = validationError;
      return;
    }
    loading.value = true;
    error.value = null;
    notFound.value = false;
    vehiculo.value = null;
    try {
      vehiculo.value = await service.findByPatente(p);
    } catch (e: any) {
      if (e.response?.status === 404) notFound.value = true;
      else error.value = 'Error al buscar el vehículo. Intente nuevamente.';
    } finally {
      loading.value = false;
    }
  }

  function setVehiculo(v: IVehiculo) {
    vehiculo.value = v;
    notFound.value = false;
    error.value = null;
  }

  function esVendido(v: IVehiculo): boolean {
    return v.condicion === CondicionVehiculo.VENDIDO;
  }

  function limpiar() {
    vehiculo.value = null;
    notFound.value = false;
    error.value = null;
  }

  return { vehiculo, loading, error, notFound, buscarPorPatente, validarPatente, setVehiculo, esVendido, limpiar };
}
