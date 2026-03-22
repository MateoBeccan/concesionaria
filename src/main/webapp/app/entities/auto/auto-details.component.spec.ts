import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import AutoDetails from './auto-details.vue';
import AutoService from './auto.service';

type AutoDetailsComponentType = InstanceType<typeof AutoDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const autoSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Auto Management Detail Component', () => {
    let autoServiceStub: SinonStubbedInstance<AutoService>;
    let mountOptions: MountingOptions<AutoDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      autoServiceStub = sinon.createStubInstance<AutoService>(AutoService);

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
          autoService: () => autoServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        autoServiceStub.find.resolves(autoSample);
        route = {
          params: {
            autoId: `${123}`,
          },
        };
        const wrapper = shallowMount(AutoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.auto).toMatchObject(autoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        autoServiceStub.find.resolves(autoSample);
        const wrapper = shallowMount(AutoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
