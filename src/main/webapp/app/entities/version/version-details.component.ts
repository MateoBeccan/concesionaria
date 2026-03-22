import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type IVersion } from '@/shared/model/version.model';

import VersionService from './version.service';

export default defineComponent({
  name: 'VersionDetails',
  setup() {
    const versionService = inject('versionService', () => new VersionService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const version: Ref<IVersion> = ref({});

    const retrieveVersion = async versionId => {
      try {
        const res = await versionService().find(versionId);
        version.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.versionId) {
      retrieveVersion(route.params.versionId);
    }

    return {
      alertService,
      version,

      previousState,
    };
  },
});
