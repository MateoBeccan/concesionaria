<template>
  <header class="app-topbar">

    <!-- TÍTULO DE PÁGINA (slot) -->
    <div class="flex-grow-1">
      <slot name="title">
        <span class="fw-semibold" style="font-size:.95rem;color:var(--color-text)">{{ pageTitle }}</span>
      </slot>
    </div>

    <!-- ACCIONES RÁPIDAS -->
    <div class="d-flex align-items-center gap-3">

      <!-- Botón nueva venta -->
      <router-link
        v-if="authenticated"
        :to="{ name: 'VentaWizard' }"
        class="btn btn-primary btn-sm d-none d-md-flex align-items-center gap-1"
      >
        <span>＋</span> Nueva Venta
      </router-link>

      <!-- Notificaciones (placeholder) -->
      <button
        class="btn btn-sm"
        style="color:var(--color-text-muted);position:relative;padding:.35rem .5rem"
        title="Notificaciones"
      >
        🔔
        <span
          class="position-absolute top-0 end-0 translate-middle badge rounded-pill bg-danger"
          style="font-size:.55rem;padding:.2em .4em"
        >
          3
        </span>
      </button>

      <!-- Usuario -->
      <div v-if="authenticated" class="d-flex align-items-center gap-2" style="cursor:default">
        <div
          class="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white fw-bold"
          style="width:32px;height:32px;font-size:.75rem;flex-shrink:0"
        >
          {{ userInitials }}
        </div>
        <span class="d-none d-md-block fw-semibold" style="font-size:.85rem">{{ username }}</span>
      </div>

      <!-- Login si no autenticado -->
      <button v-else class="btn btn-primary btn-sm" @click="showLogin()">
        Iniciar sesión
      </button>

    </div>
  </header>
</template>

<script lang="ts" setup>
import { computed, inject } from 'vue';
import type { ComputedRef } from 'vue';
import { useRoute } from 'vue-router';
import { useStore } from '@/store';
import { useLoginModal } from '@/account/login-modal';

const store = useStore();
const route = useRoute();
const { showLogin } = useLoginModal();

const authenticated = computed(() => store.authenticated);
const username = computed(() => store.account?.login);
const userInitials = computed(() => (store.account?.login ?? '').slice(0, 2).toUpperCase());

const PAGE_TITLES: Record<string, string> = {
  Home:          'Dashboard',
  VentaWizard:   'Nueva Venta',
  VentaList:     'Ventas',
  VehiculoSearch:'Buscar Vehículo',
  Vehiculo:      'Vehículos',
  Cliente:       'Clientes',
  Inventario:    'Inventario',
  Marca:         'Marcas',
  Modelo:        'Modelos',
};

const pageTitle = computed(() => PAGE_TITLES[route.name as string] ?? '');
</script>
