import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import CotizacionDetails from './cotizacion-details.vue';
import CotizacionService from './cotizacion.service';

type CotizacionDetailsComponentType = InstanceType<typeof CotizacionDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const cotizacionSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Cotizacion Management Detail Component', () => {
    let cotizacionServiceStub: SinonStubbedInstance<CotizacionService>;
    let mountOptions: MountingOptions<CotizacionDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      cotizacionServiceStub = sinon.createStubInstance<CotizacionService>(CotizacionService);

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
          cotizacionService: () => cotizacionServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        cotizacionServiceStub.find.resolves(cotizacionSample);
        route = {
          params: {
            cotizacionId: `${123}`,
          },
        };
        const wrapper = shallowMount(CotizacionDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.cotizacion).toMatchObject(cotizacionSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        cotizacionServiceStub.find.resolves(cotizacionSample);
        const wrapper = shallowMount(CotizacionDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
