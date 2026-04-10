import { beforeEach, describe, expect, it } from 'vitest';

import axios from 'axios';
import dayjs from 'dayjs';
import sinon from 'sinon';

import { DATE_FORMAT, DATE_TIME_FORMAT } from '@/shared/composables/date-format';
import { Vehiculo, type IVehiculo } from '@/shared/model/vehiculo.model';

import VehiculoService from './vehiculo.service';

const error = {
  response: {
    status: null,
    data: {
      type: null,
    },
  },
};

const axiosStub = {
  get: sinon.stub(axios, 'get'),
  post: sinon.stub(axios, 'post'),
  put: sinon.stub(axios, 'put'),
  patch: sinon.stub(axios, 'patch'),
  delete: sinon.stub(axios, 'delete'),
};

describe('Service Tests', () => {
  describe('Vehiculo Service', () => {
    let service: VehiculoService;
    let elemDefault: IVehiculo;
    let currentDate: Date;

    beforeEach(() => {
      service = new VehiculoService();
      currentDate = new Date();

      elemDefault = {
        id: 123,
        estado: 'NUEVO',
        condicion: 'EN_VENTA',
        fechaFabricacion: currentDate,
        km: 0,
        patente: 'AAA123',
        precio: 0,
        createdDate: currentDate,
        lastModifiedDate: currentDate,
      };
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = {
          ...elemDefault,
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
        };

        axiosStub.get.resolves({ data: returnedFromService });

        const res = await service.find(123);
        expect(res).toMatchObject(elemDefault);
      });

      it('should not find an element', async () => {
        axiosStub.get.rejects(error);

        await expect(service.find(123)).rejects.toMatchObject(error);
      });

      it('should create a Vehiculo', async () => {
        const returnedFromService = {
          ...elemDefault,
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
        };

        const expected = {
          ...elemDefault,
          fechaFabricacion: currentDate,
          createdDate: currentDate,
          lastModifiedDate: currentDate,
        };

        axiosStub.post.resolves({ data: returnedFromService });

        const res = await service.create(elemDefault);
        expect(res).toMatchObject(expected);
      });

      it('should not create a Vehiculo', async () => {
        axiosStub.post.rejects(error);

        await expect(service.create(elemDefault)).rejects.toMatchObject(error);
      });

      it('should update a Vehiculo', async () => {
        const returnedFromService = {
          ...elemDefault,
          km: 10,
          patente: 'BBB123',
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
        };

        const expected = {
          ...returnedFromService,
          fechaFabricacion: currentDate,
          createdDate: currentDate,
          lastModifiedDate: currentDate,
        };

        axiosStub.put.resolves({ data: returnedFromService });

        const res = await service.update(expected);
        expect(res).toMatchObject(expected);
      });

      it('should not update a Vehiculo', async () => {
        axiosStub.put.rejects(error);

        await expect(service.update(elemDefault)).rejects.toMatchObject(error);
      });

      it('should partial update a Vehiculo', async () => {
        const patchObject: Partial<IVehiculo> = {
          id: 123,
          km: 5,
        };

        const returnedFromService = {
          ...elemDefault,
          ...patchObject,
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
        };

        const expected = {
          ...returnedFromService,
          fechaFabricacion: currentDate,
          createdDate: currentDate,
          lastModifiedDate: currentDate,
        };

        axiosStub.patch.resolves({ data: returnedFromService });

        const res = await service.partialUpdate(patchObject);
        expect(res).toMatchObject(expected);
      });

      it('should not partial update a Vehiculo', async () => {
        axiosStub.patch.rejects(error);

        await expect(service.partialUpdate({})).rejects.toMatchObject(error);
      });

      it('should return a list of Vehiculo', async () => {
        const returnedFromService = {
          ...elemDefault,
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
        };

        axiosStub.get.resolves({
          data: [returnedFromService],
          headers: {},
        });

        const res = await service.retrieve({});

        expect(res.data).toBeDefined();
        expect(res.data[0]).toMatchObject(elemDefault);
      });

      it('should not return a list of Vehiculo', async () => {
        axiosStub.get.rejects(error);

        await expect(service.retrieve()).rejects.toMatchObject(error);
      });

      it('should delete a Vehiculo', async () => {
        axiosStub.delete.resolves({ data: { ok: true } });

        const res = await service.delete(123);
        expect(res.ok).toBeTruthy();
      });

      it('should not delete a Vehiculo', async () => {
        axiosStub.delete.rejects(error);

        await expect(service.delete(123)).rejects.toMatchObject(error);
      });
    });
  });
});
