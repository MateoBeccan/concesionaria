import { type IAuto } from '@/shared/model/auto.model';
import { type IVenta } from '@/shared/model/venta.model';

export interface IDetalleVenta {
  id?: number;
  precioUnitario?: number;
  cantidad?: number;
  subtotal?: number;
  venta?: IVenta | null;
  auto?: IAuto | null;
}

export class DetalleVenta implements IDetalleVenta {
  constructor(
    public id?: number,
    public precioUnitario?: number,
    public cantidad?: number,
    public subtotal?: number,
    public venta?: IVenta | null,
    public auto?: IAuto | null,
  ) {}
}
