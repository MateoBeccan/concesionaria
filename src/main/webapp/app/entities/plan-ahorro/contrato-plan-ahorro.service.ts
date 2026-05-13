import axios from 'axios';
import { type IContratoPlanAhorro } from '@/shared/model/contrato-plan-ahorro.model';
import { type ICuotaPlanAhorro } from '@/shared/model/cuota-plan-ahorro.model';

const baseApiUrl = 'api/contrato-plan-ahorros';

export default class ContratoPlanAhorroService {
  public retrieve(paginationQuery?: any): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .get(baseApiUrl, { params: paginationQuery })
        .then(res => resolve(res))
        .catch(err => reject(err));
    });
  }

  public find(id: number): Promise<IContratoPlanAhorro> {
    return new Promise<IContratoPlanAhorro>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${id}`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public create(entity: IContratoPlanAhorro): Promise<IContratoPlanAhorro> {
    return new Promise<IContratoPlanAhorro>((resolve, reject) => {
      axios
        .post(baseApiUrl, entity)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public cuotas(contratoId: number): Promise<ICuotaPlanAhorro[]> {
    return new Promise<ICuotaPlanAhorro[]>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${contratoId}/cuotas`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public pagarCuota(cuotaId: number, monto: number, observaciones?: string): Promise<ICuotaPlanAhorro> {
    return new Promise<ICuotaPlanAhorro>((resolve, reject) => {
      axios
        .post(`${baseApiUrl}/cuotas/${cuotaId}/pagar`, { monto, observaciones })
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }
}

