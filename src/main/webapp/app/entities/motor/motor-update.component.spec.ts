import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import CombustibleService from '@/entities/combustible/combustible.service';
import TipoCajaService from '@/entities/tipo-caja/tipo-caja.service';
import TraccionService from '@/entities/traccion/traccion.service';
import AlertService from '@/shared/alert/alert.service';

import MotorUpdate from './motor-update.vue';
import MotorService from './motor.service';

type MotorUpdateComponentType = InstanceType<typeof MotorUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const motorSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<MotorUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Motor Management Update Component', () => {
    let comp: MotorUpdateComponentType;
    let motorServiceStub: SinonStubbedInstance<MotorService>;

    beforeEach(() => {
      route = {};
      motorServiceStub = sinon.createStubInstance<MotorService>(MotorService);
      motorServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          motorService: () => motorServiceStub,
          combustibleService: () =>
            sinon.createStubInstance<CombustibleService>(CombustibleService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          tipoCajaService: () =>
            sinon.createStubInstance<TipoCajaService>(TipoCajaService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          traccionService: () =>
            sinon.createStubInstance<TraccionService>(TraccionService, {
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
        const wrapper = shallowMount(MotorUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.motor = motorSample;
        motorServiceStub.update.resolves(motorSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(motorServiceStub.update.calledWith(motorSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        motorServiceStub.create.resolves(entity);
        const wrapper = shallowMount(MotorUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.motor = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(motorServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        motorServiceStub.find.resolves(motorSample);
        motorServiceStub.retrieve.resolves([motorSample]);

        // WHEN
        route = {
          params: {
            motorId: `${motorSample.id}`,
          },
        };
        const wrapper = shallowMount(MotorUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.motor).toMatchObject(motorSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        motorServiceStub.find.resolves(motorSample);
        const wrapper = shallowMount(MotorUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
