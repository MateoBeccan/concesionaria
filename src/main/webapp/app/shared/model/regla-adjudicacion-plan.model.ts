import type { TipoReglaAdjudicacionPlan } from './enumerations/tipo-regla-adjudicacion-plan.model';

export interface IReglaAdjudicacionPlan {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
  tipoRegla?: TipoReglaAdjudicacionPlan;
  minimoCuotas?: number | null;
  minimoPorcentaje?: number | null;
  permiteMora?: boolean;
  requiereContratoActivo?: boolean;
  activo?: boolean;
}
