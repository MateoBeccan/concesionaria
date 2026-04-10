import { beforeEach, afterEach, describe, expect, it, vitest } from 'vitest';
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

import { EstadoVehiculo } from '@/shared/model/enumerations/estado-vehiculo.model';
import { CondicionVehiculo } from '@/shared/model/enumerations/condicion-vehiculo.model';

const vehiculoSample = {
  id: 123,
  estado: EstadoVehiculo.NUEVO,
  condicion: CondicionVehiculo.EN_VENTA,
  km: 0,
  patente: 'AAA123',
  precio: 0,
};

describe('Component Tests', () => {
  let mountOptions: MountingOptions<VehiculoUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Vehiculo Management Update Component', () => {
    let comp: VehiculoUpdateComponentType;
    let vehiculoServiceStub: SinonStubbedInstance<VehiculoService>;

    beforeEach(() => {
      route = {};
      vehiculoServiceStub = sinon.createStubInstance<VehiculoService>(VehiculoService);

      // FIX retrieve (debe devolver { data, headers })
      vehiculoServiceStub.retrieve.resolves({ data: [], headers: {} });

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
              retrieve: sinon.stub().resolves({ data: [], headers: {} }),
            } as any),
          motorService: () =>
            sinon.createStubInstance<MotorService>(MotorService, {
              retrieve: sinon.stub().resolves({ data: [], headers: {} }),
            } as any),
          tipoVehiculoService: () =>
            sinon.createStubInstance<TipoVehiculoService>(TipoVehiculoService, {
              retrieve: sinon.stub().resolves({ data: [], headers: {} }),
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
        const date = new Date('2019-10-15T11:42:02Z');

        const convertedDate = comp.convertDateTimeFromServer(date);

        expect(convertedDate).toEqual(dayjs(date).format(DATE_TIME_LONG_FORMAT));
      });

      it('Should not convert date if date is not present', () => {
        expect(comp.convertDateTimeFromServer(null as any)).toBeNull();
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        const wrapper = shallowMount(VehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;

        comp.vehiculo = vehiculoSample as any;
        vehiculoServiceStub.update.resolves(vehiculoSample as any);

        comp.save();
        await comp.$nextTick();

        expect(vehiculoServiceStub.update.called).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        const entity = {};
        vehiculoServiceStub.create.resolves(entity as any);

        const wrapper = shallowMount(VehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.vehiculo = entity as any;

        comp.save();
        await comp.$nextTick();

        expect(vehiculoServiceStub.create.called).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        vehiculoServiceStub.find.resolves(vehiculoSample as any);

        // 🔥 FIX retrieve correcto
        vehiculoServiceStub.retrieve.resolves({
          data: [vehiculoSample],
          headers: {},
        });

        route = {
          params: {
            vehiculoId: `${vehiculoSample.id}`,
          },
        };

        const wrapper = shallowMount(VehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;

        await comp.$nextTick();

        expect(comp.vehiculo).toMatchObject(vehiculoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        vehiculoServiceStub.find.resolves(vehiculoSample as any);

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
