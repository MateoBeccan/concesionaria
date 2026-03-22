import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import MotorService from '@/entities/motor/motor.service';
import AlertService from '@/shared/alert/alert.service';

import CombustibleUpdate from './combustible-update.vue';
import CombustibleService from './combustible.service';

type CombustibleUpdateComponentType = InstanceType<typeof CombustibleUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const combustibleSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<CombustibleUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Combustible Management Update Component', () => {
    let comp: CombustibleUpdateComponentType;
    let combustibleServiceStub: SinonStubbedInstance<CombustibleService>;

    beforeEach(() => {
      route = {};
      combustibleServiceStub = sinon.createStubInstance<CombustibleService>(CombustibleService);
      combustibleServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          combustibleService: () => combustibleServiceStub,
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
        const wrapper = shallowMount(CombustibleUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.combustible = combustibleSample;
        combustibleServiceStub.update.resolves(combustibleSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(combustibleServiceStub.update.calledWith(combustibleSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        combustibleServiceStub.create.resolves(entity);
        const wrapper = shallowMount(CombustibleUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.combustible = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(combustibleServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        combustibleServiceStub.find.resolves(combustibleSample);
        combustibleServiceStub.retrieve.resolves([combustibleSample]);

        // WHEN
        route = {
          params: {
            combustibleId: `${combustibleSample.id}`,
          },
        };
        const wrapper = shallowMount(CombustibleUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.combustible).toMatchObject(combustibleSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        combustibleServiceStub.find.resolves(combustibleSample);
        const wrapper = shallowMount(CombustibleUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
