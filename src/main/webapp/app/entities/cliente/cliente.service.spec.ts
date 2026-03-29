import { beforeEach, describe, expect, it } from 'vitest';

import axios from 'axios';
import dayjs from 'dayjs';
import sinon from 'sinon';

import { DATE_TIME_FORMAT } from '@/shared/composables/date-format';
import { Cliente } from '@/shared/model/cliente.model';

import ClienteService from './cliente.service';

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
  describe('Cliente Service', () => {
    let service: ClienteService;
    let elemDefault;
    let currentDate: Date;

    beforeEach(() => {
      service = new ClienteService();
      currentDate = new Date();
      elemDefault = new Cliente(
        123,
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        false,
        currentDate,
        currentDate,
        currentDate,
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = {
          fechaAlta: dayjs(currentDate).format(DATE_TIME_FORMAT),
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

      it('should create a Cliente', async () => {
        const returnedFromService = {
          id: 123,
          fechaAlta: dayjs(currentDate).format(DATE_TIME_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = { fechaAlta: currentDate, createdDate: currentDate, lastModifiedDate: currentDate, ...returnedFromService };

        axiosStub.post.resolves({ data: returnedFromService });
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a Cliente', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a Cliente', async () => {
        const returnedFromService = {
          nombre: 'BBBBBB',
          apellido: 'BBBBBB',
          nroDocumento: 'BBBBBB',
          telefono: 'BBBBBB',
          email: 'BBBBBB',
          direccion: 'BBBBBB',
          ciudad: 'BBBBBB',
          provincia: 'BBBBBB',
          pais: 'BBBBBB',
          activo: true,
          fechaAlta: dayjs(currentDate).format(DATE_TIME_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = { fechaAlta: currentDate, createdDate: currentDate, lastModifiedDate: currentDate, ...returnedFromService };
        axiosStub.put.resolves({ data: returnedFromService });

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a Cliente', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a Cliente', async () => {
        const patchObject = {
          nroDocumento: 'BBBBBB',
          telefono: 'BBBBBB',
          email: 'BBBBBB',
          ciudad: 'BBBBBB',
          provincia: 'BBBBBB',
          activo: true,
          fechaAlta: dayjs(currentDate).format(DATE_TIME_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...new Cliente(),
        };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = { fechaAlta: currentDate, createdDate: currentDate, lastModifiedDate: currentDate, ...returnedFromService };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a Cliente', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of Cliente', async () => {
        const returnedFromService = {
          nombre: 'BBBBBB',
          apellido: 'BBBBBB',
          nroDocumento: 'BBBBBB',
          telefono: 'BBBBBB',
          email: 'BBBBBB',
          direccion: 'BBBBBB',
          ciudad: 'BBBBBB',
          provincia: 'BBBBBB',
          pais: 'BBBBBB',
          activo: true,
          fechaAlta: dayjs(currentDate).format(DATE_TIME_FORMAT),
          createdDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          lastModifiedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = { fechaAlta: currentDate, createdDate: currentDate, lastModifiedDate: currentDate, ...returnedFromService };
        axiosStub.get.resolves([returnedFromService]);
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of Cliente', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a Cliente', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a Cliente', async () => {
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
