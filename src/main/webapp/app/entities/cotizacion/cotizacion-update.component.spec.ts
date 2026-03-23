import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import dayjs from 'dayjs';
import sinon, { type SinonStubbedInstance } from 'sinon';

import MonedaService from '@/entities/moneda/moneda.service';
import AlertService from '@/shared/alert/alert.service';
import { DATE_TIME_LONG_FORMAT } from '@/shared/composables/date-format';

import CotizacionUpdate from './cotizacion-update.vue';
import CotizacionService from './cotizacion.service';

type CotizacionUpdateComponentType = InstanceType<typeof CotizacionUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const cotizacionSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<CotizacionUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Cotizacion Management Update Component', () => {
    let comp: CotizacionUpdateComponentType;
    let cotizacionServiceStub: SinonStubbedInstance<CotizacionService>;

    beforeEach(() => {
      route = {};
      cotizacionServiceStub = sinon.createStubInstance<CotizacionService>(CotizacionService);
      cotizacionServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          cotizacionService: () => cotizacionServiceStub,
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
        const wrapper = shallowMount(CotizacionUpdate, { global: mountOptions });
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
        const wrapper = shallowMount(CotizacionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.cotizacion = cotizacionSample;
        cotizacionServiceStub.update.resolves(cotizacionSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(cotizacionServiceStub.update.calledWith(cotizacionSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        cotizacionServiceStub.create.resolves(entity);
        const wrapper = shallowMount(CotizacionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.cotizacion = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(cotizacionServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        cotizacionServiceStub.find.resolves(cotizacionSample);
        cotizacionServiceStub.retrieve.resolves([cotizacionSample]);

        // WHEN
        route = {
          params: {
            cotizacionId: `${cotizacionSample.id}`,
          },
        };
        const wrapper = shallowMount(CotizacionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.cotizacion).toMatchObject(cotizacionSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        cotizacionServiceStub.find.resolves(cotizacionSample);
        const wrapper = shallowMount(CotizacionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
