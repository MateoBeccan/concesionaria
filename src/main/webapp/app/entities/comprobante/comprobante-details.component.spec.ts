import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import ComprobanteDetails from './comprobante-details.vue';
import ComprobanteService from './comprobante.service';

type ComprobanteDetailsComponentType = InstanceType<typeof ComprobanteDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const comprobanteSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Comprobante Management Detail Component', () => {
    let comprobanteServiceStub: SinonStubbedInstance<ComprobanteService>;
    let mountOptions: MountingOptions<ComprobanteDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      comprobanteServiceStub = sinon.createStubInstance<ComprobanteService>(ComprobanteService);

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
          comprobanteService: () => comprobanteServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        comprobanteServiceStub.find.resolves(comprobanteSample);
        route = {
          params: {
            comprobanteId: `${123}`,
          },
        };
        const wrapper = shallowMount(ComprobanteDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.comprobante).toMatchObject(comprobanteSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        comprobanteServiceStub.find.resolves(comprobanteSample);
        const wrapper = shallowMount(ComprobanteDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
