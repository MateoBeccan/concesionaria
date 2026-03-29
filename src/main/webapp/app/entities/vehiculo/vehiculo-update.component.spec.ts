import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import dayjs from 'dayjs';
import sinon, { type SinonStubbedInstance } from 'sinon';

import MotorService from '@/entities/motor/motor.service';
import TipoVehiculoService from '@/entities/tipo-vehiculo/tipo-vehiculo.service';
import VersionService from '@/entities/version/version.service';
import AlertService from '@/shared/alert/alert.service';
import { DATE_TIME_LONG_FORMAT } from '@/shared/composables/date-format';

import VehiculoUpdate from './vehiculo-update.vue';
import VehiculoService from './vehiculo.service';

type VehiculoUpdateComponentType = InstanceType<typeof VehiculoUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const vehiculoSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<VehiculoUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Vehiculo Management Update Component', () => {
    let comp: VehiculoUpdateComponentType;
    let vehiculoServiceStub: SinonStubbedInstance<VehiculoService>;

    beforeEach(() => {
      route = {};
      vehiculoServiceStub = sinon.createStubInstance<VehiculoService>(VehiculoService);
      vehiculoServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          vehiculoService: () => vehiculoServiceStub,
          versionService: () =>
            sinon.createStubInstance<VersionService>(VersionService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          motorService: () =>
            sinon.createStubInstance<MotorService>(MotorService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          tipoVehiculoService: () =>
            sinon.createStubInstance<TipoVehiculoService>(TipoVehiculoService, {
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
        const wrapper = shallowMount(VehiculoUpdate, { global: mountOptions });
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
        const wrapper = shallowMount(VehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.vehiculo = vehiculoSample;
        vehiculoServiceStub.update.resolves(vehiculoSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(vehiculoServiceStub.update.calledWith(vehiculoSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        vehiculoServiceStub.create.resolves(entity);
        const wrapper = shallowMount(VehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.vehiculo = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(vehiculoServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        vehiculoServiceStub.find.resolves(vehiculoSample);
        vehiculoServiceStub.retrieve.resolves([vehiculoSample]);

        // WHEN
        route = {
          params: {
            vehiculoId: `${vehiculoSample.id}`,
          },
        };
        const wrapper = shallowMount(VehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.vehiculo).toMatchObject(vehiculoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        vehiculoServiceStub.find.resolves(vehiculoSample);
        const wrapper = shallowMount(VehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
