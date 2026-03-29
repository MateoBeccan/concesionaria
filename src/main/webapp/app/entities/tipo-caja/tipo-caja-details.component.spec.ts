import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TipoCajaDetails from './tipo-caja-details.vue';
import TipoCajaService from './tipo-caja.service';

type TipoCajaDetailsComponentType = InstanceType<typeof TipoCajaDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const tipoCajaSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('TipoCaja Management Detail Component', () => {
    let tipoCajaServiceStub: SinonStubbedInstance<TipoCajaService>;
    let mountOptions: MountingOptions<TipoCajaDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      tipoCajaServiceStub = sinon.createStubInstance<TipoCajaService>(TipoCajaService);

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
          tipoCajaService: () => tipoCajaServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        tipoCajaServiceStub.find.resolves(tipoCajaSample);
        route = {
          params: {
            tipoCajaId: `${123}`,
          },
        };
        const wrapper = shallowMount(TipoCajaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.tipoCaja).toMatchObject(tipoCajaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        tipoCajaServiceStub.find.resolves(tipoCajaSample);
        const wrapper = shallowMount(TipoCajaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
