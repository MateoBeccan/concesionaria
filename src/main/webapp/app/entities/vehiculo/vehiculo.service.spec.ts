import { beforeEach, describe, expect, it } from 'vitest';

import axios from 'axios';
import dayjs from 'dayjs';
import sinon from 'sinon';

import { DATE_FORMAT, DATE_TIME_FORMAT } from '@/shared/composables/date-format';
import { Vehiculo } from '@/shared/model/vehiculo.model';

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
    let elemDefault;
    let currentDate: Date;

    beforeEach(() => {
      service = new VehiculoService();
      currentDate = new Date();
      elemDefault = new Vehiculo(123, 'NUEVO', currentDate, 0, 'AAAAAAA', 0, currentDate, currentDate);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = {
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        axiosStub.get.resolves({ data: returnedFromService });

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });

      it('should not find an element', async () => {
        axiosStub.get.rejects(error);
        return service
          .find(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should create a Vehiculo', async () => {
        const returnedFromService = {
          id: 123,
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = { fechaFabricacion: currentDate, createdDate: currentDate, lastModifiedDate: currentDate, ...returnedFromService };

        axiosStub.post.resolves({ data: returnedFromService });
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a Vehiculo', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a Vehiculo', async () => {
        const returnedFromService = {
          estado: 'BBBBBB',
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          km: 1,
          patente: 'BBBBBB',
          precio: 1,
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = { fechaFabricacion: currentDate, createdDate: currentDate, lastModifiedDate: currentDate, ...returnedFromService };
        axiosStub.put.resolves({ data: returnedFromService });

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a Vehiculo', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a Vehiculo', async () => {
        const patchObject = {
          estado: 'BBBBBB',
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          km: 1,
          patente: 'BBBBBB',
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...new Vehiculo(),
        };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = { fechaFabricacion: currentDate, createdDate: currentDate, lastModifiedDate: currentDate, ...returnedFromService };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a Vehiculo', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of Vehiculo', async () => {
        const returnedFromService = {
          estado: 'BBBBBB',
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          km: 1,
          patente: 'BBBBBB',
          precio: 1,
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = { fechaFabricacion: currentDate, createdDate: currentDate, lastModifiedDate: currentDate, ...returnedFromService };
        axiosStub.get.resolves([returnedFromService]);
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of Vehiculo', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a Vehiculo', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a Vehiculo', async () => {
        axiosStub.delete.rejects(error);

        return service
          .delete(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });
    });
  });
});
