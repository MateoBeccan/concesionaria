import axios from 'axios';

import { type ICliente } from '@/shared/model/cliente.model';
import buildPaginationQueryOpts from '@/shared/sort/sorts';

const baseApiUrl = 'api/clientes';

export default class ClienteService {
  find(id: number): Promise<ICliente> {
    return new Promise<ICliente>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${id}`)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  retrieve(paginationQuery?: any): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}?${buildPaginationQueryOpts(paginationQuery)}`)
        .then(res => {
          resolve(res);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  delete(id: number): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .delete(`${baseApiUrl}/${id}`)
        .then(res => {
          resolve(res);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  create(entity: ICliente): Promise<ICliente> {
    return new Promise<ICliente>((resolve, reject) => {
      axios
        .post(baseApiUrl, entity)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  update(entity: ICliente): Promise<ICliente> {
    return new Promise<ICliente>((resolve, reject) => {
      axios
        .put(`${baseApiUrl}/${entity.id}`, entity)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }
  findByDocumento(nroDocumento: string): Promise<ICliente> {
    return axios
      .get(`/api/clientes/documento/${nroDocumento}`)
      .then(res => res.data);
  }

  partialUpdate(entity: ICliente): Promise<ICliente> {
    return new Promise<ICliente>((resolve, reject) => {
      axios
        .patch(`${baseApiUrl}/${entity.id}`, entity)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }
}
