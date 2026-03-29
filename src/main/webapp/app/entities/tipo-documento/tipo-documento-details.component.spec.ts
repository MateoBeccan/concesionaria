import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TipoDocumentoDetails from './tipo-documento-details.vue';
import TipoDocumentoService from './tipo-documento.service';

type TipoDocumentoDetailsComponentType = InstanceType<typeof TipoDocumentoDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const tipoDocumentoSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('TipoDocumento Management Detail Component', () => {
    let tipoDocumentoServiceStub: SinonStubbedInstance<TipoDocumentoService>;
    let mountOptions: MountingOptions<TipoDocumentoDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      tipoDocumentoServiceStub = sinon.createStubInstance<TipoDocumentoService>(TipoDocumentoService);

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
          tipoDocumentoService: () => tipoDocumentoServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        tipoDocumentoServiceStub.find.resolves(tipoDocumentoSample);
        route = {
          params: {
            tipoDocumentoId: `${123}`,
          },
        };
        const wrapper = shallowMount(TipoDocumentoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.tipoDocumento).toMatchObject(tipoDocumentoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        tipoDocumentoServiceStub.find.resolves(tipoDocumentoSample);
        const wrapper = shallowMount(TipoDocumentoDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
