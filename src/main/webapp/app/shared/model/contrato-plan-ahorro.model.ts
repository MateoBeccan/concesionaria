import { type ICliente } from './cliente.model';
import { type IPlanAhorro } from './plan-ahorro.model';
import { type EstadoContratoPlanAhorro } from './enumerations/estado-contrato-plan-ahorro.model';

export interface IContratoPlanAhorro {
  id?: number;
  cliente?: ICliente;
  plan?: IPlanAhorro;
  fechaInicio?: Date;
  estado?: EstadoContratoPlanAhorro;
  numeroContrato?: string;
  cuotasTotales?: number;
  cuotasPagadas?: number;
  saldoPendiente?: number;
  observaciones?: string | null;
  usuarioRegistro?: string | null;
}

