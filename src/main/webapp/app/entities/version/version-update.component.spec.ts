import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import VersionUpdate from './version-update.vue';
import VersionService from './version.service';

type VersionUpdateComponentType = InstanceType<typeof VersionUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const versionSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<VersionUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Version Management Update Component', () => {
    let comp: VersionUpdateComponentType;
    let versionServiceStub: SinonStubbedInstance<VersionService>;

    beforeEach(() => {
      route = {};
      versionServiceStub = sinon.createStubInstance<VersionService>(VersionService);
      versionServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          versionService: () => versionServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(VersionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.version = versionSample;
        versionServiceStub.update.resolves(versionSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(versionServiceStub.update.calledWith(versionSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        versionServiceStub.create.resolves(entity);
        const wrapper = shallowMount(VersionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.version = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(versionServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        versionServiceStub.find.resolves(versionSample);
        versionServiceStub.retrieve.resolves([versionSample]);

        // WHEN
        route = {
          params: {
            versionId: `${versionSample.id}`,
          },
        };
        const wrapper = shallowMount(VersionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.version).toMatchObject(versionSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        versionServiceStub.find.resolves(versionSample);
        const wrapper = shallowMount(VersionUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
