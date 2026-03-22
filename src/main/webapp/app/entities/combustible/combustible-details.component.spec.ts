import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import CombustibleDetails from './combustible-details.vue';
import CombustibleService from './combustible.service';

type CombustibleDetailsComponentType = InstanceType<typeof CombustibleDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const combustibleSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Combustible Management Detail Component', () => {
    let combustibleServiceStub: SinonStubbedInstance<CombustibleService>;
    let mountOptions: MountingOptions<CombustibleDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      combustibleServiceStub = sinon.createStubInstance<CombustibleService>(CombustibleService);

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
          combustibleService: () => combustibleServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        combustibleServiceStub.find.resolves(combustibleSample);
        route = {
          params: {
            combustibleId: `${123}`,
          },
        };
        const wrapper = shallowMount(CombustibleDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.combustible).toMatchObject(combustibleSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        combustibleServiceStub.find.resolves(combustibleSample);
        const wrapper = shallowMount(CombustibleDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
