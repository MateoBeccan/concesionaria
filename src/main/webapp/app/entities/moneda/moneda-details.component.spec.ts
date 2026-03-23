import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import MonedaDetails from './moneda-details.vue';
import MonedaService from './moneda.service';

type MonedaDetailsComponentType = InstanceType<typeof MonedaDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const monedaSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Moneda Management Detail Component', () => {
    let monedaServiceStub: SinonStubbedInstance<MonedaService>;
    let mountOptions: MountingOptions<MonedaDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      monedaServiceStub = sinon.createStubInstance<MonedaService>(MonedaService);

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
          monedaService: () => monedaServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        monedaServiceStub.find.resolves(monedaSample);
        route = {
          params: {
            monedaId: `${123}`,
          },
        };
        const wrapper = shallowMount(MonedaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.moneda).toMatchObject(monedaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        monedaServiceStub.find.resolves(monedaSample);
        const wrapper = shallowMount(MonedaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
