import { type IMoneda } from '@/shared/model/moneda.model';

export interface ICotizacion {
  id?: number;
  fecha?: Date;
  valorCompra?: number;
  valorVenta?: number;
  activo?: boolean;
  createdDate?: Date | null;
  createdBy?: string | null;
  lastModifiedDate?: Date | null;
  lastModifiedBy?: string | null;
  moneda?: IMoneda | null;
}

export class Cotizacion implements ICotizacion {
  constructor(
    public id?: number,
    public fecha?: Date,
    public valorCompra?: number,
    public valorVenta?: number,
    public activo?: boolean,
    public createdDate?: Date | null,
    public createdBy?: string | null,
    public lastModifiedDate?: Date | null,
    public lastModifiedBy?: string | null,
    public moneda?: IMoneda | null,
  ) {
    this.activo = this.activo ?? false;
  }
}
