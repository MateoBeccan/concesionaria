import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TraccionDetails from './traccion-details.vue';
import TraccionService from './traccion.service';

type TraccionDetailsComponentType = InstanceType<typeof TraccionDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const traccionSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Traccion Management Detail Component', () => {
    let traccionServiceStub: SinonStubbedInstance<TraccionService>;
    let mountOptions: MountingOptions<TraccionDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      traccionServiceStub = sinon.createStubInstance<TraccionService>(TraccionService);

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
          traccionService: () => traccionServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        traccionServiceStub.find.resolves(traccionSample);
        route = {
          params: {
            traccionId: `${123}`,
          },
        };
        const wrapper = shallowMount(TraccionDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.traccion).toMatchObject(traccionSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        traccionServiceStub.find.resolves(traccionSample);
        const wrapper = shallowMount(TraccionDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
