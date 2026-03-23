import { beforeEach, describe, expect, it, vitest } from 'vitest';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import TipoComprobanteService from './tipo-comprobante.service';
import TipoComprobante from './tipo-comprobante.vue';

type TipoComprobanteComponentType = InstanceType<typeof TipoComprobante>;

const bModalStub = {
  render: () => {},
  methods: {
    hide: () => {},
    show: () => {},
  },
};

describe('Component Tests', () => {
  let alertService: AlertService;

  describe('TipoComprobante Management Component', () => {
    let tipoComprobanteServiceStub: SinonStubbedInstance<TipoComprobanteService>;
    let mountOptions: MountingOptions<TipoComprobanteComponentType>['global'];

    beforeEach(() => {
      tipoComprobanteServiceStub = sinon.createStubInstance<TipoComprobanteService>(TipoComprobanteService);
      tipoComprobanteServiceStub.retrieve.resolves({ headers: {} });

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
          tipoComprobanteService: () => tipoComprobanteServiceStub,
        },
      };
    });

    describe('Mount', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        tipoComprobanteServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        const wrapper = shallowMount(TipoComprobante, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(tipoComprobanteServiceStub.retrieve.calledOnce).toBeTruthy();
        expect(comp.tipoComprobantes[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for an id', async () => {
        // WHEN
        const wrapper = shallowMount(TipoComprobante, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(tipoComprobanteServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['id,asc'],
        });
      });
    });
    describe('Handles', () => {
      let comp: TipoComprobanteComponentType;

      beforeEach(async () => {
        const wrapper = shallowMount(TipoComprobante, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();
        tipoComprobanteServiceStub.retrieve.reset();
        tipoComprobanteServiceStub.retrieve.resolves({ headers: {}, data: [] });
      });

      it('should load a page', async () => {
        // GIVEN
        tipoComprobanteServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.page = 2;
        await comp.$nextTick();

        // THEN
        expect(tipoComprobanteServiceStub.retrieve.called).toBeTruthy();
        expect(comp.tipoComprobantes[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should not load a page if the page is the same as the previous page', () => {
        // WHEN
        comp.page = 1;

        // THEN
        expect(tipoComprobanteServiceStub.retrieve.called).toBeFalsy();
      });

      it('should re-initialize the page', async () => {
        // GIVEN
        comp.page = 2;
        await comp.$nextTick();
        tipoComprobanteServiceStub.retrieve.reset();
        tipoComprobanteServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.clear();
        await comp.$nextTick();

        // THEN
        expect(comp.page).toEqual(1);
        expect(tipoComprobanteServiceStub.retrieve.callCount).toEqual(1);
        expect(comp.tipoComprobantes[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for a non-id attribute', async () => {
        // WHEN
        comp.propOrder = 'name';
        await comp.$nextTick();

        // THEN
        expect(tipoComprobanteServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['name,asc', 'id'],
        });
      });

      it('Should call delete service on confirmDelete', async () => {
        // GIVEN
        tipoComprobanteServiceStub.delete.resolves({});

        // WHEN
        comp.prepareRemove({ id: 123 });

        comp.removeTipoComprobante();
        await comp.$nextTick(); // clear components

        // THEN
        expect(tipoComprobanteServiceStub.delete.called).toBeTruthy();

        // THEN
        await comp.$nextTick(); // handle component clear watch
        expect(tipoComprobanteServiceStub.retrieve.callCount).toEqual(1);
      });
    });
  });
});
