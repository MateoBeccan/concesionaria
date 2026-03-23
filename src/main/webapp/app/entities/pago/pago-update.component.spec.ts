import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import dayjs from 'dayjs';
import sinon, { type SinonStubbedInstance } from 'sinon';

import MetodoPagoService from '@/entities/metodo-pago/metodo-pago.service';
import MonedaService from '@/entities/moneda/moneda.service';
import VentaService from '@/entities/venta/venta.service';
import AlertService from '@/shared/alert/alert.service';
import { DATE_TIME_LONG_FORMAT } from '@/shared/composables/date-format';

import PagoUpdate from './pago-update.vue';
import PagoService from './pago.service';

type PagoUpdateComponentType = InstanceType<typeof PagoUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const pagoSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<PagoUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Pago Management Update Component', () => {
    let comp: PagoUpdateComponentType;
    let pagoServiceStub: SinonStubbedInstance<PagoService>;

    beforeEach(() => {
      route = {};
      pagoServiceStub = sinon.createStubInstance<PagoService>(PagoService);
      pagoServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          pagoService: () => pagoServiceStub,
          ventaService: () =>
            sinon.createStubInstance<VentaService>(VentaService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          metodoPagoService: () =>
            sinon.createStubInstance<MetodoPagoService>(MetodoPagoService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          monedaService: () =>
            sinon.createStubInstance<MonedaService>(MonedaService, {
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
        const wrapper = shallowMount(PagoUpdate, { global: mountOptions });
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
        const wrapper = shallowMount(PagoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.pago = pagoSample;
        pagoServiceStub.update.resolves(pagoSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(pagoServiceStub.update.calledWith(pagoSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        pagoServiceStub.create.resolves(entity);
        const wrapper = shallowMount(PagoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.pago = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(pagoServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        pagoServiceStub.find.resolves(pagoSample);
        pagoServiceStub.retrieve.resolves([pagoSample]);

        // WHEN
        route = {
          params: {
            pagoId: `${pagoSample.id}`,
          },
        };
        const wrapper = shallowMount(PagoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.pago).toMatchObject(pagoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        pagoServiceStub.find.resolves(pagoSample);
        const wrapper = shallowMount(PagoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
