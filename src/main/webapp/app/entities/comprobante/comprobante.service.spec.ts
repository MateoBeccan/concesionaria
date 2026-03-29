import { beforeEach, describe, expect, it } from 'vitest';

import axios from 'axios';
import dayjs from 'dayjs';
import sinon from 'sinon';

import { DATE_TIME_FORMAT } from '@/shared/composables/date-format';
import { Comprobante } from '@/shared/model/comprobante.model';

import ComprobanteService from './comprobante.service';

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
  describe('Comprobante Service', () => {
    let service: ComprobanteService;
    let elemDefault;
    let currentDate: Date;

    beforeEach(() => {
      service = new ComprobanteService();
      currentDate = new Date();
      elemDefault = new Comprobante(123, 'AAAAAAA', currentDate, 0, 0, 0, currentDate);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = {
          fechaEmision: dayjs(currentDate).format(DATE_TIME_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
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

      it('should create a Comprobante', async () => {
        const returnedFromService = {
          id: 123,
          fechaEmision: dayjs(currentDate).format(DATE_TIME_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = { fechaEmision: currentDate, createdDate: currentDate, ...returnedFromService };

        axiosStub.post.resolves({ data: returnedFromService });
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a Comprobante', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a Comprobante', async () => {
        const returnedFromService = {
          numeroComprobante: 'BBBBBB',
          fechaEmision: dayjs(currentDate).format(DATE_TIME_FORMAT),
          importeNeto: 1,
          impuesto: 1,
          total: 1,
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = { fechaEmision: currentDate, createdDate: currentDate, ...returnedFromService };
        axiosStub.put.resolves({ data: returnedFromService });

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a Comprobante', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a Comprobante', async () => {
        const patchObject = { numeroComprobante: 'BBBBBB', impuesto: 1, ...new Comprobante() };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = { fechaEmision: currentDate, createdDate: currentDate, ...returnedFromService };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a Comprobante', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of Comprobante', async () => {
        const returnedFromService = {
          numeroComprobante: 'BBBBBB',
          fechaEmision: dayjs(currentDate).format(DATE_TIME_FORMAT),
          importeNeto: 1,
          impuesto: 1,
          total: 1,
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = { fechaEmision: currentDate, createdDate: currentDate, ...returnedFromService };
        axiosStub.get.resolves([returnedFromService]);
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of Comprobante', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a Comprobante', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a Comprobante', async () => {
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
