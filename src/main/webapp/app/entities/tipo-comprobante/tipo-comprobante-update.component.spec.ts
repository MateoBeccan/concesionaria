import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TipoComprobanteUpdate from './tipo-comprobante-update.vue';
import TipoComprobanteService from './tipo-comprobante.service';

type TipoComprobanteUpdateComponentType = InstanceType<typeof TipoComprobanteUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const tipoComprobanteSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<TipoComprobanteUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('TipoComprobante Management Update Component', () => {
    let comp: TipoComprobanteUpdateComponentType;
    let tipoComprobanteServiceStub: SinonStubbedInstance<TipoComprobanteService>;

    beforeEach(() => {
      route = {};
      tipoComprobanteServiceStub = sinon.createStubInstance<TipoComprobanteService>(TipoComprobanteService);
      tipoComprobanteServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          tipoComprobanteService: () => tipoComprobanteServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(TipoComprobanteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.tipoComprobante = tipoComprobanteSample;
        tipoComprobanteServiceStub.update.resolves(tipoComprobanteSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(tipoComprobanteServiceStub.update.calledWith(tipoComprobanteSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        tipoComprobanteServiceStub.create.resolves(entity);
        const wrapper = shallowMount(TipoComprobanteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.tipoComprobante = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(tipoComprobanteServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        tipoComprobanteServiceStub.find.resolves(tipoComprobanteSample);
        tipoComprobanteServiceStub.retrieve.resolves([tipoComprobanteSample]);

        // WHEN
        route = {
          params: {
            tipoComprobanteId: `${tipoComprobanteSample.id}`,
          },
        };
        const wrapper = shallowMount(TipoComprobanteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.tipoComprobante).toMatchObject(tipoComprobanteSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        tipoComprobanteServiceStub.find.resolves(tipoComprobanteSample);
        const wrapper = shallowMount(TipoComprobanteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
