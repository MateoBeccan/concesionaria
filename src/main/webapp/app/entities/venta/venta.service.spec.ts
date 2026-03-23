import { beforeEach, describe, expect, it } from 'vitest';

import axios from 'axios';
import dayjs from 'dayjs';
import sinon from 'sinon';

import { DATE_TIME_FORMAT } from '@/shared/composables/date-format';
import { Venta } from '@/shared/model/venta.model';

import VentaService from './venta.service';

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
  describe('Venta Service', () => {
    let service: VentaService;
    let elemDefault;
    let currentDate: Date;

    beforeEach(() => {
      service = new VentaService();
      currentDate = new Date();
      elemDefault = new Venta(123, currentDate, 0, 0, 0, 0, 0, 0, 0, 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = { fecha: dayjs(currentDate).format(DATE_TIME_FORMAT), ...elemDefault };
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

      it('should create a Venta', async () => {
        const returnedFromService = { id: 123, fecha: dayjs(currentDate).format(DATE_TIME_FORMAT), ...elemDefault };
        const expected = { fecha: currentDate, ...returnedFromService };

        axiosStub.post.resolves({ data: returnedFromService });
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a Venta', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a Venta', async () => {
        const returnedFromService = {
          fecha: dayjs(currentDate).format(DATE_TIME_FORMAT),
          cotizacion: 1,
          importeNeto: 1,
          impuesto: 1,
          total: 1,
          porcentajeImpuesto: 1,
          totalPagado: 1,
          saldo: 1,
          observaciones: 'BBBBBB',
          ...elemDefault,
        };

        const expected = { fecha: currentDate, ...returnedFromService };
        axiosStub.put.resolves({ data: returnedFromService });

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a Venta', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a Venta', async () => {
        const patchObject = { importeNeto: 1, impuesto: 1, total: 1, saldo: 1, observaciones: 'BBBBBB', ...new Venta() };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = { fecha: currentDate, ...returnedFromService };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a Venta', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of Venta', async () => {
        const returnedFromService = {
          fecha: dayjs(currentDate).format(DATE_TIME_FORMAT),
          cotizacion: 1,
          importeNeto: 1,
          impuesto: 1,
          total: 1,
          porcentajeImpuesto: 1,
          totalPagado: 1,
          saldo: 1,
          observaciones: 'BBBBBB',
          ...elemDefault,
        };
        const expected = { fecha: currentDate, ...returnedFromService };
        axiosStub.get.resolves([returnedFromService]);
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of Venta', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a Venta', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a Venta', async () => {
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
