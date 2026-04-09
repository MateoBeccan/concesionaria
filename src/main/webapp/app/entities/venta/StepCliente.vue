<template>
  <div>
    <p class="text-muted mb-3">Buscá por nombre, apellido, DNI o email.</p>

    <EntitySearchInput
      v-model="query"
      placeholder="Ej: Juan García o 20123456"
      :loading="loading"
      :error="error"
      @search="buscarConDebounce"
      @clear="limpiar"
    />

    <!-- RESULTADOS -->
    <ul v-if="resultados.length" class="list-group mt-2 shadow-sm">
      <li
        v-for="c in resultados"
        :key="c.id"
        class="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3"
        style="cursor:pointer"
        @click="seleccionarCliente(c)"
      >
        <div>
          <span class="fw-semibold">{{ c.nombre }} {{ c.apellido }}</span>
          <span class="badge bg-light text-dark border ms-2">{{ c.nroDocumento }}</span>
        </div>
        <small class="text-muted">{{ c.email }}</small>
      </li>
    </ul>

    <!-- NO ENCONTRADO -->
    <div v-if="notFound && !loading" class="alert alert-warning mt-3 d-flex justify-content-between align-items-center">
      <div>
        <strong>Sin resultados</strong>
        <p class="mb-0 small">No se encontró ningún cliente con "{{ query }}"</p>
      </div>
      <button class="btn btn-sm btn-success" @click="mostrarModal = true">
        + Crear cliente
      </button>
    </div>

    <!-- CLIENTE SELECCIONADO -->
    <div v-if="clienteSeleccionado" class="card border-success mt-3">
      <div class="card-body d-flex justify-content-between align-items-center py-2">
        <div>
          <span class="text-success me-2">✓</span>
          <strong>{{ clienteSeleccionado.nombre }} {{ clienteSeleccionado.apellido }}</strong>
          <span class="text-muted ms-2 small">{{ clienteSeleccionado.nroDocumento }}</span>
        </div>
        <button class="btn btn-sm btn-outline-secondary" @click="deseleccionar">Cambiar</button>
      </div>
    </div>

    <!-- BOTÓN SIGUIENTE -->
    <div class="d-flex justify-content-end mt-4">
      <button
        class="btn btn-primary px-4"
        :disabled="!clienteSeleccionado"
        @click="emit('seleccionado', clienteSeleccionado!)"
      >
        Siguiente →
      </button>
    </div>

    <!-- MODAL CREACIÓN RÁPIDA -->
    <ClienteQuickCreate
      v-if="mostrarModal"
      @guardado="onClienteCreado"
      @cerrar="mostrarModal = false"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useCliente } from '@/shared/composables/useCliente';
import EntitySearchInput from '@/shared/composables/EntitySearchInput.vue';
import ClienteQuickCreate from '@/entities/cliente/ClienteQuickCreate.vue';
import type { ICliente } from '@/shared/model/cliente.model';

const emit = defineEmits<{ seleccionado: [c: ICliente] }>();

const query = ref('');
const mostrarModal = ref(false);
const { resultados, clienteSeleccionado, loading, error, notFound, buscarConDebounce, seleccionar, limpiar } = useCliente();

function seleccionarCliente(c: ICliente) {
  seleccionar(c);
  query.value = `${c.nombre} ${c.apellido}`;
}

function deseleccionar() {
  limpiar();
  query.value = '';
}

function onClienteCreado(c: ICliente) {
  mostrarModal.value = false;
  seleccionarCliente(c);
}
</script>
