import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import PagoDetails from './pago-details.vue';
import PagoService from './pago.service';

type PagoDetailsComponentType = InstanceType<typeof PagoDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const pagoSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Pago Management Detail Component', () => {
    let pagoServiceStub: SinonStubbedInstance<PagoService>;
    let mountOptions: MountingOptions<PagoDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      pagoServiceStub = sinon.createStubInstance<PagoService>(PagoService);

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
          pagoService: () => pagoServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        pagoServiceStub.find.resolves(pagoSample);
        route = {
          params: {
            pagoId: `${123}`,
          },
        };
        const wrapper = shallowMount(PagoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.pago).toMatchObject(pagoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        pagoServiceStub.find.resolves(pagoSample);
        const wrapper = shallowMount(PagoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
