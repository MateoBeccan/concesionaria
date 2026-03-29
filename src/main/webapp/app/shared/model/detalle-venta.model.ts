import { type IVehiculo } from '@/shared/model/vehiculo.model';
import { type IVenta } from '@/shared/model/venta.model';

export interface IDetalleVenta {
  id?: number;
  precioUnitario?: number;
  cantidad?: number;
  subtotal?: number;
  venta?: IVenta | null;
  vehiculo?: IVehiculo | null;
}

export class DetalleVenta implements IDetalleVenta {
  constructor(
    public id?: number,
    public precioUnitario?: number,
    public cantidad?: number,
    public subtotal?: number,
    public venta?: IVenta | null,
    public vehiculo?: IVehiculo | null,
  ) {}
}
