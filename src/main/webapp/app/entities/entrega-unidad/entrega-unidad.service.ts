import axios from 'axios';
import type { IEntregaChecklistItem } from '@/shared/model/entrega-checklist-item.model';
import type { IEntregaUnidad } from '@/shared/model/entrega-unidad.model';

const baseApiUrl = 'api/entregas-unidad';

export default class EntregaUnidadService {
  public retrieve(params?: any): Promise<any> {
    return axios.get(baseApiUrl, { params });
  }

  public findByVenta(ventaId: number): Promise<IEntregaUnidad> {
    return axios.get(`${baseApiUrl}/venta/${ventaId}`).then(res => res.data);
  }

  public programarEntrega(ventaId: number, fechaProgramada: string, observaciones?: string): Promise<IEntregaUnidad> {
    return axios.post(`${baseApiUrl}/ventas/${ventaId}/programar`, { fechaProgramada, observaciones }).then(res => res.data);
  }

  public actualizarChecklist(entregaId: number, items: IEntregaChecklistItem[]): Promise<IEntregaUnidad> {
    return axios.put(`${baseApiUrl}/${entregaId}/checklist`, items).then(res => res.data);
  }

  public confirmarEntrega(
    entregaId: number,
    kilometrajeEntrega?: number | null,
    nivelCombustible?: string | null,
    observaciones?: string | null,
  ): Promise<IEntregaUnidad> {
    return axios.post(`${baseApiUrl}/${entregaId}/confirmar`, { kilometrajeEntrega, nivelCombustible, observaciones }).then(res => res.data);
  }

  public cancelarEntrega(entregaId: number, motivo?: string): Promise<IEntregaUnidad> {
    return axios.post(`${baseApiUrl}/${entregaId}/cancelar`, { motivo }).then(res => res.data);
  }
}

