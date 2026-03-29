import { type IModelo } from '@/shared/model/modelo.model';
import { type IMotor } from '@/shared/model/motor.model';
import { type IVersion } from '@/shared/model/version.model';

export interface IConfiguracionAuto {
  id?: number;
  modelo?: IModelo | null;
  version?: IVersion | null;
  motor?: IMotor | null;
}

export class ConfiguracionAuto implements IConfiguracionAuto {
  constructor(
    public id?: number,
    public modelo?: IModelo | null,
    public version?: IVersion | null,
    public motor?: IMotor | null,
  ) {}
}
