import { type EstadoVenta } from '@/shared/model/estado-venta.model';

export interface IVentaHistorial {
  id?: number;
  ventaId?: number | null;
  fecha?: Date | string | null;
  estadoAnterior?: EstadoVenta | null;
  estadoNuevo?: EstadoVenta | null;
  accion?: string | null;
  detalle?: string | null;
  usuario?: string | null;
}
