import { type EstadoVehiculo } from '@/shared/model/enumerations/estado-vehiculo.model';
import { type CondicionVehiculo } from '@/shared/model/enumerations/condicion-vehiculo.model';
import { type IMotor } from '@/shared/model/motor.model';
import { type ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';
import { type IVersion } from '@/shared/model/version.model';

export interface IVehiculo {
  id?: number;

  // Estado físico: NUEVO / USADO
  estado?: keyof typeof EstadoVehiculo;

  // Condición comercial: EN_VENTA / RESERVADO / VENDIDO
  condicion?: keyof typeof CondicionVehiculo;

  fechaFabricacion?: Date;
  km?: number;
  patente?: string;
  precio?: number;

  createdDate?: Date | null;
  lastModifiedDate?: Date | null;

  version?: IVersion | null;
  motor?: IMotor | null;
  tipoVehiculo?: ITipoVehiculo | null;
}

export class Vehiculo implements IVehiculo {
  constructor(
    public id?: number,
    public estado?: keyof typeof EstadoVehiculo,
    public condicion?: keyof typeof CondicionVehiculo,
    public fechaFabricacion?: Date,
    public km?: number,
    public patente?: string,
    public precio?: number,
    public createdDate?: Date | null,
    public lastModifiedDate?: Date | null,
    public version?: IVersion | null,
    public motor?: IMotor | null,
    public tipoVehiculo?: ITipoVehiculo | null,
  ) {}
}
