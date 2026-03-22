import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import MarcaService from '@/entities/marca/marca.service';
import VersionService from '@/entities/version/version.service';
import AlertService from '@/shared/alert/alert.service';

import ModeloUpdate from './modelo-update.vue';
import ModeloService from './modelo.service';

type ModeloUpdateComponentType = InstanceType<typeof ModeloUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const modeloSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<ModeloUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Modelo Management Update Component', () => {
    let comp: ModeloUpdateComponentType;
    let modeloServiceStub: SinonStubbedInstance<ModeloService>;

    beforeEach(() => {
      route = {};
      modeloServiceStub = sinon.createStubInstance<ModeloService>(ModeloService);
      modeloServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          modeloService: () => modeloServiceStub,
          marcaService: () =>
            sinon.createStubInstance<MarcaService>(MarcaService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          versionService: () =>
            sinon.createStubInstance<VersionService>(VersionService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(ModeloUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.modelo = modeloSample;
        modeloServiceStub.update.resolves(modeloSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(modeloServiceStub.update.calledWith(modeloSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        modeloServiceStub.create.resolves(entity);
        const wrapper = shallowMount(ModeloUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.modelo = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(modeloServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        modeloServiceStub.find.resolves(modeloSample);
        modeloServiceStub.retrieve.resolves([modeloSample]);

        // WHEN
        route = {
          params: {
            modeloId: `${modeloSample.id}`,
          },
        };
        const wrapper = shallowMount(ModeloUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.modelo).toMatchObject(modeloSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        modeloServiceStub.find.resolves(modeloSample);
        const wrapper = shallowMount(ModeloUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
