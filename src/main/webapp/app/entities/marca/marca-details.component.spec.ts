import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import MarcaDetails from './marca-details.vue';
import MarcaService from './marca.service';

type MarcaDetailsComponentType = InstanceType<typeof MarcaDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const marcaSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Marca Management Detail Component', () => {
    let marcaServiceStub: SinonStubbedInstance<MarcaService>;
    let mountOptions: MountingOptions<MarcaDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      marcaServiceStub = sinon.createStubInstance<MarcaService>(MarcaService);

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
          marcaService: () => marcaServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        marcaServiceStub.find.resolves(marcaSample);
        route = {
          params: {
            marcaId: `${123}`,
          },
        };
        const wrapper = shallowMount(MarcaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.marca).toMatchObject(marcaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        marcaServiceStub.find.resolves(marcaSample);
        const wrapper = shallowMount(MarcaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
