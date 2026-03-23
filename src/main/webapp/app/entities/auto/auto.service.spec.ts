import { beforeEach, describe, expect, it } from 'vitest';

import axios from 'axios';
import dayjs from 'dayjs';
import sinon from 'sinon';

import { DATE_FORMAT } from '@/shared/composables/date-format';
import { Auto } from '@/shared/model/auto.model';

import AutoService from './auto.service';

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
  describe('Auto Service', () => {
    let service: AutoService;
    let elemDefault;
    let currentDate: Date;

    beforeEach(() => {
      service = new AutoService();
      currentDate = new Date();
      elemDefault = new Auto(123, 'NUEVO', 'EN_VENTA', currentDate, currentDate, 0, 'AAAAAAA', 0);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = {
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          fechaIngreso: dayjs(currentDate).format(DATE_FORMAT),
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

      it('should create a Auto', async () => {
        const returnedFromService = {
          id: 123,
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          fechaIngreso: dayjs(currentDate).format(DATE_FORMAT),
          ...elemDefault,
        };
        const expected = { fechaFabricacion: currentDate, fechaIngreso: currentDate, ...returnedFromService };

        axiosStub.post.resolves({ data: returnedFromService });
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a Auto', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a Auto', async () => {
        const returnedFromService = {
          estado: 'BBBBBB',
          condicion: 'BBBBBB',
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          fechaIngreso: dayjs(currentDate).format(DATE_FORMAT),
          km: 1,
          patente: 'BBBBBB',
          precio: 1,
          ...elemDefault,
        };

        const expected = { fechaFabricacion: currentDate, fechaIngreso: currentDate, ...returnedFromService };
        axiosStub.put.resolves({ data: returnedFromService });

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a Auto', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a Auto', async () => {
        const patchObject = {
          estado: 'BBBBBB',
          condicion: 'BBBBBB',
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          km: 1,
          patente: 'BBBBBB',
          ...new Auto(),
        };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = { fechaFabricacion: currentDate, fechaIngreso: currentDate, ...returnedFromService };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a Auto', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of Auto', async () => {
        const returnedFromService = {
          estado: 'BBBBBB',
          condicion: 'BBBBBB',
          fechaFabricacion: dayjs(currentDate).format(DATE_FORMAT),
          fechaIngreso: dayjs(currentDate).format(DATE_FORMAT),
          km: 1,
          patente: 'BBBBBB',
          precio: 1,
          ...elemDefault,
        };
        const expected = { fechaFabricacion: currentDate, fechaIngreso: currentDate, ...returnedFromService };
        axiosStub.get.resolves([returnedFromService]);
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of Auto', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a Auto', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a Auto', async () => {
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
