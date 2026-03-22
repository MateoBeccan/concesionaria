import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import Prueba1Update from './prueba-1-update.vue';
import Prueba1Service from './prueba-1.service';

type Prueba1UpdateComponentType = InstanceType<typeof Prueba1Update>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const prueba1Sample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<Prueba1UpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Prueba1 Management Update Component', () => {
    let comp: Prueba1UpdateComponentType;
    let prueba1ServiceStub: SinonStubbedInstance<Prueba1Service>;

    beforeEach(() => {
      route = {};
      prueba1ServiceStub = sinon.createStubInstance<Prueba1Service>(Prueba1Service);
      prueba1ServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          prueba1Service: () => prueba1ServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(Prueba1Update, { global: mountOptions });
        comp = wrapper.vm;
        comp.prueba1 = prueba1Sample;
        prueba1ServiceStub.update.resolves(prueba1Sample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(prueba1ServiceStub.update.calledWith(prueba1Sample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        prueba1ServiceStub.create.resolves(entity);
        const wrapper = shallowMount(Prueba1Update, { global: mountOptions });
        comp = wrapper.vm;
        comp.prueba1 = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(prueba1ServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        prueba1ServiceStub.find.resolves(prueba1Sample);
        prueba1ServiceStub.retrieve.resolves([prueba1Sample]);

        // WHEN
        route = {
          params: {
            prueba1Id: `${prueba1Sample.id}`,
          },
        };
        const wrapper = shallowMount(Prueba1Update, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.prueba1).toMatchObject(prueba1Sample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        prueba1ServiceStub.find.resolves(prueba1Sample);
        const wrapper = shallowMount(Prueba1Update, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
