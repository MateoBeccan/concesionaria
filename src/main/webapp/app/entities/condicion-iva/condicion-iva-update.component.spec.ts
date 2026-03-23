import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import CondicionIvaUpdate from './condicion-iva-update.vue';
import CondicionIvaService from './condicion-iva.service';

type CondicionIvaUpdateComponentType = InstanceType<typeof CondicionIvaUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const condicionIvaSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<CondicionIvaUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('CondicionIva Management Update Component', () => {
    let comp: CondicionIvaUpdateComponentType;
    let condicionIvaServiceStub: SinonStubbedInstance<CondicionIvaService>;

    beforeEach(() => {
      route = {};
      condicionIvaServiceStub = sinon.createStubInstance<CondicionIvaService>(CondicionIvaService);
      condicionIvaServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          condicionIvaService: () => condicionIvaServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(CondicionIvaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.condicionIva = condicionIvaSample;
        condicionIvaServiceStub.update.resolves(condicionIvaSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(condicionIvaServiceStub.update.calledWith(condicionIvaSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        condicionIvaServiceStub.create.resolves(entity);
        const wrapper = shallowMount(CondicionIvaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.condicionIva = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(condicionIvaServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        condicionIvaServiceStub.find.resolves(condicionIvaSample);
        condicionIvaServiceStub.retrieve.resolves([condicionIvaSample]);

        // WHEN
        route = {
          params: {
            condicionIvaId: `${condicionIvaSample.id}`,
          },
        };
        const wrapper = shallowMount(CondicionIvaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.condicionIva).toMatchObject(condicionIvaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        condicionIvaServiceStub.find.resolves(condicionIvaSample);
        const wrapper = shallowMount(CondicionIvaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
