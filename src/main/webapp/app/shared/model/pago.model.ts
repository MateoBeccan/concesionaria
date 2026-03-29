import { type IMetodoPago } from '@/shared/model/metodo-pago.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type IVenta } from '@/shared/model/venta.model';

export interface IPago {
  id?: number;
  monto?: number;
  fecha?: Date;
  referencia?: string | null;
  createdDate?: Date | null;
  venta?: IVenta | null;
  metodoPago?: IMetodoPago | null;
  moneda?: IMoneda | null;
}

export class Pago implements IPago {
  constructor(
    public id?: number,
    public monto?: number,
    public fecha?: Date,
    public referencia?: string | null,
    public createdDate?: Date | null,
    public venta?: IVenta | null,
    public metodoPago?: IMetodoPago | null,
    public moneda?: IMoneda | null,
  ) {}
}
