import { computed, defineComponent } from 'vue';
import { Authority } from '@/shared/jhipster/constants';
import { useStore } from '@/store';

export default defineComponent({
  name: 'EntitiesMenu',
  setup() {
    const store = useStore();
    const isAdmin = computed(() => (store.account?.authorities ?? []).includes(Authority.ADMIN));
    return { isAdmin };
  },
});
