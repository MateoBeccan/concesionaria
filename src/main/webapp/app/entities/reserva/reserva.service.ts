import axios from 'axios';
import buildPaginationQueryOpts from '@/shared/sort/sorts';
import type { IPago } from '@/shared/model/pago.model';
import type { IReserva } from '@/shared/model/reserva.model';
import type { IVenta } from '@/shared/model/venta.model';

const baseApiUrl = 'api/reservas';

export default class ReservaService {
  async create(entity: IReserva): Promise<IReserva> {
    const res = await axios.post<IReserva>(baseApiUrl, entity);
    return res.data;
  }

  async find(id: number): Promise<IReserva> {
    const res = await axios.get<IReserva>(`${baseApiUrl}/${id}`);
    return res.data;
  }

  async retrieve(paginationQuery?: any): Promise<{ data: IReserva[]; headers: any }> {
    return axios.get(`${baseApiUrl}?${buildPaginationQueryOpts(paginationQuery)}`);
  }

  async cancelar(id: number, motivo?: string): Promise<IReserva> {
    const res = await axios.post<IReserva>(`${baseApiUrl}/${id}/cancelar`, null, { params: motivo ? { motivo } : undefined });
    return res.data;
  }

  async expirarVencidas(): Promise<{ reservasExpiradas: number }> {
    const res = await axios.post<{ reservasExpiradas: number }>(`${baseApiUrl}/expirar-vencidas`);
    return res.data;
  }

  async findActivaByInventarioId(inventarioId: number): Promise<IReserva | null> {
    try {
      const res = await axios.get<IReserva>(`${baseApiUrl}/inventario/${inventarioId}/activa`);
      return res.data;
    } catch (e: any) {
      if (e?.response?.status === 404) {
        return null;
      }
      throw e;
    }
  }

  async pagosByReserva(reservaId: number): Promise<IPago[]> {
    const res = await axios.get<IPago[]>(`api/pagos/by-reserva/${reservaId}`);
    return res.data;
  }

  async ventaByReserva(reservaId: number): Promise<IVenta | null> {
    try {
      const res = await axios.get<IVenta>(`api/ventas/by-reserva/${reservaId}`);
      return res.data;
    } catch (e: any) {
      if (e?.response?.status === 404) {
        return null;
      }
      throw e;
    }
  }
}
