import axios from 'axios';
import type { IVenta } from '@/shared/model/venta.model';
import buildPaginationQueryOpts from '@/shared/sort/sorts';

const baseApiUrl = 'api/ventas';

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
}
