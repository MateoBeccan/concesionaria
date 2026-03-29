import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import CarroceriaUpdate from './carroceria-update.vue';
import CarroceriaService from './carroceria.service';

type CarroceriaUpdateComponentType = InstanceType<typeof CarroceriaUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const carroceriaSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<CarroceriaUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Carroceria Management Update Component', () => {
    let comp: CarroceriaUpdateComponentType;
    let carroceriaServiceStub: SinonStubbedInstance<CarroceriaService>;

    beforeEach(() => {
      route = {};
      carroceriaServiceStub = sinon.createStubInstance<CarroceriaService>(CarroceriaService);
      carroceriaServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          carroceriaService: () => carroceriaServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(CarroceriaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.carroceria = carroceriaSample;
        carroceriaServiceStub.update.resolves(carroceriaSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(carroceriaServiceStub.update.calledWith(carroceriaSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        carroceriaServiceStub.create.resolves(entity);
        const wrapper = shallowMount(CarroceriaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.carroceria = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(carroceriaServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        carroceriaServiceStub.find.resolves(carroceriaSample);
        carroceriaServiceStub.retrieve.resolves([carroceriaSample]);

        // WHEN
        route = {
          params: {
            carroceriaId: `${carroceriaSample.id}`,
          },
        };
        const wrapper = shallowMount(CarroceriaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.carroceria).toMatchObject(carroceriaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        carroceriaServiceStub.find.resolves(carroceriaSample);
        const wrapper = shallowMount(CarroceriaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
