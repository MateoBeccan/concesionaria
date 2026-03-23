import { type IModelo } from '@/shared/model/modelo.model';
import { type IMotor } from '@/shared/model/motor.model';

export interface IVersion {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
  anioInicio?: number | null;
  anioFin?: number | null;
  modeloses?: IModelo[] | null;
  motoreses?: IMotor[] | null;
}

export class Version implements IVersion {
  constructor(
    public id?: number,
    public nombre?: string,
    public descripcion?: string | null,
    public anioInicio?: number | null,
    public anioFin?: number | null,
    public modeloses?: IModelo[] | null,
    public motoreses?: IMotor[] | null,
  ) {}
}
