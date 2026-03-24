import { type ICombustible } from '@/shared/model/combustible.model';

export interface IMotor {
  id?: number;
  nombre?: string;
  cilindradaCc?: number | null;
  cilindroCant?: number | null;
  potenciaHp?: number | null;
  turbo?: boolean | null;
  combustible?: ICombustible | null;
}

export class Motor implements IMotor {
  constructor(
    public id?: number,
    public nombre?: string,
    public cilindradaCc?: number | null,
    public cilindroCant?: number | null,
    public potenciaHp?: number | null,
    public turbo?: boolean | null,
    public combustible?: ICombustible | null,
  ) {
    this.turbo = this.turbo ?? false;
  }
}
