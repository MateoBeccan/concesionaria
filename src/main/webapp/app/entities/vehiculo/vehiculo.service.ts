import axios from 'axios';

import { type IVehiculo } from '@/shared/model/vehiculo.model';
import buildPaginationQueryOpts from '@/shared/sort/sorts';

const baseApiUrl = 'api/vehiculos';

export default class VehiculoService {
  find(id: number): Promise<IVehiculo> {
    return new Promise<IVehiculo>((resolve, reject) => {
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

  create(entity: IVehiculo): Promise<IVehiculo> {
    return new Promise<IVehiculo>((resolve, reject) => {
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

  update(entity: IVehiculo): Promise<IVehiculo> {
    return new Promise<IVehiculo>((resolve, reject) => {
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

  partialUpdate(entity: IVehiculo): Promise<IVehiculo> {
    return new Promise<IVehiculo>((resolve, reject) => {
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
  findByPatente(patente: string): Promise<IVehiculo> {
    return new Promise((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/patente/${patente}`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });

  }
  vender(vehiculoId: number, clienteId: number) {
    return axios.post(`/api/vehiculos/${vehiculoId}/vender/${clienteId}`);
  }
}
