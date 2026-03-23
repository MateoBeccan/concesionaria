import { beforeEach, describe, expect, it, vitest } from 'vitest';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import EstadoVentaService from './estado-venta.service';
import EstadoVenta from './estado-venta.vue';

type EstadoVentaComponentType = InstanceType<typeof EstadoVenta>;

const bModalStub = {
  render: () => {},
  methods: {
    hide: () => {},
    show: () => {},
  },
};

describe('Component Tests', () => {
  let alertService: AlertService;

  describe('EstadoVenta Management Component', () => {
    let estadoVentaServiceStub: SinonStubbedInstance<EstadoVentaService>;
    let mountOptions: MountingOptions<EstadoVentaComponentType>['global'];

    beforeEach(() => {
      estadoVentaServiceStub = sinon.createStubInstance<EstadoVentaService>(EstadoVentaService);
      estadoVentaServiceStub.retrieve.resolves({ headers: {} });

      alertService = new AlertService({
        toast: {
          show: vitest.fn(),
        } as any,
      });

      mountOptions = {
        stubs: {
          jhiItemCount: true,
          bPagination: true,
          bModal: bModalStub as any,
          'font-awesome-icon': true,
          'b-badge': true,
          'jhi-sort-indicator': true,
          'b-button': true,
          'router-link': true,
        },
        directives: {
          'b-modal': {},
        },
        provide: {
          alertService,
          estadoVentaService: () => estadoVentaServiceStub,
        },
      };
    });

    describe('Mount', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        estadoVentaServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        const wrapper = shallowMount(EstadoVenta, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(estadoVentaServiceStub.retrieve.calledOnce).toBeTruthy();
        expect(comp.estadoVentas[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for an id', async () => {
        // WHEN
        const wrapper = shallowMount(EstadoVenta, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(estadoVentaServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['id,asc'],
        });
      });
    });
    describe('Handles', () => {
      let comp: EstadoVentaComponentType;

      beforeEach(async () => {
        const wrapper = shallowMount(EstadoVenta, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();
        estadoVentaServiceStub.retrieve.reset();
        estadoVentaServiceStub.retrieve.resolves({ headers: {}, data: [] });
      });

      it('should load a page', async () => {
        // GIVEN
        estadoVentaServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.page = 2;
        await comp.$nextTick();

        // THEN
        expect(estadoVentaServiceStub.retrieve.called).toBeTruthy();
        expect(comp.estadoVentas[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should not load a page if the page is the same as the previous page', () => {
        // WHEN
        comp.page = 1;

        // THEN
        expect(estadoVentaServiceStub.retrieve.called).toBeFalsy();
      });

      it('should re-initialize the page', async () => {
        // GIVEN
        comp.page = 2;
        await comp.$nextTick();
        estadoVentaServiceStub.retrieve.reset();
        estadoVentaServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.clear();
        await comp.$nextTick();

        // THEN
        expect(comp.page).toEqual(1);
        expect(estadoVentaServiceStub.retrieve.callCount).toEqual(1);
        expect(comp.estadoVentas[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for a non-id attribute', async () => {
        // WHEN
        comp.propOrder = 'name';
        await comp.$nextTick();

        // THEN
        expect(estadoVentaServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['name,asc', 'id'],
        });
      });

      it('Should call delete service on confirmDelete', async () => {
        // GIVEN
        estadoVentaServiceStub.delete.resolves({});

        // WHEN
        comp.prepareRemove({ id: 123 });

        comp.removeEstadoVenta();
        await comp.$nextTick(); // clear components

        // THEN
        expect(estadoVentaServiceStub.delete.called).toBeTruthy();

        // THEN
        await comp.$nextTick(); // handle component clear watch
        expect(estadoVentaServiceStub.retrieve.callCount).toEqual(1);
      });
    });
  });
});
