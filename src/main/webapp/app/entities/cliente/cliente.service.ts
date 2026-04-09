import axios from 'axios';
import type { ICliente } from '@/shared/model/cliente.model';
import buildPaginationQueryOpts from '@/shared/sort/sorts';

const baseApiUrl = 'api/clientes';

export default class ClienteService {
  async find(id: number): Promise<ICliente> {
    const res = await axios.get<ICliente>(`${baseApiUrl}/${id}`);
    return res.data;
  }

  async retrieve(paginationQuery?: any): Promise<{ data: ICliente[]; headers: any }> {
    return axios.get(`${baseApiUrl}?${buildPaginationQueryOpts(paginationQuery)}`);
  }

  async create(entity: ICliente): Promise<ICliente> {
    const res = await axios.post<ICliente>(baseApiUrl, entity);
    return res.data;
  }

  async update(entity: ICliente): Promise<ICliente> {
    const res = await axios.put<ICliente>(`${baseApiUrl}/${entity.id}`, entity);
    return res.data;
  }

  async partialUpdate(entity: ICliente): Promise<ICliente> {
    const res = await axios.patch<ICliente>(`${baseApiUrl}/${entity.id}`, entity);
    return res.data;
  }

  async delete(id: number): Promise<void> {
    await axios.delete(`${baseApiUrl}/${id}`);
  }

  async findByDocumento(nroDocumento: string): Promise<ICliente> {
    const res = await axios.get<ICliente>(`${baseApiUrl}/documento/${nroDocumento}`);
    return res.data;
  }

  // Requiere endpoint backend: GET /api/clientes/buscar?q=
  // Busca por nombre, apellido, nroDocumento o email
  async buscarPorQuery(q: string): Promise<ICliente[]> {
    const res = await axios.get<ICliente[]>(`${baseApiUrl}/buscar`, { params: { q } });
    return res.data;
  }
}
