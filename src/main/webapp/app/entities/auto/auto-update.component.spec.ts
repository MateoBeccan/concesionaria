import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import MarcaService from '@/entities/marca/marca.service';
import ModeloService from '@/entities/modelo/modelo.service';
import MotorService from '@/entities/motor/motor.service';
import VersionService from '@/entities/version/version.service';
import AlertService from '@/shared/alert/alert.service';

import AutoUpdate from './auto-update.vue';
import AutoService from './auto.service';

type AutoUpdateComponentType = InstanceType<typeof AutoUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const autoSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<AutoUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Auto Management Update Component', () => {
    let comp: AutoUpdateComponentType;
    let autoServiceStub: SinonStubbedInstance<AutoService>;

    beforeEach(() => {
      route = {};
      autoServiceStub = sinon.createStubInstance<AutoService>(AutoService);
      autoServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          autoService: () => autoServiceStub,
          marcaService: () =>
            sinon.createStubInstance<MarcaService>(MarcaService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          modeloService: () =>
            sinon.createStubInstance<ModeloService>(ModeloService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          versionService: () =>
            sinon.createStubInstance<VersionService>(VersionService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          motorService: () =>
            sinon.createStubInstance<MotorService>(MotorService, {
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
        const wrapper = shallowMount(AutoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.auto = autoSample;
        autoServiceStub.update.resolves(autoSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(autoServiceStub.update.calledWith(autoSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        autoServiceStub.create.resolves(entity);
        const wrapper = shallowMount(AutoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.auto = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(autoServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        autoServiceStub.find.resolves(autoSample);
        autoServiceStub.retrieve.resolves([autoSample]);

        // WHEN
        route = {
          params: {
            autoId: `${autoSample.id}`,
          },
        };
        const wrapper = shallowMount(AutoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.auto).toMatchObject(autoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        autoServiceStub.find.resolves(autoSample);
        const wrapper = shallowMount(AutoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
