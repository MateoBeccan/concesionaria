import type { TipoReglaAdjudicacionPlan } from './enumerations/tipo-regla-adjudicacion-plan.model';

export interface IElegibilidadAdjudicacion {
  apto?: boolean;
  mensaje?: string;
  tipoRegla?: TipoReglaAdjudicacionPlan;
  cuotasPagadas?: number;
  minimoCuotas?: number | null;
  porcentajeIntegrado?: number;
  minimoPorcentaje?: number | null;
  permiteMora?: boolean;
  nombreRegla?: string;
}
