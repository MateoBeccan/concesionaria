import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import VehiculoDetails from './vehiculo-details.vue';
import VehiculoService from './vehiculo.service';

type VehiculoDetailsComponentType = InstanceType<typeof VehiculoDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const vehiculoSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Vehiculo Management Detail Component', () => {
    let vehiculoServiceStub: SinonStubbedInstance<VehiculoService>;
    let mountOptions: MountingOptions<VehiculoDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      vehiculoServiceStub = sinon.createStubInstance<VehiculoService>(VehiculoService);

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
          vehiculoService: () => vehiculoServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        vehiculoServiceStub.find.resolves(vehiculoSample);
        route = {
          params: {
            vehiculoId: `${123}`,
          },
        };
        const wrapper = shallowMount(VehiculoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.vehiculo).toMatchObject(vehiculoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        vehiculoServiceStub.find.resolves(vehiculoSample);
        const wrapper = shallowMount(VehiculoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
