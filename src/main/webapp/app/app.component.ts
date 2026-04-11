import { computed, defineComponent, provide } from 'vue';
import { useRoute } from 'vue-router';

import { BToastOrchestrator } from 'bootstrap-vue-next';
import { storeToRefs } from 'pinia';

import LoginForm from '@/account/login-form/login-form.vue';
import { useLoginModal } from '@/account/login-modal';
import AppLayout from '@/core/AppLayout.vue';
import PublicLayout from '@/core/PublicLayout.vue';
import Ribbon from '@/core/ribbon/ribbon.vue';
import { useAlertService } from '@/shared/alert/alert.service';
import { useStore } from '@/store';
import '@/shared/config/dayjs';

const PUBLIC_LAYOUT_ROUTE_NAMES = new Set(['Login', 'Register', 'Activate', 'ResetPasswordInit', 'ResetPasswordFinish']);

export default defineComponent({
  name: 'App',
  components: {
    BToastOrchestrator,
    AppLayout,
    PublicLayout,
    Ribbon,
    LoginForm,
  },
  setup() {
    provide('alertService', useAlertService());
    const { loginModalOpen } = storeToRefs(useLoginModal());
    const store = useStore();
    const route = useRoute();
    const authenticated = computed(() => store.authenticated);
    const useAuthenticatedLayout = computed(() => authenticated.value && !PUBLIC_LAYOUT_ROUTE_NAMES.has(String(route.name ?? '')));

    return {
      loginModalOpen,
      authenticated,
      useAuthenticatedLayout,
    };
  },
});
