import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import CarroceriaDetails from './carroceria-details.vue';
import CarroceriaService from './carroceria.service';

type CarroceriaDetailsComponentType = InstanceType<typeof CarroceriaDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const carroceriaSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Carroceria Management Detail Component', () => {
    let carroceriaServiceStub: SinonStubbedInstance<CarroceriaService>;
    let mountOptions: MountingOptions<CarroceriaDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      carroceriaServiceStub = sinon.createStubInstance<CarroceriaService>(CarroceriaService);

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
          carroceriaService: () => carroceriaServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        carroceriaServiceStub.find.resolves(carroceriaSample);
        route = {
          params: {
            carroceriaId: `${123}`,
          },
        };
        const wrapper = shallowMount(CarroceriaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.carroceria).toMatchObject(carroceriaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        carroceriaServiceStub.find.resolves(carroceriaSample);
        const wrapper = shallowMount(CarroceriaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
