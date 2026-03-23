import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import ClienteDetails from './cliente-details.vue';
import ClienteService from './cliente.service';

type ClienteDetailsComponentType = InstanceType<typeof ClienteDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const clienteSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Cliente Management Detail Component', () => {
    let clienteServiceStub: SinonStubbedInstance<ClienteService>;
    let mountOptions: MountingOptions<ClienteDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      clienteServiceStub = sinon.createStubInstance<ClienteService>(ClienteService);

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
          clienteService: () => clienteServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        clienteServiceStub.find.resolves(clienteSample);
        route = {
          params: {
            clienteId: `${123}`,
          },
        };
        const wrapper = shallowMount(ClienteDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.cliente).toMatchObject(clienteSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        clienteServiceStub.find.resolves(clienteSample);
        const wrapper = shallowMount(ClienteDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
