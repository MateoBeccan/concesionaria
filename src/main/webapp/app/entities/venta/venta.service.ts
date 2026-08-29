import axios from 'axios';
import type { IComprobante } from '@/shared/model/comprobante.model';
import type { IPago } from '@/shared/model/pago.model';
import type { IVenta } from '@/shared/model/venta.model';
import type { IVentaHistorial } from '@/shared/model/venta-historial.model';
import buildPaginationQueryOpts from '@/shared/sort/sorts';

const baseApiUrl = 'api/ventas';

export interface IConfirmarVentaRequest {
  venta: IVenta;
  pagos: IPago[];
  tipoComprobanteId?: number | null;
}

export interface IConfirmarVentaResponse {
  venta: IVenta;
  pagos: IPago[];
  comprobante?: IComprobante | null;
}

export default class VentaService {
  async find(id: number): Promise<IVenta> {
    const res = await axios.get<IVenta>(`${baseApiUrl}/${id}`);
    return res.data;
  }

  async retrieve(paginationQuery?: any): Promise<{ data: IVenta[]; headers: any }> {
    return axios.get(`${baseApiUrl}?${buildPaginationQueryOpts(paginationQuery)}`);
  }

  async create(entity: IVenta): Promise<IVenta> {
    const res = await axios.post<IVenta>(baseApiUrl, entity);
    return res.data;
  }

  async update(entity: IVenta): Promise<IVenta> {
    const res = await axios.put<IVenta>(`${baseApiUrl}/${entity.id}`, entity);
    return res.data;
  }

  async partialUpdate(entity: IVenta): Promise<IVenta> {
    const res = await axios.patch<IVenta>(`${baseApiUrl}/${entity.id}`, entity);
    return res.data;
  }

  async delete(id: number): Promise<void> {
    await axios.delete(`${baseApiUrl}/${id}`);
  }

  async historial(id: number): Promise<IVentaHistorial[]> {
    const res = await axios.get<IVentaHistorial[]>(`${baseApiUrl}/${id}/historial`);
    return res.data;
  }

  async confirmarVenta(payload: IConfirmarVentaRequest, idempotencyKey: string): Promise<IConfirmarVentaResponse> {
    const res = await axios.post<IConfirmarVentaResponse>(`${baseApiUrl}/confirmar`, payload, {
      headers: { 'Idempotency-Key': idempotencyKey },
    });
    return res.data;
  }
}
