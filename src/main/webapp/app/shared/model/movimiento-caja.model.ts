import { type IEntidadFinanciera } from '@/shared/model/entidad-financiera.model';
import { type IMetodoPago } from '@/shared/model/metodo-pago.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type EstadoPago } from '@/shared/model/estado-pago.model';
import { type TipoMovimientoCaja } from '@/shared/model/tipo-movimiento-caja.model';

export interface IMovimientoCaja {
  id?: number;
  fecha?: Date;
  usuario?: string;
  tipoMovimiento?: TipoMovimientoCaja;
  estado?: EstadoPago;
  montoOriginal?: number;
  cotizacionUsada?: number | null;
  montoAplicadoArs?: number;
  referencia?: string | null;
  numeroOperacion?: string | null;
  pagoId?: number | null;
  ventaId?: number | null;
  reservaId?: number | null;
  metodoPago?: IMetodoPago | null;
  moneda?: IMoneda | null;
  entidadFinanciera?: IEntidadFinanciera | null;
}

export interface IResumenMetodoCaja {
  metodoPagoId?: number;
  metodoPagoCodigo?: string;
  metodoPagoDescripcion?: string;
  totalIngresos?: number;
  totalReversos?: number;
  neto?: number;
}

export interface IResumenDiarioCaja {
  fecha?: string;
  totalIngresos?: number;
  totalReversos?: number;
  neto?: number;
  porMetodo?: IResumenMetodoCaja[];
}
