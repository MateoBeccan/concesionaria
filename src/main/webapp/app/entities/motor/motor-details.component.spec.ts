import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import MotorDetails from './motor-details.vue';
import MotorService from './motor.service';

type MotorDetailsComponentType = InstanceType<typeof MotorDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const motorSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Motor Management Detail Component', () => {
    let motorServiceStub: SinonStubbedInstance<MotorService>;
    let mountOptions: MountingOptions<MotorDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      motorServiceStub = sinon.createStubInstance<MotorService>(MotorService);

      alertService = new AlertService({
        toast: {
          show: vitest.fn(),
        } as any,
      });

      mountOptions = {
        stubs: {
          'font-awesome-icon': true,
          'router-link': true,
        },
        provide: {
          alertService,
          motorService: () => motorServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        motorServiceStub.find.resolves(motorSample);
        route = {
          params: {
            motorId: `${123}`,
          },
        };
        const wrapper = shallowMount(MotorDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.motor).toMatchObject(motorSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        motorServiceStub.find.resolves(motorSample);
        const wrapper = shallowMount(MotorDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
