import { type IMoneda } from '@/shared/model/moneda.model';
import { type ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import { type IVenta } from '@/shared/model/venta.model';

export interface IComprobante {
  id?: number;
  numeroComprobante?: string;
  fechaEmision?: Date;
  importeNeto?: number;
  impuesto?: number;
  total?: number;
  createdDate?: Date | null;
  venta?: IVenta | null;
  tipoComprobante?: ITipoComprobante | null;
  moneda?: IMoneda | null;
}

export class Comprobante implements IComprobante {
  constructor(
    public id?: number,
    public numeroComprobante?: string,
    public fechaEmision?: Date,
    public importeNeto?: number,
    public impuesto?: number,
    public total?: number,
    public createdDate?: Date | null,
    public venta?: IVenta | null,
    public tipoComprobante?: ITipoComprobante | null,
    public moneda?: IMoneda | null,
  ) {}
}
