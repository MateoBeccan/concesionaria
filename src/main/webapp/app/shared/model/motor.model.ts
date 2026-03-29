import { type ICombustible } from '@/shared/model/combustible.model';
import { type ITipoCaja } from '@/shared/model/tipo-caja.model';
import { type ITraccion } from '@/shared/model/traccion.model';

export interface IMotor {
  id?: number;
  nombre?: string;
  cilindradaCc?: number;
  cilindroCant?: number;
  potenciaHp?: number;
  turbo?: boolean;
  combustible?: ICombustible | null;
  tipoCaja?: ITipoCaja | null;
  traccion?: ITraccion | null;
}

export class Motor implements IMotor {
  constructor(
    public id?: number,
    public nombre?: string,
    public cilindradaCc?: number,
    public cilindroCant?: number,
    public potenciaHp?: number,
    public turbo?: boolean,
    public combustible?: ICombustible | null,
    public tipoCaja?: ITipoCaja | null,
    public traccion?: ITraccion | null,
  ) {
    this.turbo = this.turbo ?? false;
  }
}
