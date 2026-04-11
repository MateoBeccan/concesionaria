<template>
  <div class="d-grid gap-3">
    <div v-if="clienteSeleccionado" class="border rounded-3 p-3 bg-light">
      <div class="d-flex justify-content-between align-items-start gap-3">
        <div>
          <div class="fw-semibold">{{ clienteSeleccionado.nombre }} {{ clienteSeleccionado.apellido }}</div>
          <div class="text-muted small">
            {{ clienteSeleccionado.nroDocumento }}{{ clienteSeleccionado.email ? ` · ${clienteSeleccionado.email}` : '' }}
          </div>
        </div>
        <button class="btn btn-sm btn-outline-secondary" @click="limpiarSeleccion">Cambiar</button>
      </div>
    </div>

    <template v-else>
      <div class="d-flex gap-2">
        <input v-model="query" class="form-control" placeholder="Buscar por nombre, DNI o email" @input="buscarConDebounce(query)" />
        <button class="btn btn-outline-secondary" type="button" @click="limpiarBusqueda">Limpiar</button>
      </div>

      <div v-if="loading" class="text-muted small">Buscando clientes...</div>
      <div v-else-if="error" class="alert alert-danger py-2 mb-0">{{ error }}</div>

      <div v-if="resultados.length" class="list-group shadow-sm">
        <button
          v-for="cliente in resultados"
          :key="cliente.id"
          type="button"
          class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
          @click="seleccionarCliente(cliente)"
        >
          <span class="fw-semibold">{{ cliente.nombre }} {{ cliente.apellido }}</span>
          <span class="text-muted small">{{ cliente.nroDocumento }}</span>
        </button>
      </div>

      <div
        v-else-if="notFound && query.trim().length >= 2"
        class="alert alert-warning mb-0 d-flex justify-content-between align-items-center"
      >
        <span>No encontramos clientes con "{{ query }}".</span>
        <button class="btn btn-sm btn-success" type="button" @click="mostrarAlta = true">+ Crear cliente</button>
      </div>
    </template>

    <div class="d-flex justify-content-between gap-2 pt-2">
      <button class="btn btn-outline-secondary" type="button" :disabled="!clienteSeleccionado" @click="limpiarSeleccion">Reiniciar</button>
      <button class="btn btn-primary" type="button" :disabled="!clienteSeleccionado" @click="confirmarSeleccion">Continuar</button>
    </div>

    <ClienteQuickCreate v-if="mostrarAlta" @cerrar="mostrarAlta = false" @guardado="onClienteGuardado" />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useCliente } from '@/shared/composables';
import ClienteQuickCreate from '@/entities/cliente/ClienteQuickCreate.vue';
import type { ICliente } from '@/shared/model/cliente.model';

const emit = defineEmits<{ seleccionado: [cliente: ICliente] }>();

const query = ref('');
const mostrarAlta = ref(false);

const { resultados, clienteSeleccionado, loading, error, notFound, buscarConDebounce, seleccionar, limpiar } = useCliente();

function seleccionarCliente(cliente: ICliente) {
  seleccionar(cliente);
}

function limpiarBusqueda() {
  query.value = '';
  limpiar();
}

function limpiarSeleccion() {
  limpiarBusqueda();
}

function confirmarSeleccion() {
  if (!clienteSeleccionado.value) return;
  emit('seleccionado', clienteSeleccionado.value);
}

function onClienteGuardado(cliente: ICliente) {
  mostrarAlta.value = false;
  query.value = `${cliente.nombre ?? ''} ${cliente.apellido ?? ''}`.trim();
  seleccionar(cliente);
  emit('seleccionado', cliente);
}
</script>
