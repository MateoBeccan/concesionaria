import { beforeEach, describe, expect, it, vitest } from 'vitest';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import DetalleVentaService from './detalle-venta.service';
import DetalleVenta from './detalle-venta.vue';

type DetalleVentaComponentType = InstanceType<typeof DetalleVenta>;

const bModalStub = {
  render: () => {},
  methods: {
    hide: () => {},
    show: () => {},
  },
};

describe('Component Tests', () => {
  let alertService: AlertService;

  describe('DetalleVenta Management Component', () => {
    let detalleVentaServiceStub: SinonStubbedInstance<DetalleVentaService>;
    let mountOptions: MountingOptions<DetalleVentaComponentType>['global'];

    beforeEach(() => {
      detalleVentaServiceStub = sinon.createStubInstance<DetalleVentaService>(DetalleVentaService);
      detalleVentaServiceStub.retrieve.resolves({ headers: {} });

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
          detalleVentaService: () => detalleVentaServiceStub,
        },
      };
    });

    describe('Mount', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        detalleVentaServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        const wrapper = shallowMount(DetalleVenta, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(detalleVentaServiceStub.retrieve.calledOnce).toBeTruthy();
        expect(comp.detalleVentas[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for an id', async () => {
        // WHEN
        const wrapper = shallowMount(DetalleVenta, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(detalleVentaServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['id,asc'],
        });
      });
    });
    describe('Handles', () => {
      let comp: DetalleVentaComponentType;

      beforeEach(async () => {
        const wrapper = shallowMount(DetalleVenta, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();
        detalleVentaServiceStub.retrieve.reset();
        detalleVentaServiceStub.retrieve.resolves({ headers: {}, data: [] });
      });

      it('should load a page', async () => {
        // GIVEN
        detalleVentaServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.page = 2;
        await comp.$nextTick();

        // THEN
        expect(detalleVentaServiceStub.retrieve.called).toBeTruthy();
        expect(comp.detalleVentas[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should not load a page if the page is the same as the previous page', () => {
        // WHEN
        comp.page = 1;

        // THEN
        expect(detalleVentaServiceStub.retrieve.called).toBeFalsy();
      });

      it('should re-initialize the page', async () => {
        // GIVEN
        comp.page = 2;
        await comp.$nextTick();
        detalleVentaServiceStub.retrieve.reset();
        detalleVentaServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.clear();
        await comp.$nextTick();

        // THEN
        expect(comp.page).toEqual(1);
        expect(detalleVentaServiceStub.retrieve.callCount).toEqual(1);
        expect(comp.detalleVentas[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for a non-id attribute', async () => {
        // WHEN
        comp.propOrder = 'name';
        await comp.$nextTick();

        // THEN
        expect(detalleVentaServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['name,asc', 'id'],
        });
      });

      it('Should call delete service on confirmDelete', async () => {
        // GIVEN
        detalleVentaServiceStub.delete.resolves({});

        // WHEN
        comp.prepareRemove({ id: 123 });

        comp.removeDetalleVenta();
        await comp.$nextTick(); // clear components

        // THEN
        expect(detalleVentaServiceStub.delete.called).toBeTruthy();

        // THEN
        await comp.$nextTick(); // handle component clear watch
        expect(detalleVentaServiceStub.retrieve.callCount).toEqual(1);
      });
    });
  });
});
