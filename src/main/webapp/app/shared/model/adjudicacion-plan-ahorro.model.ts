import type { ICliente } from './cliente.model';
import type { IInventario } from './inventario.model';
import type { IVehiculo } from './vehiculo.model';
import type { IVenta } from './venta.model';
import type { EstadoAdjudicacionPlanAhorro } from './enumerations/estado-adjudicacion-plan-ahorro.model';

export interface IAdjudicacionPlanAhorro {
  id?: number;
  contratoPlanAhorroId?: number;
  numeroContrato?: string;
  planNombre?: string | null;
  cliente?: ICliente;
  fechaAdjudicacion?: Date;
  estado?: EstadoAdjudicacionPlanAhorro;
  inventario?: IInventario | null;
  vehiculo?: IVehiculo | null;
  venta?: IVenta | null;
  montoReconocidoCuotas?: number;
  diferenciaAPagar?: number;
  observaciones?: string | null;
  usuarioAdjudicacion?: string | null;
}
