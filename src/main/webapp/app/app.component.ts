import { computed, defineComponent, provide } from 'vue';

import { BToastOrchestrator } from 'bootstrap-vue-next';
import { storeToRefs } from 'pinia';

import LoginForm from '@/account/login-form/login-form.vue';
import { useLoginModal } from '@/account/login-modal';
import AppLayout from '@/core/AppLayout.vue';
import Ribbon from '@/core/ribbon/ribbon.vue';
import { useAlertService } from '@/shared/alert/alert.service';
import { useStore } from '@/store';
import '@/shared/config/dayjs';

export default defineComponent({
  name: 'App',
  components: {
    BToastOrchestrator,
    AppLayout,
    Ribbon,
    LoginForm,
  },
  setup() {
    provide('alertService', useAlertService());
    const { loginModalOpen } = storeToRefs(useLoginModal());
    const store = useStore();
    const authenticated = computed(() => store.authenticated);

    return {
      loginModalOpen,
      authenticated,
    };
  },
});
