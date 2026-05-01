import { type ICliente } from '@/shared/model/cliente.model';
import { type EstadoVenta } from '@/shared/model/estado-venta.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type IReserva } from '@/shared/model/reserva.model';
import { type ITasacionUsado } from '@/shared/model/tasacion-usado.model';
import { type IUser } from '@/shared/model/user.model';
import { type IVehiculo } from '@/shared/model/vehiculo.model';

export interface IVenta {
  id?: number;
  fecha?: Date;
  cotizacion?: number;
  fechaCotizacionUsada?: Date | null;
  cotizacionId?: number | null;
  importeNeto?: number;
  impuesto?: number;
  total?: number;
  porcentajeImpuesto?: number | null;
  totalPagado?: number | null;
  saldo?: number | null;
  observaciones?: string | null;
  createdDate?: Date | null;
  createdBy?: string | null;
  lastModifiedDate?: Date | null;
  lastModifiedBy?: string | null;
  cliente?: ICliente | null;
  estado?: EstadoVenta | null;

  moneda?: IMoneda | null;
  user?: IUser | null;
  reserva?: IReserva | null;
  vehiculo?: IVehiculo | null;
  tasacionUsado?: ITasacionUsado | null;
  precioBaseVehiculo?: number | null;
  monedaVehiculo?: IMoneda | null;
  importeConvertido?: number | null;
}
export class Venta implements IVenta {
  constructor(
    public id?: number,
    public fecha?: Date,
    public cotizacion?: number,
    public fechaCotizacionUsada?: Date | null,
    public cotizacionId?: number | null,
    public importeNeto?: number,
    public impuesto?: number,
    public total?: number,
    public porcentajeImpuesto?: number | null,
    public totalPagado?: number | null,
    public saldo?: number | null,
    public observaciones?: string | null,
    public createdDate?: Date | null,
    public createdBy?: string | null,
    public lastModifiedDate?: Date | null,
    public lastModifiedBy?: string | null,
    public cliente?: ICliente | null,
    public estado?: EstadoVenta | null,
    public moneda?: IMoneda | null,
    public user?: IUser | null,
    public reserva?: IReserva | null,
    public vehiculo?: IVehiculo | null,
    public tasacionUsado?: ITasacionUsado | null,
    public precioBaseVehiculo?: number | null,
    public monedaVehiculo?: IMoneda | null,
    public importeConvertido?: number | null,
  ) {}
}
