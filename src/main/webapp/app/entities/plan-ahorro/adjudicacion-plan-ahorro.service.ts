import axios from 'axios';
import type { IAdjudicacionPlanAhorro } from '@/shared/model/adjudicacion-plan-ahorro.model';
import type { IInventario } from '@/shared/model/inventario.model';

const baseApiUrl = 'api/adjudicaciones-plan-ahorro';

export default class AdjudicacionPlanAhorroService {
  public retrieve(paginationQuery?: any): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .get(baseApiUrl, { params: paginationQuery })
        .then(res => resolve(res))
        .catch(err => reject(err));
    });
  }

  public findByContrato(contratoId: number): Promise<IAdjudicacionPlanAhorro | null> {
    return new Promise((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/contratos/${contratoId}`)
        .then(res => resolve(res.data))
        .catch(err => {
          if (err?.response?.status === 404) {
            resolve(null);
            return;
          }
          reject(err);
        });
    });
  }

  public adjudicarContrato(contratoId: number, observaciones?: string): Promise<IAdjudicacionPlanAhorro> {
    return new Promise((resolve, reject) => {
      axios
        .post(`${baseApiUrl}/contratos/${contratoId}/adjudicar`, { observaciones })
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public inventariosCompatibles(contratoId: number): Promise<IInventario[]> {
    return new Promise((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/contratos/${contratoId}/inventarios-compatibles`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public asignarInventario(adjudicacionId: number, inventarioId: number): Promise<IAdjudicacionPlanAhorro> {
    return new Promise((resolve, reject) => {
      axios
        .post(`${baseApiUrl}/${adjudicacionId}/inventario/${inventarioId}`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  public generarVenta(adjudicacionId: number): Promise<IAdjudicacionPlanAhorro> {
    return new Promise((resolve, reject) => {
      axios
        .post(`${baseApiUrl}/${adjudicacionId}/generar-venta`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }
}
