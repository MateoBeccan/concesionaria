import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import dayjs from 'dayjs';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';
import { DATE_TIME_LONG_FORMAT } from '@/shared/composables/date-format';

import MarcaUpdate from './marca-update.vue';
import MarcaService from './marca.service';

type MarcaUpdateComponentType = InstanceType<typeof MarcaUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const marcaSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<MarcaUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Marca Management Update Component', () => {
    let comp: MarcaUpdateComponentType;
    let marcaServiceStub: SinonStubbedInstance<MarcaService>;

    beforeEach(() => {
      route = {};
      marcaServiceStub = sinon.createStubInstance<MarcaService>(MarcaService);
      marcaServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          marcaService: () => marcaServiceStub,
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('load', () => {
      beforeEach(() => {
        const wrapper = shallowMount(MarcaUpdate, { global: mountOptions });
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
        const wrapper = shallowMount(MarcaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.marca = marcaSample;
        marcaServiceStub.update.resolves(marcaSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(marcaServiceStub.update.calledWith(marcaSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        marcaServiceStub.create.resolves(entity);
        const wrapper = shallowMount(MarcaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.marca = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(marcaServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        marcaServiceStub.find.resolves(marcaSample);
        marcaServiceStub.retrieve.resolves([marcaSample]);

        // WHEN
        route = {
          params: {
            marcaId: `${marcaSample.id}`,
          },
        };
        const wrapper = shallowMount(MarcaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.marca).toMatchObject(marcaSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        marcaServiceStub.find.resolves(marcaSample);
        const wrapper = shallowMount(MarcaUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
