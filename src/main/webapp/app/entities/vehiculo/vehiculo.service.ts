import axios from 'axios';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import buildPaginationQueryOpts from '@/shared/sort/sorts';

const baseApiUrl = 'api/vehiculos';

export default class VehiculoService {
  async find(id: number): Promise<IVehiculo> {
    const res = await axios.get<IVehiculo>(`${baseApiUrl}/${id}`);
    return res.data;
  }

  async retrieve(paginationQuery?: any): Promise<{ data: IVehiculo[]; headers: any }> {
    return axios.get(`${baseApiUrl}?${buildPaginationQueryOpts(paginationQuery)}`);
  }

  async create(entity: IVehiculo): Promise<IVehiculo> {
    const res = await axios.post<IVehiculo>(baseApiUrl, entity);
    return res.data;
  }

  async update(entity: IVehiculo): Promise<IVehiculo> {
    const res = await axios.put<IVehiculo>(`${baseApiUrl}/${entity.id}`, entity);
    return res.data;
  }

  async partialUpdate(entity: IVehiculo): Promise<IVehiculo> {
    const res = await axios.patch<IVehiculo>(`${baseApiUrl}/${entity.id}`, entity);
    return res.data;
  }

  async delete(id: number): Promise<any> {
    const res = await axios.delete(`${baseApiUrl}/${id}`);
    return res.data;
  }

  async findByPatente(patente: string): Promise<IVehiculo> {
    const res = await axios.get<IVehiculo>(`${baseApiUrl}/patente/${patente}`);
    return res.data;
  }

  async buscar(q: string): Promise<IVehiculo> {
    const res = await axios.get<IVehiculo>(`${baseApiUrl}/buscar`, { params: { q } });
    return res.data;
  }

  async vender(vehiculoId: number, clienteId: number): Promise<void> {
    await axios.post(`${baseApiUrl}/${vehiculoId}/vender/${clienteId}`);
  }

  async reservar(vehiculoId: number, clienteId: number): Promise<void> {
    await axios.post(`${baseApiUrl}/${vehiculoId}/reservar/${clienteId}`);
  }

  async cancelarReserva(vehiculoId: number): Promise<void> {
    await axios.post(`${baseApiUrl}/${vehiculoId}/cancelar-reserva`);
  }

}
