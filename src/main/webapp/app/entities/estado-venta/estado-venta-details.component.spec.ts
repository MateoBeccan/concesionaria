import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import EstadoVentaDetails from './estado-venta-details.vue';
import EstadoVentaService from './estado-venta.service';

type EstadoVentaDetailsComponentType = InstanceType<typeof EstadoVentaDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const estadoVentaSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('EstadoVenta Management Detail Component', () => {
    let estadoVentaServiceStub: SinonStubbedInstance<EstadoVentaService>;
    let mountOptions: MountingOptions<EstadoVentaDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      estadoVentaServiceStub = sinon.createStubInstance<EstadoVentaService>(EstadoVentaService);

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
          estadoVentaService: () => estadoVentaServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        estadoVentaServiceStub.find.resolves(estadoVentaSample);
        route = {
          params: {
            estadoVentaId: `${123}`,
          },
        };
        const wrapper = shallowMount(EstadoVentaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.estadoVenta).toMatchObject(estadoVentaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        estadoVentaServiceStub.find.resolves(estadoVentaSample);
        const wrapper = shallowMount(EstadoVentaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
