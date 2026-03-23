import { beforeEach, describe, expect, it, vitest } from 'vitest';

import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import AlertService from '@/shared/alert/alert.service';

import CondicionIvaService from './condicion-iva.service';
import CondicionIva from './condicion-iva.vue';

type CondicionIvaComponentType = InstanceType<typeof CondicionIva>;

const bModalStub = {
  render: () => {},
  methods: {
    hide: () => {},
    show: () => {},
  },
};

describe('Component Tests', () => {
  let alertService: AlertService;

  describe('CondicionIva Management Component', () => {
    let condicionIvaServiceStub: SinonStubbedInstance<CondicionIvaService>;
    let mountOptions: MountingOptions<CondicionIvaComponentType>['global'];

    beforeEach(() => {
      condicionIvaServiceStub = sinon.createStubInstance<CondicionIvaService>(CondicionIvaService);
      condicionIvaServiceStub.retrieve.resolves({ headers: {} });

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
          condicionIvaService: () => condicionIvaServiceStub,
        },
      };
    });

    describe('Mount', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        condicionIvaServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        const wrapper = shallowMount(CondicionIva, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(condicionIvaServiceStub.retrieve.calledOnce).toBeTruthy();
        expect(comp.condicionIvas[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for an id', async () => {
        // WHEN
        const wrapper = shallowMount(CondicionIva, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(condicionIvaServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['id,asc'],
        });
      });
    });
    describe('Handles', () => {
      let comp: CondicionIvaComponentType;

      beforeEach(async () => {
        const wrapper = shallowMount(CondicionIva, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();
        condicionIvaServiceStub.retrieve.reset();
        condicionIvaServiceStub.retrieve.resolves({ headers: {}, data: [] });
      });

      it('should load a page', async () => {
        // GIVEN
        condicionIvaServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.page = 2;
        await comp.$nextTick();

        // THEN
        expect(condicionIvaServiceStub.retrieve.called).toBeTruthy();
        expect(comp.condicionIvas[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should not load a page if the page is the same as the previous page', () => {
        // WHEN
        comp.page = 1;

        // THEN
        expect(condicionIvaServiceStub.retrieve.called).toBeFalsy();
      });

      it('should re-initialize the page', async () => {
        // GIVEN
        comp.page = 2;
        await comp.$nextTick();
        condicionIvaServiceStub.retrieve.reset();
        condicionIvaServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.clear();
        await comp.$nextTick();

        // THEN
        expect(comp.page).toEqual(1);
        expect(condicionIvaServiceStub.retrieve.callCount).toEqual(1);
        expect(comp.condicionIvas[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for a non-id attribute', async () => {
        // WHEN
        comp.propOrder = 'name';
        await comp.$nextTick();

        // THEN
        expect(condicionIvaServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['name,asc', 'id'],
        });
      });

      it('Should call delete service on confirmDelete', async () => {
        // GIVEN
        condicionIvaServiceStub.delete.resolves({});

        // WHEN
        comp.prepareRemove({ id: 123 });

        comp.removeCondicionIva();
        await comp.$nextTick(); // clear components

        // THEN
        expect(condicionIvaServiceStub.delete.called).toBeTruthy();

        // THEN
        await comp.$nextTick(); // handle component clear watch
        expect(condicionIvaServiceStub.retrieve.callCount).toEqual(1);
      });
    });
  });
});
