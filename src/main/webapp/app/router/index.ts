import { createRouter as createVueRouter, createWebHistory } from 'vue-router';
import AccountService from '@/account/account.service';
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

// 🔥 🔒 GUARD GLOBAL CENTRALIZADO
router.beforeEach(async to => {
  const store = useStore();
  const accountService = new AccountService(store);
  const requiresAuth = to.meta?.requiresAuth || Array.isArray(to.meta?.authorities);

  // 🔄 Intentar recuperar sesión si la ruta lo requiere y aún no está autenticado
  if (requiresAuth && !store.authenticated) {
    try {
      await accountService.update();
    } catch {
      // ignore
    }
  }

  // 🔁 Evitar ir al login si ya está logueado
  if (to.name === 'Login' && store.authenticated) {
    return { name: 'Home' };
  }

  // ❌ Ruta protegida sin sesión
  if (requiresAuth && !store.authenticated) {
    if (to.name === 'Login') return true;
    return {
      name: 'Login',
      query: to.fullPath ? { redirect: to.fullPath } : undefined,
    };
  }

  // ⛔ Sesión válida pero sin permisos
  const requiredAuthorities = to.meta?.authorities as string[] | undefined;
  if (requiredAuthorities?.length) {
    const hasAnyAuthority = requiredAuthorities.some(authority => store.account?.authorities?.includes(authority));
    if (!hasAnyAuthority) {
      if (to.name === 'Forbidden') return true;
      return { name: 'Forbidden' };
    }
  }

  return true;
});

// 🔥 NOT FOUND
router.beforeResolve(to => {
  if (!to.matched.length) {
    return { path: '/not-found' };
  }
});

export default router;
