import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import InventarioDetails from './inventario-details.vue';
import InventarioService from './inventario.service';

type InventarioDetailsComponentType = InstanceType<typeof InventarioDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const inventarioSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Inventario Management Detail Component', () => {
    let inventarioServiceStub: SinonStubbedInstance<InventarioService>;
    let mountOptions: MountingOptions<InventarioDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      inventarioServiceStub = sinon.createStubInstance<InventarioService>(InventarioService);

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
          inventarioService: () => inventarioServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        inventarioServiceStub.find.resolves(inventarioSample);
        route = {
          params: {
            inventarioId: `${123}`,
          },
        };
        const wrapper = shallowMount(InventarioDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.inventario).toMatchObject(inventarioSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        inventarioServiceStub.find.resolves(inventarioSample);
        const wrapper = shallowMount(InventarioDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
