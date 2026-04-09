import { ref } from 'vue';
import ClienteService from '@/entities/cliente/cliente.service';
import type { ICliente } from '@/shared/model/cliente.model';

const service = new ClienteService();

export function useCliente() {
  const resultados = ref<ICliente[]>([]);
  const clienteSeleccionado = ref<ICliente | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const notFound = ref(false);

  let debounceTimer: ReturnType<typeof setTimeout> | null = null;

  function buscarConDebounce(query: string, delay = 350) {
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => buscar(query), delay);
  }

  async function buscar(query: string) {
    const q = query.trim();
    if (q.length < 2) {
      resultados.value = [];
      notFound.value = false;
      return;
    }
    loading.value = true;
    error.value = null;
    notFound.value = false;
    try {
      resultados.value = await service.buscarPorQuery(q);
      notFound.value = resultados.value.length === 0;
    } catch {
      error.value = 'Error al buscar clientes. Intente nuevamente.';
      resultados.value = [];
    } finally {
      loading.value = false;
    }
  }

  function seleccionar(c: ICliente) {
    clienteSeleccionado.value = c;
    resultados.value = [];
    notFound.value = false;
    error.value = null;
  }

  function limpiar() {
    clienteSeleccionado.value = null;
    resultados.value = [];
    notFound.value = false;
    error.value = null;
  }

  return { resultados, clienteSeleccionado, loading, error, notFound, buscar, buscarConDebounce, seleccionar, limpiar };
}
