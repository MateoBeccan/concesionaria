import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TipoDocumentoUpdate from './tipo-documento-update.vue';
import TipoDocumentoService from './tipo-documento.service';

type TipoDocumentoUpdateComponentType = InstanceType<typeof TipoDocumentoUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const tipoDocumentoSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<TipoDocumentoUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('TipoDocumento Management Update Component', () => {
    let comp: TipoDocumentoUpdateComponentType;
    let tipoDocumentoServiceStub: SinonStubbedInstance<TipoDocumentoService>;

    beforeEach(() => {
      route = {};
      tipoDocumentoServiceStub = sinon.createStubInstance<TipoDocumentoService>(TipoDocumentoService);
      tipoDocumentoServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

      alertService = new AlertService({
        toast: {
          show: vitest.fn(),
        } as any,
      });

      mountOptions = {
        stubs: {
          'font-awesome-icon': true,
          'b-input-group': true,
          'b-input-group-prepend': true,
          'b-form-datepicker': true,
          'b-form-input': true,
        },
        provide: {
          alertService,
          tipoDocumentoService: () => tipoDocumentoServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(TipoDocumentoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.tipoDocumento = tipoDocumentoSample;
        tipoDocumentoServiceStub.update.resolves(tipoDocumentoSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(tipoDocumentoServiceStub.update.calledWith(tipoDocumentoSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        tipoDocumentoServiceStub.create.resolves(entity);
        const wrapper = shallowMount(TipoDocumentoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.tipoDocumento = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(tipoDocumentoServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        tipoDocumentoServiceStub.find.resolves(tipoDocumentoSample);
        tipoDocumentoServiceStub.retrieve.resolves([tipoDocumentoSample]);

        // WHEN
        route = {
          params: {
            tipoDocumentoId: `${tipoDocumentoSample.id}`,
          },
        };
        const wrapper = shallowMount(TipoDocumentoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.tipoDocumento).toMatchObject(tipoDocumentoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        tipoDocumentoServiceStub.find.resolves(tipoDocumentoSample);
        const wrapper = shallowMount(TipoDocumentoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
