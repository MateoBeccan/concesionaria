import { type CondicionAuto } from '@/shared/model/enumerations/condicion-auto.model';
import { type EstadoAuto } from '@/shared/model/enumerations/estado-auto.model';
import { type IMarca } from '@/shared/model/marca.model';
import { type IModelo } from '@/shared/model/modelo.model';
import { type IMotor } from '@/shared/model/motor.model';
import { type IVersion } from '@/shared/model/version.model';

export interface IAuto {
  id?: number;
  estado?: keyof typeof EstadoAuto;
  condicion?: keyof typeof CondicionAuto;
  fechaFabricacion?: Date | null;
  km?: number | null;
  patente?: string | null;
  precio?: number | null;
  marca?: IMarca | null;
  modelo?: IModelo | null;
  version?: IVersion | null;
  motor?: IMotor | null;
}

export class Auto implements IAuto {
  constructor(
    public id?: number,
    public estado?: keyof typeof EstadoAuto,
    public condicion?: keyof typeof CondicionAuto,
    public fechaFabricacion?: Date | null,
    public km?: number | null,
    public patente?: string | null,
    public precio?: number | null,
    public marca?: IMarca | null,
    public modelo?: IModelo | null,
    public version?: IVersion | null,
    public motor?: IMotor | null,
  ) {}
}
