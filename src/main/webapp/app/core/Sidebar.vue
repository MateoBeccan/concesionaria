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

      <!-- OPERACIONES -->
      <div class="sidebar-section">Operaciones</div>

      <router-link class="sidebar-item" :to="{ name: 'VentaEditor' }">
        <span class="sidebar-icon">＋</span>
        Nueva Venta
      </router-link>

      <router-link class="sidebar-item" :to="{ name: 'VentaList' }">
        <span class="sidebar-icon">◈</span>
        Ventas
      </router-link>

      <router-link class="sidebar-item" :to="{ name: 'VehiculoSearch' }">
        <span class="sidebar-icon">⌕</span>
        Buscar Vehículo
      </router-link>

      <!-- CATÁLOGO -->
      <div class="sidebar-section">Catálogo</div>

      <router-link class="sidebar-item" :to="{ name: 'Vehiculo' }">
        <span class="sidebar-icon">🚙</span>
        Vehículos
      </router-link>

      <router-link class="sidebar-item" :to="{ name: 'Cliente' }">
        <span class="sidebar-icon">👤</span>
        Clientes
      </router-link>

      <router-link class="sidebar-item" :to="{ name: 'Inventario' }">
        <span class="sidebar-icon">📦</span>
        Inventario
      </router-link>

      <!-- CONFIGURACIÓN -->
      <div class="sidebar-section">Configuración</div>

      <router-link class="sidebar-item" :to="{ name: 'Marca' }">
        <span class="sidebar-icon">◎</span>
        Marcas
      </router-link>

      <router-link class="sidebar-item" :to="{ name: 'Modelo' }">
        <span class="sidebar-icon">◎</span>
        Modelos
      </router-link>

      <router-link class="sidebar-item" :to="{ name: 'Motor' }">
        <span class="sidebar-icon">◎</span>
        Motores
      </router-link>

      <template v-if="isAdmin">
        <div class="sidebar-section">Administración</div>
        <router-link class="sidebar-item" :to="{ name: 'JhiUser' }">
          <span class="sidebar-icon">👥</span>
          Usuarios
        </router-link>
        <router-link class="sidebar-item" :to="{ name: 'JhiHealthComponent' }">
          <span class="sidebar-icon">♥</span>
          Sistema
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
          title="Cerrar sesión"
        >
          ⏻
        </button>
      </div>
    </div>

  </aside>
</template>

<script lang="ts" setup>
import { computed, inject } from 'vue';
import type { ComputedRef } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from '@/store';
import type AccountService from '@/account/account.service';

const store = useStore();
const router = useRouter();
const accountService = inject<AccountService>('accountService');

const username = computed(() => store.account?.login);
const isAdmin = computed(() => store.account?.authorities?.includes('ROLE_ADMIN') ?? false);
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
