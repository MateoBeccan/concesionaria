import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import ModeloDetails from './modelo-details.vue';
import ModeloService from './modelo.service';

type ModeloDetailsComponentType = InstanceType<typeof ModeloDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const modeloSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Modelo Management Detail Component', () => {
    let modeloServiceStub: SinonStubbedInstance<ModeloService>;
    let mountOptions: MountingOptions<ModeloDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      modeloServiceStub = sinon.createStubInstance<ModeloService>(ModeloService);

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
          modeloService: () => modeloServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        modeloServiceStub.find.resolves(modeloSample);
        route = {
          params: {
            modeloId: `${123}`,
          },
        };
        const wrapper = shallowMount(ModeloDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.modelo).toMatchObject(modeloSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        modeloServiceStub.find.resolves(modeloSample);
        const wrapper = shallowMount(ModeloDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
