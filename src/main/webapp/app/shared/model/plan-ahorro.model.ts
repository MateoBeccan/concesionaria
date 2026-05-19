import { type IMoneda } from './moneda.model';
import { type IVersion } from './version.model';
import { type IReglaAdjudicacionPlan } from './regla-adjudicacion-plan.model';
import { type EstadoPlanAhorro } from './enumerations/estado-plan-ahorro.model';

export interface IPlanAhorro {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
  versionObjetivo?: IVersion | null;
  cantidadCuotas?: number;
  valorMovil?: number;
  moneda?: IMoneda;
  estado?: EstadoPlanAhorro;
  reglaAdjudicacion?: IReglaAdjudicacionPlan | null;
}
