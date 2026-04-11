<template>
  <aside class="app-sidebar">

    <!-- BRAND -->
    <div class="sidebar-brand">
      <span class="brand-icon">🚗</span>
      <div>
        <span class="brand-name">AutoGestión</span>
        <span class="brand-sub">Sistema de Concesionaria</span>
      </div>
    </div>

    <!-- NAV -->
    <nav class="sidebar-nav">

      <!-- PRINCIPAL -->
      <div class="sidebar-section">Principal</div>

      <router-link class="sidebar-item" :to="{ name: 'Home' }">
        <span class="sidebar-icon">⊞</span>
        Dashboard
      </router-link>

      <!-- OPERACIONES (💣 LO IMPORTANTE) -->
      <div class="sidebar-section">Operaciones</div>

      <router-link class="sidebar-item" :to="{ name: 'VehiculoSearch' }">
        <span class="sidebar-icon">🔍</span>
        Buscar Vehículo
      </router-link>

      <router-link class="sidebar-item" :to="{ name: 'VentaEditor' }">
        <span class="sidebar-icon">💰</span>
        Nueva venta
      </router-link>



      <!-- VENTAS -->
      <router-link class="sidebar-item" :to="{ name: 'VentaList' }">
        <span class="sidebar-icon">📄</span>
        Ventas
      </router-link>

      <!-- CLIENTES -->
      <div class="sidebar-section">Clientes</div>

      <router-link class="sidebar-item" :to="{ name: 'Cliente' }">
        <span class="sidebar-icon">👤</span>
        Clientes
      </router-link>

      <router-link class="sidebar-item" :to="{ name: 'ClienteCreate' }">
        <span class="sidebar-icon">＋</span>
        Nuevo Cliente
      </router-link>

      <!-- ADMIN (OCULTO PARA NEGOCIO) -->
      <template v-if="isAdmin">
        <div class="sidebar-section">Administración</div>

        <router-link class="sidebar-item" :to="{ name: 'Vehiculo' }">
          🚙 Ir a Vehículos
        </router-link>

        <router-link class="sidebar-item" :to="{ name: 'Inventario' }">
          📦 Ir a Inventario
        </router-link>

        <router-link class="sidebar-item" :to="{ name: 'Cotizacion' }">
          <span class="sidebar-icon">💱</span>
          Cotizaciones
        </router-link>
      </template>

    </nav>

    <!-- FOOTER -->
    <div class="sidebar-footer">
      <div class="d-flex align-items-center gap-2">
        <div
          class="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white fw-bold"
          style="width:32px;height:32px;font-size:.75rem;flex-shrink:0"
        >
          {{ userInitials }}
        </div>

        <div style="min-width:0">
          <div class="text-white fw-semibold" style="font-size:.8rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
            {{ username ?? 'Usuario' }}
          </div>
          <div style="font-size:.7rem;color:rgba(255,255,255,.4)">Activo</div>
        </div>

        <button
          class="btn btn-sm ms-auto"
          style="color:rgba(255,255,255,.5);padding:.2rem .4rem"
          @click="logout"
        >
          ⏻
        </button>
      </div>
    </div>

  </aside>
</template>

<script lang="ts" setup>
import { computed, inject } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from '@/store';
import type AccountService from '@/account/account.service';

const store = useStore();
const router = useRouter();
inject<AccountService>('accountService');

const username = computed(() => store.account?.login);

const isAdmin = computed(() =>
  store.account?.authorities?.includes('ROLE_ADMIN') ?? false
);

const userInitials = computed(() => {
  const u = store.account?.login ?? '';
  return u.slice(0, 2).toUpperCase();
});

async function logout() {
  localStorage.removeItem('jhi-authenticationToken');
  sessionStorage.removeItem('jhi-authenticationToken');
  store.logout();
  router.push('/');
}
</script>

<style scoped>
.sidebar-subsection {
  font-size: 0.7rem;
  text-transform: uppercase;
  opacity: 0.6;
  margin: 0.5rem 0 0.2rem 1rem;
}

.sidebar-item.sub {
  padding-left: 2rem;
  font-size: 0.85rem;
  opacity: 0.85;
}
</style>
