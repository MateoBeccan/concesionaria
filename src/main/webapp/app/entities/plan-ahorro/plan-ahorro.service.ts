import axios from 'axios';
import { type IPlanAhorro } from '@/shared/model/plan-ahorro.model';

const baseApiUrl = 'api/plan-ahorros';

export default class PlanAhorroService {
  public find(id: number): Promise<IPlanAhorro> {
    return new Promise<IPlanAhorro>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${id}`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public retrieve(paginationQuery?: any): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .get(baseApiUrl, { params: paginationQuery })
        .then(res => resolve(res))
        .catch(err => reject(err));
    });
  }

  public create(entity: IPlanAhorro): Promise<IPlanAhorro> {
    return new Promise<IPlanAhorro>((resolve, reject) => {
      axios
        .post(baseApiUrl, entity)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public update(entity: IPlanAhorro): Promise<IPlanAhorro> {
    return new Promise<IPlanAhorro>((resolve, reject) => {
      axios
        .put(`${baseApiUrl}/${entity.id}`, entity)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }
}

