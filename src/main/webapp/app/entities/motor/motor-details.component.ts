import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type IMotor } from '@/shared/model/motor.model';

import MotorService from './motor.service';

export default defineComponent({
  name: 'MotorDetails',
  setup() {
    const motorService = inject('motorService', () => new MotorService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const motor: Ref<IMotor> = ref({});

    const retrieveMotor = async motorId => {
      try {
        const res = await motorService().find(motorId);
        motor.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.motorId) {
      retrieveMotor(route.params.motorId);
    }

    return {
      alertService,
      motor,

      previousState,
    };
  },
});
