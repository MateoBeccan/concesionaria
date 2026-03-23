import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import EstadoVentaUpdate from './estado-venta-update.vue';
import EstadoVentaService from './estado-venta.service';

type EstadoVentaUpdateComponentType = InstanceType<typeof EstadoVentaUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const estadoVentaSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<EstadoVentaUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('EstadoVenta Management Update Component', () => {
    let comp: EstadoVentaUpdateComponentType;
    let estadoVentaServiceStub: SinonStubbedInstance<EstadoVentaService>;

    beforeEach(() => {
      route = {};
      estadoVentaServiceStub = sinon.createStubInstance<EstadoVentaService>(EstadoVentaService);
      estadoVentaServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          estadoVentaService: () => estadoVentaServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(EstadoVentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.estadoVenta = estadoVentaSample;
        estadoVentaServiceStub.update.resolves(estadoVentaSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(estadoVentaServiceStub.update.calledWith(estadoVentaSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        estadoVentaServiceStub.create.resolves(entity);
        const wrapper = shallowMount(EstadoVentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.estadoVenta = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(estadoVentaServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        estadoVentaServiceStub.find.resolves(estadoVentaSample);
        estadoVentaServiceStub.retrieve.resolves([estadoVentaSample]);

        // WHEN
        route = {
          params: {
            estadoVentaId: `${estadoVentaSample.id}`,
          },
        };
        const wrapper = shallowMount(EstadoVentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.estadoVenta).toMatchObject(estadoVentaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        estadoVentaServiceStub.find.resolves(estadoVentaSample);
        const wrapper = shallowMount(EstadoVentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
