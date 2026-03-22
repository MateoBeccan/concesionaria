import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import VersionDetails from './version-details.vue';
import VersionService from './version.service';

type VersionDetailsComponentType = InstanceType<typeof VersionDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const versionSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Version Management Detail Component', () => {
    let versionServiceStub: SinonStubbedInstance<VersionService>;
    let mountOptions: MountingOptions<VersionDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      versionServiceStub = sinon.createStubInstance<VersionService>(VersionService);

      alertService = new AlertService({
        toast: {
          show: vitest.fn(),
        } as any,
      });

      mountOptions = {
        stubs: {
          'font-awesome-icon': true,
          'router-link': true,
        },
        provide: {
          alertService,
          versionService: () => versionServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        versionServiceStub.find.resolves(versionSample);
        route = {
          params: {
            versionId: `${123}`,
          },
        };
        const wrapper = shallowMount(VersionDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.version).toMatchObject(versionSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        versionServiceStub.find.resolves(versionSample);
        const wrapper = shallowMount(VersionDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
