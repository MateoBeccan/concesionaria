import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import MonedaUpdate from './moneda-update.vue';
import MonedaService from './moneda.service';

type MonedaUpdateComponentType = InstanceType<typeof MonedaUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const monedaSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<MonedaUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Moneda Management Update Component', () => {
    let comp: MonedaUpdateComponentType;
    let monedaServiceStub: SinonStubbedInstance<MonedaService>;

    beforeEach(() => {
      route = {};
      monedaServiceStub = sinon.createStubInstance<MonedaService>(MonedaService);
      monedaServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          monedaService: () => monedaServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(MonedaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.moneda = monedaSample;
        monedaServiceStub.update.resolves(monedaSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(monedaServiceStub.update.calledWith(monedaSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        monedaServiceStub.create.resolves(entity);
        const wrapper = shallowMount(MonedaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.moneda = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(monedaServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        monedaServiceStub.find.resolves(monedaSample);
        monedaServiceStub.retrieve.resolves([monedaSample]);

        // WHEN
        route = {
          params: {
            monedaId: `${monedaSample.id}`,
          },
        };
        const wrapper = shallowMount(MonedaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.moneda).toMatchObject(monedaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        monedaServiceStub.find.resolves(monedaSample);
        const wrapper = shallowMount(MonedaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
