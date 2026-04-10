import { createRouter as createVueRouter, createWebHistory } from 'vue-router';
import { useStore } from '@/store';

// 🔥 VISTAS
const Home = () => import('@/core/home/home.vue');
const Login = () => import('@/account/login-form/login-form.vue'); // ⚠ ajustá si tu ruta es distinta
const Error = () => import('@/core/error/error.vue');

// 🔥 MÓDULOS
import account from '@/router/account';
import admin from '@/router/admin';
import entities from '@/router/entities';
import pages from '@/router/pages';

// ROUTER
export const createRouter = () =>
  createVueRouter({
    history: createWebHistory(),
    routes: [
      // LOGIN / PÚBLICO
      {
        path: '/login',
        name: 'Login',
        component: Login,
      },

      // DASHBOARD (PROTEGIDO)
      {
        path: '/',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true },
      },

      // ERRORES
      {
        path: '/forbidden',
        name: 'Forbidden',
        component: Error,
        meta: { error403: true },
      },
      {
        path: '/not-found',
        name: 'NotFound',
        component: Error,
        meta: { error404: true },
      },

      // 🔥 RESTO DEL SISTEMA (PROTEGIDO)
      ...account,
      ...admin,
      entities,
      ...pages,
    ],
  });

const router = createRouter();


// 🔥 🔒 GUARD GLOBAL (LA CLAVE DEL PROBLEMA)
router.beforeEach((to) => {
  const store = useStore();

  // ❌ No autenticado → bloquear acceso
  if (to.meta?.requiresAuth && !store.authenticated) {
    return { name: 'Login' };
  }

  // ✔ Si está logueado y quiere ir a login → redirigir a home
  if (to.name === 'Login' && store.authenticated) {
    return { name: 'Home' };
  }

  return true;
});


// 🔥 NOT FOUND
router.beforeResolve((to) => {
  if (!to.matched.length) {
    return { path: '/not-found' };
  }
});

export default router;
