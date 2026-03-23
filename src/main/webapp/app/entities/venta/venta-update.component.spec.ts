import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import dayjs from 'dayjs';
import sinon, { type SinonStubbedInstance } from 'sinon';

import ClienteService from '@/entities/cliente/cliente.service';
import EstadoVentaService from '@/entities/estado-venta/estado-venta.service';
import MonedaService from '@/entities/moneda/moneda.service';
import UserService from '@/entities/user/user.service';
import AlertService from '@/shared/alert/alert.service';
import { DATE_TIME_LONG_FORMAT } from '@/shared/composables/date-format';

import VentaUpdate from './venta-update.vue';
import VentaService from './venta.service';

type VentaUpdateComponentType = InstanceType<typeof VentaUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const ventaSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<VentaUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Venta Management Update Component', () => {
    let comp: VentaUpdateComponentType;
    let ventaServiceStub: SinonStubbedInstance<VentaService>;

    beforeEach(() => {
      route = {};
      ventaServiceStub = sinon.createStubInstance<VentaService>(VentaService);
      ventaServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          ventaService: () => ventaServiceStub,
          clienteService: () =>
            sinon.createStubInstance<ClienteService>(ClienteService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          estadoVentaService: () =>
            sinon.createStubInstance<EstadoVentaService>(EstadoVentaService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          monedaService: () =>
            sinon.createStubInstance<MonedaService>(MonedaService, {
              retrieve: sinon.stub().resolves({}),
            } as any),

          userService: () =>
            sinon.createStubInstance<UserService>(UserService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('load', () => {
      beforeEach(() => {
        const wrapper = shallowMount(VentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
      });
      it('Should convert date from string', () => {
        // GIVEN
        const date = new Date('2019-10-15T11:42:02Z');

        // WHEN
        const convertedDate = comp.convertDateTimeFromServer(date);

        // THEN
        expect(convertedDate).toEqual(dayjs(date).format(DATE_TIME_LONG_FORMAT));
      });

      it('Should not convert date if date is not present', () => {
        expect(comp.convertDateTimeFromServer(null)).toBeNull();
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(VentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.venta = ventaSample;
        ventaServiceStub.update.resolves(ventaSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(ventaServiceStub.update.calledWith(ventaSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        ventaServiceStub.create.resolves(entity);
        const wrapper = shallowMount(VentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.venta = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(ventaServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        ventaServiceStub.find.resolves(ventaSample);
        ventaServiceStub.retrieve.resolves([ventaSample]);

        // WHEN
        route = {
          params: {
            ventaId: `${ventaSample.id}`,
          },
        };
        const wrapper = shallowMount(VentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.venta).toMatchObject(ventaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        ventaServiceStub.find.resolves(ventaSample);
        const wrapper = shallowMount(VentaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
