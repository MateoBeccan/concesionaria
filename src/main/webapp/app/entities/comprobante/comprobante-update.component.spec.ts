import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import dayjs from 'dayjs';
import sinon, { type SinonStubbedInstance } from 'sinon';

import MonedaService from '@/entities/moneda/moneda.service';
import TipoComprobanteService from '@/entities/tipo-comprobante/tipo-comprobante.service';
import VentaService from '@/entities/venta/venta.service';
import AlertService from '@/shared/alert/alert.service';
import { DATE_TIME_LONG_FORMAT } from '@/shared/composables/date-format';

import ComprobanteUpdate from './comprobante-update.vue';
import ComprobanteService from './comprobante.service';

type ComprobanteUpdateComponentType = InstanceType<typeof ComprobanteUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const comprobanteSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<ComprobanteUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Comprobante Management Update Component', () => {
    let comp: ComprobanteUpdateComponentType;
    let comprobanteServiceStub: SinonStubbedInstance<ComprobanteService>;

    beforeEach(() => {
      route = {};
      comprobanteServiceStub = sinon.createStubInstance<ComprobanteService>(ComprobanteService);
      comprobanteServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          comprobanteService: () => comprobanteServiceStub,
          ventaService: () =>
            sinon.createStubInstance<VentaService>(VentaService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          tipoComprobanteService: () =>
            sinon.createStubInstance<TipoComprobanteService>(TipoComprobanteService, {
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
        const wrapper = shallowMount(ComprobanteUpdate, { global: mountOptions });
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
        const wrapper = shallowMount(ComprobanteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.comprobante = comprobanteSample;
        comprobanteServiceStub.update.resolves(comprobanteSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(comprobanteServiceStub.update.calledWith(comprobanteSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        comprobanteServiceStub.create.resolves(entity);
        const wrapper = shallowMount(ComprobanteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.comprobante = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(comprobanteServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        comprobanteServiceStub.find.resolves(comprobanteSample);
        comprobanteServiceStub.retrieve.resolves([comprobanteSample]);

        // WHEN
        route = {
          params: {
            comprobanteId: `${comprobanteSample.id}`,
          },
        };
        const wrapper = shallowMount(ComprobanteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.comprobante).toMatchObject(comprobanteSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        comprobanteServiceStub.find.resolves(comprobanteSample);
        const wrapper = shallowMount(ComprobanteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
