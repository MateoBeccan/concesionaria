import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import CondicionIvaDetails from './condicion-iva-details.vue';
import CondicionIvaService from './condicion-iva.service';

type CondicionIvaDetailsComponentType = InstanceType<typeof CondicionIvaDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const condicionIvaSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('CondicionIva Management Detail Component', () => {
    let condicionIvaServiceStub: SinonStubbedInstance<CondicionIvaService>;
    let mountOptions: MountingOptions<CondicionIvaDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      condicionIvaServiceStub = sinon.createStubInstance<CondicionIvaService>(CondicionIvaService);

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
          condicionIvaService: () => condicionIvaServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        condicionIvaServiceStub.find.resolves(condicionIvaSample);
        route = {
          params: {
            condicionIvaId: `${123}`,
          },
        };
        const wrapper = shallowMount(CondicionIvaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.condicionIva).toMatchObject(condicionIvaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        condicionIvaServiceStub.find.resolves(condicionIvaSample);
        const wrapper = shallowMount(CondicionIvaDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
