import { type EstadoCuotaPlanAhorro } from './enumerations/estado-cuota-plan-ahorro.model';
import { type EstadoComprobante } from './estado-comprobante.model';

export interface ICuotaPlanAhorro {
  id?: number;
  contratoId?: number;
  numeroCuota?: number;
  fechaVencimiento?: Date;
  importe?: number;
  estado?: EstadoCuotaPlanAhorro;
  fechaPago?: Date | null;
  pagoId?: number | null;
  comprobanteId?: number | null;
  comprobantePlanAhorroId?: number | null;
  numeroComprobantePlanAhorro?: string | null;
  estadoComprobantePlanAhorro?: EstadoComprobante | null;
}
