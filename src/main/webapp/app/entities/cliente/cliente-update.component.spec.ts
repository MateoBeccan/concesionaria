import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { type RouteLocation } from 'vue-router';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import dayjs from 'dayjs';
import sinon, { type SinonStubbedInstance } from 'sinon';

import CondicionIvaService from '@/entities/condicion-iva/condicion-iva.service';
import AlertService from '@/shared/alert/alert.service';
import { DATE_TIME_LONG_FORMAT } from '@/shared/composables/date-format';

import ClienteUpdate from './cliente-update.vue';
import ClienteService from './cliente.service';

type ClienteUpdateComponentType = InstanceType<typeof ClienteUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const clienteSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<ClienteUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Cliente Management Update Component', () => {
    let comp: ClienteUpdateComponentType;
    let clienteServiceStub: SinonStubbedInstance<ClienteService>;

    beforeEach(() => {
      route = {};
      clienteServiceStub = sinon.createStubInstance<ClienteService>(ClienteService);
      clienteServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          clienteService: () => clienteServiceStub,
          condicionIvaService: () =>
            sinon.createStubInstance<CondicionIvaService>(CondicionIvaService, {
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
        const wrapper = shallowMount(ClienteUpdate, { global: mountOptions });
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
        const wrapper = shallowMount(ClienteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.cliente = clienteSample;
        clienteServiceStub.update.resolves(clienteSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(clienteServiceStub.update.calledWith(clienteSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        clienteServiceStub.create.resolves(entity);
        const wrapper = shallowMount(ClienteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.cliente = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(clienteServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        clienteServiceStub.find.resolves(clienteSample);
        clienteServiceStub.retrieve.resolves([clienteSample]);

        // WHEN
        route = {
          params: {
            clienteId: `${clienteSample.id}`,
          },
        };
        const wrapper = shallowMount(ClienteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.cliente).toMatchObject(clienteSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        clienteServiceStub.find.resolves(clienteSample);
        const wrapper = shallowMount(ClienteUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
