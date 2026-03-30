import { type ICliente } from '@/shared/model/cliente.model';
import { type EstadoVenta } from '@/shared/model/estado-venta.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type IUser } from '@/shared/model/user.model';

export interface IVenta {
  id?: number;
  fecha?: Date;
  cotizacion?: number;
  importeNeto?: number;
  impuesto?: number;
  total?: number;
  porcentajeImpuesto?: number | null;
  totalPagado?: number | null;
  saldo?: number | null;
  observaciones?: string | null;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;
  cliente?: ICliente | null;
  estadoVenta?: EstadoVenta | null;
  moneda?: IMoneda | null;
  user?: IUser | null;
}

export class Venta implements IVenta {
  constructor(
    public id?: number,
    public fecha?: Date,
    public cotizacion?: number,
    public importeNeto?: number,
    public impuesto?: number,
    public total?: number,
    public porcentajeImpuesto?: number | null,
    public totalPagado?: number | null,
    public saldo?: number | null,
    public observaciones?: string | null,
    public createdDate?: Date | null,
    public lastModifiedDate?: Date | null,
    public cliente?: ICliente | null,
    public estado?: EstadoVenta | null,
    public moneda?: IMoneda | null,
    public user?: IUser | null,
  ) {}
}
