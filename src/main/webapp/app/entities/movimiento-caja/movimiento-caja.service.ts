import axios from 'axios';
import buildPaginationQueryOpts from '@/shared/sort/sorts';
import { type IMovimientoCaja, type IResumenDiarioCaja } from '@/shared/model/movimiento-caja.model';

const baseApiUrl = 'api/movimientos-caja';

export default class MovimientoCajaService {
  retrieve(paginationQuery?: any): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}?${buildPaginationQueryOpts(paginationQuery)}`)
        .then(res => resolve(res))
        .catch(err => reject(err));
    });
  }

  retrieveWithFilters(filters: {
    fechaDesde?: string;
    fechaHasta?: string;
    usuario?: string;
    metodoPagoId?: number | null;
    entidadFinancieraId?: number | null;
    tipo?: string;
    estado?: string;
    page?: number;
    size?: number;
    sort?: string[];
  }): Promise<any> {
    const params = new URLSearchParams();
    if (filters.fechaDesde) params.append('fechaDesde', filters.fechaDesde);
    if (filters.fechaHasta) params.append('fechaHasta', filters.fechaHasta);
    if (filters.usuario) params.append('usuario', filters.usuario);
    if (filters.metodoPagoId) params.append('metodoPagoId', String(filters.metodoPagoId));
    if (filters.entidadFinancieraId) params.append('entidadFinancieraId', String(filters.entidadFinancieraId));
    if (filters.tipo) params.append('tipo', filters.tipo);
    if (filters.estado) params.append('estado', filters.estado);
    if (filters.page !== undefined) params.append('page', String(filters.page));
    if (filters.size !== undefined) params.append('size', String(filters.size));
    (filters.sort ?? []).forEach(s => params.append('sort', s));
    return new Promise<any>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}?${params.toString()}`)
        .then(res => resolve(res))
        .catch(err => reject(err));
    });
  }

  resumenDiario(fecha?: string): Promise<IResumenDiarioCaja> {
    const suffix = fecha ? `?fecha=${encodeURIComponent(fecha)}` : '';
    return new Promise<IResumenDiarioCaja>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/resumen-diario${suffix}`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  find(id: number): Promise<IMovimientoCaja> {
    return new Promise<IMovimientoCaja>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${id}`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }
}
