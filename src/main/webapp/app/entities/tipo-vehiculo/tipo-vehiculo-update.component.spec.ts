import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TipoVehiculoUpdate from './tipo-vehiculo-update.vue';
import TipoVehiculoService from './tipo-vehiculo.service';

type TipoVehiculoUpdateComponentType = InstanceType<typeof TipoVehiculoUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const tipoVehiculoSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<TipoVehiculoUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('TipoVehiculo Management Update Component', () => {
    let comp: TipoVehiculoUpdateComponentType;
    let tipoVehiculoServiceStub: SinonStubbedInstance<TipoVehiculoService>;

    beforeEach(() => {
      route = {};
      tipoVehiculoServiceStub = sinon.createStubInstance<TipoVehiculoService>(TipoVehiculoService);
      tipoVehiculoServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          tipoVehiculoService: () => tipoVehiculoServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(TipoVehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.tipoVehiculo = tipoVehiculoSample;
        tipoVehiculoServiceStub.update.resolves(tipoVehiculoSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(tipoVehiculoServiceStub.update.calledWith(tipoVehiculoSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        tipoVehiculoServiceStub.create.resolves(entity);
        const wrapper = shallowMount(TipoVehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.tipoVehiculo = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(tipoVehiculoServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        tipoVehiculoServiceStub.find.resolves(tipoVehiculoSample);
        tipoVehiculoServiceStub.retrieve.resolves([tipoVehiculoSample]);

        // WHEN
        route = {
          params: {
            tipoVehiculoId: `${tipoVehiculoSample.id}`,
          },
        };
        const wrapper = shallowMount(TipoVehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.tipoVehiculo).toMatchObject(tipoVehiculoSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        tipoVehiculoServiceStub.find.resolves(tipoVehiculoSample);
        const wrapper = shallowMount(TipoVehiculoUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
