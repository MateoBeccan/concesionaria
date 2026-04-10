
import { computed, createApp, provide } from 'vue';
import { createPinia, storeToRefs } from 'pinia';

import AccountService from '@/account/account.service';
import { setupAxiosInterceptors } from '@/shared/config/axios-interceptor';
import { initFortAwesome } from '@/shared/config/config';
import { initBootstrapVue } from '@/shared/config/config-bootstrap-vue';
import JhiItemCount from '@/shared/jhi-item-count.vue';
import JhiSortIndicator from '@/shared/sort/jhi-sort-indicator.vue';
import { useStore } from '@/store';

import App from './app.vue';
import router from './router';

import '../content/scss/global.scss';
import '../content/scss/vendor.scss';

const pinia = createPinia();

const app = createApp({
  components: { App },

  setup() {
    const store = useStore();
    const accountService = new AccountService(store);

    // 🌐 Idioma
    provide(
      'currentLanguage',
      computed(() => store.account?.langKey ?? navigator.language ?? 'es'),
    );

    // 👤 Usuario
    provide(
      'currentUsername',
      computed(() => store.account?.login),
    );

    // 🔐 Estado auth
    const { authenticated } = storeToRefs(store);
    provide('authenticated', authenticated);

    // 🔥 Account service
    provide('accountService', accountService);

    // 🚀 🔒 GUARD GLOBAL (CLAVE)
    router.beforeEach(async (to) => {
      // 🔄 Intentar recuperar sesión si no está cargada
      if (!store.authenticated) {
        try {
          await accountService.update();
        } catch {
          // ignore
        }
      }

      // 🔒 Rutas protegidas
      if (to.meta?.requiresAuth && !store.authenticated) {
        return { name: 'Login' };
      }

      // 🔁 Evitar ir al login si ya está logueado
      if (to.name === 'Login' && store.authenticated) {
        return { name: 'Home' };
      }

      return true;
    });

    // 🚀 🔥 AXIOS INTERCEPTOR GLOBAL
    setupAxiosInterceptors(
      error => {
        const status = error.response?.status;

        if (status === 401) {
          // 🔥 LOGOUT GLOBAL
          store.logout();

          // 🔁 Redirigir al login
          router.replace({ name: 'Login' });
        }

        return Promise.reject(error);
      },
      error => Promise.reject(error),
    );
  },

  template: '<App/>',
});

// 🔧 Plugins
initFortAwesome(app);
initBootstrapVue(app);

// 🔧 Global components
app
  .component('JhiItemCount', JhiItemCount)
  .component('JhiSortIndicator', JhiSortIndicator)
  .use(router)
  .use(pinia)
  .mount('#app');
