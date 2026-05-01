import { type IMetodoPago } from '@/shared/model/metodo-pago.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type IReserva } from '@/shared/model/reserva.model';
import { type ITasacionUsado } from '@/shared/model/tasacion-usado.model';
import { type IVenta } from '@/shared/model/venta.model';
import { type EstadoPago } from '@/shared/model/estado-pago.model';
import { type TipoMovimientoPago } from '@/shared/model/tipo-movimiento-pago.model';

export interface IPago {
  id?: number;
  monto?: number;
  fecha?: Date;
  referencia?: string | null;
  tipoMovimiento?: TipoMovimientoPago | null;
  cotizacionUsada?: number | null;
  montoAplicadoVenta?: number | null;
  fechaCotizacionUsada?: Date | null;
  cotizacionId?: number | null;
  usuarioRegistro?: string | null;
  observaciones?: string | null;
  comprobanteExterno?: string | null;
  bancoEntidad?: string | null;
  numeroOperacion?: string | null;
  createdDate?: Date | null;
  venta?: IVenta | null;
  reserva?: IReserva | null;
  metodoPago?: IMetodoPago | null;
  moneda?: IMoneda | null;
  tasacionUsadoId?: number | null;
  tasacionUsado?: ITasacionUsado | null;
  estado?: EstadoPago;
}

export class Pago implements IPago {
  constructor(
    public id?: number,
    public monto?: number,
    public fecha?: Date,
    public referencia?: string | null,
    public tipoMovimiento?: TipoMovimientoPago | null,
    public cotizacionUsada?: number | null,
    public montoAplicadoVenta?: number | null,
    public fechaCotizacionUsada?: Date | null,
    public cotizacionId?: number | null,
    public usuarioRegistro?: string | null,
    public observaciones?: string | null,
    public comprobanteExterno?: string | null,
    public bancoEntidad?: string | null,
    public numeroOperacion?: string | null,
    public createdDate?: Date | null,
    public venta?: IVenta | null,
    public reserva?: IReserva | null,
    public metodoPago?: IMetodoPago | null,
    public moneda?: IMoneda | null,
    public tasacionUsadoId?: number | null,
    public tasacionUsado?: ITasacionUsado | null,
    public estado?: EstadoPago,
  ) {}
}
