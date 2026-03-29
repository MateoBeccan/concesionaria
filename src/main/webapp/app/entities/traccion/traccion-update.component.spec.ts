import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TraccionUpdate from './traccion-update.vue';
import TraccionService from './traccion.service';

type TraccionUpdateComponentType = InstanceType<typeof TraccionUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const traccionSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<TraccionUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Traccion Management Update Component', () => {
    let comp: TraccionUpdateComponentType;
    let traccionServiceStub: SinonStubbedInstance<TraccionService>;

    beforeEach(() => {
      route = {};
      traccionServiceStub = sinon.createStubInstance<TraccionService>(TraccionService);
      traccionServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          traccionService: () => traccionServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(TraccionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.traccion = traccionSample;
        traccionServiceStub.update.resolves(traccionSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(traccionServiceStub.update.calledWith(traccionSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        traccionServiceStub.create.resolves(entity);
        const wrapper = shallowMount(TraccionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.traccion = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(traccionServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        traccionServiceStub.find.resolves(traccionSample);
        traccionServiceStub.retrieve.resolves([traccionSample]);

        // WHEN
        route = {
          params: {
            traccionId: `${traccionSample.id}`,
          },
        };
        const wrapper = shallowMount(TraccionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.traccion).toMatchObject(traccionSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        traccionServiceStub.find.resolves(traccionSample);
        const wrapper = shallowMount(TraccionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
