import axios from 'axios';
import buildPaginationQueryOpts from '@/shared/sort/sorts';
import type { ITasacionUsado } from '@/shared/model/tasacion-usado.model';

const baseApiUrl = 'api/tasacion-usados';

export default class TasacionUsadoService {
  async create(entity: ITasacionUsado): Promise<ITasacionUsado> {
    const res = await axios.post<ITasacionUsado>(baseApiUrl, entity);
    return res.data;
  }

  async update(entity: ITasacionUsado): Promise<ITasacionUsado> {
    const res = await axios.put<ITasacionUsado>(`${baseApiUrl}/${entity.id}`, entity);
    return res.data;
  }

  async find(id: number): Promise<ITasacionUsado> {
    const res = await axios.get<ITasacionUsado>(`${baseApiUrl}/${id}`);
    return res.data;
  }

  async retrieve(paginationQuery?: any): Promise<{ data: ITasacionUsado[]; headers: any }> {
    return axios.get(`${baseApiUrl}?${buildPaginationQueryOpts(paginationQuery)}`);
  }

  async aceptadasDisponibles(clienteId: number): Promise<ITasacionUsado[]> {
    const res = await axios.get<ITasacionUsado[]>(`${baseApiUrl}/aceptadas-disponibles`, { params: { clienteId } });
    return res.data;
  }
}

