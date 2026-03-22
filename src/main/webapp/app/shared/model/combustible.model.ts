import { type IMotor } from '@/shared/model/motor.model';

export interface ICombustible {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
  motor?: IMotor | null;
}

export class Combustible implements ICombustible {
  constructor(
    public id?: number,
    public nombre?: string,
    public descripcion?: string | null,
    public motor?: IMotor | null,
  ) {}
}
