import { type ICliente } from '@/shared/model/cliente.model';
import { type EstadoReserva } from '@/shared/model/enumerations/estado-reserva.model';
import { type IInventario } from '@/shared/model/inventario.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type IVenta } from '@/shared/model/venta.model';

export interface IReserva {
  id?: number;
  fechaReserva?: Date;
  fechaVencimiento?: Date;
  montoSenia?: number | null;
  estado?: EstadoReserva;
  observaciones?: string | null;
  usuarioCreacion?: string | null;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;
  inventario?: IInventario | null;
  cliente?: ICliente | null;
  ventaAsociada?: IVenta | null;
  moneda?: IMoneda | null;
}

export class Reserva implements IReserva {
  constructor(
    public id?: number,
    public fechaReserva?: Date,
    public fechaVencimiento?: Date,
    public montoSenia?: number | null,
    public estado?: EstadoReserva,
    public observaciones?: string | null,
    public usuarioCreacion?: string | null,
    public createdDate?: Date | null,
    public lastModifiedDate?: Date | null,
    public inventario?: IInventario | null,
    public cliente?: ICliente | null,
    public ventaAsociada?: IVenta | null,
    public moneda?: IMoneda | null,
  ) {}
}
