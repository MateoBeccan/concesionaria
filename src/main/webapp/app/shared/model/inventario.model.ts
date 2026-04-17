import { type ICliente } from '@/shared/model/cliente.model';
import { type EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import { type IVehiculo } from '@/shared/model/vehiculo.model';
export interface IInventario {
  id?: number;
  fechaIngreso?: Date;
  ubicacion?: string | null;
  estadoInventario?: keyof typeof EstadoInventario;
  observaciones?: string | null;
  createdDate?: Date | null;
  createdBy?: string | null;
  lastModifiedDate?: Date | null;
  lastModifiedBy?: string | null;
  fechaReserva?: Date | null;
  fechaVencimientoReserva?: Date | null;
  vehiculo?: IVehiculo | null;
  clienteReserva?: ICliente | null;
}

export class Inventario implements IInventario {
  constructor(
    public id?: number,
    public fechaIngreso?: Date,
    public ubicacion?: string | null,
    public estadoInventario?: keyof typeof EstadoInventario,
    public observaciones?: string | null,
    public createdDate?: Date | null,
    public createdBy?: string | null,
    public lastModifiedDate?: Date | null,
    public lastModifiedBy?: string | null,
    public fechaReserva?: Date | null,
    public fechaVencimientoReserva?: Date | null,
    public vehiculo?: IVehiculo | null,
    public clienteReserva?: ICliente | null,
  ) {}
}
