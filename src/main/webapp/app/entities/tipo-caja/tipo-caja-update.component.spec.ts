import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TipoCajaUpdate from './tipo-caja-update.vue';
import TipoCajaService from './tipo-caja.service';

type TipoCajaUpdateComponentType = InstanceType<typeof TipoCajaUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const tipoCajaSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<TipoCajaUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('TipoCaja Management Update Component', () => {
    let comp: TipoCajaUpdateComponentType;
    let tipoCajaServiceStub: SinonStubbedInstance<TipoCajaService>;

    beforeEach(() => {
      route = {};
      tipoCajaServiceStub = sinon.createStubInstance<TipoCajaService>(TipoCajaService);
      tipoCajaServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          tipoCajaService: () => tipoCajaServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(TipoCajaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.tipoCaja = tipoCajaSample;
        tipoCajaServiceStub.update.resolves(tipoCajaSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(tipoCajaServiceStub.update.calledWith(tipoCajaSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        tipoCajaServiceStub.create.resolves(entity);
        const wrapper = shallowMount(TipoCajaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.tipoCaja = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(tipoCajaServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        tipoCajaServiceStub.find.resolves(tipoCajaSample);
        tipoCajaServiceStub.retrieve.resolves([tipoCajaSample]);

        // WHEN
        route = {
          params: {
            tipoCajaId: `${tipoCajaSample.id}`,
          },
        };
        const wrapper = shallowMount(TipoCajaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.tipoCaja).toMatchObject(tipoCajaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        tipoCajaServiceStub.find.resolves(tipoCajaSample);
        const wrapper = shallowMount(TipoCajaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
