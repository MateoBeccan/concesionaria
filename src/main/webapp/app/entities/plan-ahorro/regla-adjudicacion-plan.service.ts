import axios from 'axios';
import type { IReglaAdjudicacionPlan } from '@/shared/model/regla-adjudicacion-plan.model';

const baseApiUrl = 'api/reglas-adjudicacion-plan';

export default class ReglaAdjudicacionPlanService {
  public retrieve(active?: boolean): Promise<IReglaAdjudicacionPlan[]> {
    return new Promise((resolve, reject) => {
      axios
        .get(baseApiUrl, { params: active === undefined ? {} : { active } })
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public create(entity: IReglaAdjudicacionPlan): Promise<IReglaAdjudicacionPlan> {
    return new Promise((resolve, reject) => {
      axios
        .post(baseApiUrl, entity)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public update(entity: IReglaAdjudicacionPlan): Promise<IReglaAdjudicacionPlan> {
    return new Promise((resolve, reject) => {
      axios
        .put(`${baseApiUrl}/${entity.id}`, entity)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public deactivate(id: number): Promise<IReglaAdjudicacionPlan> {
    return new Promise((resolve, reject) => {
      axios
        .post(`${baseApiUrl}/${id}/deactivate`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }
}
