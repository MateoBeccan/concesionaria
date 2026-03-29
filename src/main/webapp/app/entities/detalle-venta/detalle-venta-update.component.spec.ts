import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import VehiculoService from '@/entities/vehiculo/vehiculo.service';
import VentaService from '@/entities/venta/venta.service';
import AlertService from '@/shared/alert/alert.service';

import DetalleVentaUpdate from './detalle-venta-update.vue';
import DetalleVentaService from './detalle-venta.service';

type DetalleVentaUpdateComponentType = InstanceType<typeof DetalleVentaUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const detalleVentaSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<DetalleVentaUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('DetalleVenta Management Update Component', () => {
    let comp: DetalleVentaUpdateComponentType;
    let detalleVentaServiceStub: SinonStubbedInstance<DetalleVentaService>;

    beforeEach(() => {
      route = {};
      detalleVentaServiceStub = sinon.createStubInstance<DetalleVentaService>(DetalleVentaService);
      detalleVentaServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          detalleVentaService: () => detalleVentaServiceStub,
          ventaService: () =>
            sinon.createStubInstance<VentaService>(VentaService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          vehiculoService: () =>
            sinon.createStubInstance<VehiculoService>(VehiculoService, {
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
        const wrapper = shallowMount(DetalleVentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.detalleVenta = detalleVentaSample;
        detalleVentaServiceStub.update.resolves(detalleVentaSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(detalleVentaServiceStub.update.calledWith(detalleVentaSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        detalleVentaServiceStub.create.resolves(entity);
        const wrapper = shallowMount(DetalleVentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.detalleVenta = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(detalleVentaServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        detalleVentaServiceStub.find.resolves(detalleVentaSample);
        detalleVentaServiceStub.retrieve.resolves([detalleVentaSample]);

        // WHEN
        route = {
          params: {
            detalleVentaId: `${detalleVentaSample.id}`,
          },
        };
        const wrapper = shallowMount(DetalleVentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.detalleVenta).toMatchObject(detalleVentaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        detalleVentaServiceStub.find.resolves(detalleVentaSample);
        const wrapper = shallowMount(DetalleVentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
