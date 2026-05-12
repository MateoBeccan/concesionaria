import { createRouter as createVueRouter, createWebHistory } from 'vue-router';
import AccountService from '@/account/account.service';
import { useStore } from '@/store';

import account from '@/router/account';
import admin from '@/router/admin';
import entities from '@/router/entities';
import pages from '@/router/pages';

const Home = () => import('@/core/home/home.vue');
const Login = () => import('@/account/login-form/login-form.vue');
const Error = () => import('@/core/error/error.vue');

export const createRouter = () =>
  createVueRouter({
    history: createWebHistory(),
    routes: [
      {
        path: '/login',
        name: 'Login',
        component: Login,
      },
      {
        path: '/',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true },
      },
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
      ...account,
      ...admin,
      entities,
      ...pages,
    ],
  });

const router = createRouter();

router.beforeEach(async to => {
  const store = useStore();
  const accountService = new AccountService(store);
  const requiresAuth = to.meta?.requiresAuth || Array.isArray(to.meta?.authorities);

  if (requiresAuth && !store.authenticated) {
    try {
      await accountService.update();
    } catch {
      // noop
    }
  }

  if (to.name === 'Login' && store.authenticated) {
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : null;
    return redirect ?? { name: 'Home' };
  }

  if (requiresAuth && !store.authenticated) {
    if (to.name === 'Login') return true;
    return {
      name: 'Login',
      query: to.fullPath ? { redirect: to.fullPath } : undefined,
    };
  }

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

router.beforeResolve(to => {
  if (!to.matched.length) return { name: 'NotFound' };
  return true;
});

export default router;
