import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import Prueba1Details from './prueba-1-details.vue';
import Prueba1Service from './prueba-1.service';

type Prueba1DetailsComponentType = InstanceType<typeof Prueba1Details>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const prueba1Sample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Prueba1 Management Detail Component', () => {
    let prueba1ServiceStub: SinonStubbedInstance<Prueba1Service>;
    let mountOptions: MountingOptions<Prueba1DetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      prueba1ServiceStub = sinon.createStubInstance<Prueba1Service>(Prueba1Service);

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
          prueba1Service: () => prueba1ServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        prueba1ServiceStub.find.resolves(prueba1Sample);
        route = {
          params: {
            prueba1Id: `${123}`,
          },
        };
        const wrapper = shallowMount(Prueba1Details, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.prueba1).toMatchObject(prueba1Sample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        prueba1ServiceStub.find.resolves(prueba1Sample);
        const wrapper = shallowMount(Prueba1Details, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
