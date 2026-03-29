import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TipoVehiculoDetails from './tipo-vehiculo-details.vue';
import TipoVehiculoService from './tipo-vehiculo.service';

type TipoVehiculoDetailsComponentType = InstanceType<typeof TipoVehiculoDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const tipoVehiculoSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('TipoVehiculo Management Detail Component', () => {
    let tipoVehiculoServiceStub: SinonStubbedInstance<TipoVehiculoService>;
    let mountOptions: MountingOptions<TipoVehiculoDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      tipoVehiculoServiceStub = sinon.createStubInstance<TipoVehiculoService>(TipoVehiculoService);

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
          tipoVehiculoService: () => tipoVehiculoServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        tipoVehiculoServiceStub.find.resolves(tipoVehiculoSample);
        route = {
          params: {
            tipoVehiculoId: `${123}`,
          },
        };
        const wrapper = shallowMount(TipoVehiculoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.tipoVehiculo).toMatchObject(tipoVehiculoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        tipoVehiculoServiceStub.find.resolves(tipoVehiculoSample);
        const wrapper = shallowMount(TipoVehiculoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
