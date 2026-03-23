import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TipoComprobanteDetails from './tipo-comprobante-details.vue';
import TipoComprobanteService from './tipo-comprobante.service';

type TipoComprobanteDetailsComponentType = InstanceType<typeof TipoComprobanteDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const tipoComprobanteSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('TipoComprobante Management Detail Component', () => {
    let tipoComprobanteServiceStub: SinonStubbedInstance<TipoComprobanteService>;
    let mountOptions: MountingOptions<TipoComprobanteDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      tipoComprobanteServiceStub = sinon.createStubInstance<TipoComprobanteService>(TipoComprobanteService);

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
          tipoComprobanteService: () => tipoComprobanteServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        tipoComprobanteServiceStub.find.resolves(tipoComprobanteSample);
        route = {
          params: {
            tipoComprobanteId: `${123}`,
          },
        };
        const wrapper = shallowMount(TipoComprobanteDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.tipoComprobante).toMatchObject(tipoComprobanteSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        tipoComprobanteServiceStub.find.resolves(tipoComprobanteSample);
        const wrapper = shallowMount(TipoComprobanteDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
