import { type IMoneda } from '@/shared/model/moneda.model';

export interface ICotizacion {
  id?: number;
  fecha?: Date;
  valorCompra?: number;
  valorVenta?: number;
  activo?: boolean;
  moneda?: IMoneda | null;
}

export class Cotizacion implements ICotizacion {
  constructor(
    public id?: number,
    public fecha?: Date,
    public valorCompra?: number,
    public valorVenta?: number,
    public activo?: boolean,
    public moneda?: IMoneda | null,
  ) {
    this.activo = this.activo ?? false;
  }
}
