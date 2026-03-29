import { beforeEach, describe, expect, it } from 'vitest';

import axios from 'axios';
import dayjs from 'dayjs';
import sinon from 'sinon';

import { DATE_TIME_FORMAT } from '@/shared/composables/date-format';
import { Inventario } from '@/shared/model/inventario.model';

import InventarioService from './inventario.service';

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
  describe('Inventario Service', () => {
    let service: InventarioService;
    let elemDefault;
    let currentDate: Date;

    beforeEach(() => {
      service = new InventarioService();
      currentDate = new Date();
      elemDefault = new Inventario(
        123,
        currentDate,
        'AAAAAAA',
        'DISPONIBLE',
        'AAAAAAA',
        false,
        currentDate,
        currentDate,
        currentDate,
        currentDate,
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = {
          fechaIngreso: dayjs(currentDate).format(DATE_TIME_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          fechaReserva: dayjs(currentDate).format(DATE_TIME_FORMAT),
          fechaVencimientoReserva: dayjs(currentDate).format(DATE_TIME_FORMAT),
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

      it('should create a Inventario', async () => {
        const returnedFromService = {
          id: 123,
          fechaIngreso: dayjs(currentDate).format(DATE_TIME_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          fechaReserva: dayjs(currentDate).format(DATE_TIME_FORMAT),
          fechaVencimientoReserva: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = {
          fechaIngreso: currentDate,
          createdDate: currentDate,
          lastModifiedDate: currentDate,
          fechaReserva: currentDate,
          fechaVencimientoReserva: currentDate,
          ...returnedFromService,
        };

        axiosStub.post.resolves({ data: returnedFromService });
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a Inventario', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a Inventario', async () => {
        const returnedFromService = {
          fechaIngreso: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ubicacion: 'BBBBBB',
          estadoInventario: 'BBBBBB',
          observaciones: 'BBBBBB',
          disponible: true,
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          fechaReserva: dayjs(currentDate).format(DATE_TIME_FORMAT),
          fechaVencimientoReserva: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = {
          fechaIngreso: currentDate,
          createdDate: currentDate,
          lastModifiedDate: currentDate,
          fechaReserva: currentDate,
          fechaVencimientoReserva: currentDate,
          ...returnedFromService,
        };
        axiosStub.put.resolves({ data: returnedFromService });

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a Inventario', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a Inventario', async () => {
        const patchObject = {
          fechaIngreso: dayjs(currentDate).format(DATE_TIME_FORMAT),
          estadoInventario: 'BBBBBB',
          observaciones: 'BBBBBB',
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          fechaVencimientoReserva: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...new Inventario(),
        };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = {
          fechaIngreso: currentDate,
          createdDate: currentDate,
          lastModifiedDate: currentDate,
          fechaReserva: currentDate,
          fechaVencimientoReserva: currentDate,
          ...returnedFromService,
        };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a Inventario', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of Inventario', async () => {
        const returnedFromService = {
          fechaIngreso: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ubicacion: 'BBBBBB',
          estadoInventario: 'BBBBBB',
          observaciones: 'BBBBBB',
          disponible: true,
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          fechaReserva: dayjs(currentDate).format(DATE_TIME_FORMAT),
          fechaVencimientoReserva: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = {
          fechaIngreso: currentDate,
          createdDate: currentDate,
          lastModifiedDate: currentDate,
          fechaReserva: currentDate,
          fechaVencimientoReserva: currentDate,
          ...returnedFromService,
        };
        axiosStub.get.resolves([returnedFromService]);
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of Inventario', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a Inventario', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a Inventario', async () => {
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
