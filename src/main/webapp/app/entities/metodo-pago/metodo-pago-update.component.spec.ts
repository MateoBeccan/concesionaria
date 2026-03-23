import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import MetodoPagoUpdate from './metodo-pago-update.vue';
import MetodoPagoService from './metodo-pago.service';

type MetodoPagoUpdateComponentType = InstanceType<typeof MetodoPagoUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const metodoPagoSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<MetodoPagoUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('MetodoPago Management Update Component', () => {
    let comp: MetodoPagoUpdateComponentType;
    let metodoPagoServiceStub: SinonStubbedInstance<MetodoPagoService>;

    beforeEach(() => {
      route = {};
      metodoPagoServiceStub = sinon.createStubInstance<MetodoPagoService>(MetodoPagoService);
      metodoPagoServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          metodoPagoService: () => metodoPagoServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(MetodoPagoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.metodoPago = metodoPagoSample;
        metodoPagoServiceStub.update.resolves(metodoPagoSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(metodoPagoServiceStub.update.calledWith(metodoPagoSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        metodoPagoServiceStub.create.resolves(entity);
        const wrapper = shallowMount(MetodoPagoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.metodoPago = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(metodoPagoServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        metodoPagoServiceStub.find.resolves(metodoPagoSample);
        metodoPagoServiceStub.retrieve.resolves([metodoPagoSample]);

        // WHEN
        route = {
          params: {
            metodoPagoId: `${metodoPagoSample.id}`,
          },
        };
        const wrapper = shallowMount(MetodoPagoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.metodoPago).toMatchObject(metodoPagoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        metodoPagoServiceStub.find.resolves(metodoPagoSample);
        const wrapper = shallowMount(MetodoPagoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
