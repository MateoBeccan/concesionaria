import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import MetodoPagoDetails from './metodo-pago-details.vue';
import MetodoPagoService from './metodo-pago.service';

type MetodoPagoDetailsComponentType = InstanceType<typeof MetodoPagoDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const metodoPagoSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('MetodoPago Management Detail Component', () => {
    let metodoPagoServiceStub: SinonStubbedInstance<MetodoPagoService>;
    let mountOptions: MountingOptions<MetodoPagoDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      metodoPagoServiceStub = sinon.createStubInstance<MetodoPagoService>(MetodoPagoService);

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
          metodoPagoService: () => metodoPagoServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        metodoPagoServiceStub.find.resolves(metodoPagoSample);
        route = {
          params: {
            metodoPagoId: `${123}`,
          },
        };
        const wrapper = shallowMount(MetodoPagoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.metodoPago).toMatchObject(metodoPagoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        metodoPagoServiceStub.find.resolves(metodoPagoSample);
        const wrapper = shallowMount(MetodoPagoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
