import { type EstadoVehiculo } from '@/shared/model/enumerations/estado-vehiculo.model';
import { type EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import { type IMotor } from '@/shared/model/motor.model';
import { type IMoneda } from '@/shared/model/moneda.model';
import { type ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';
import { type IVersion } from '@/shared/model/version.model';

export interface IVehiculo {
  id?: number;
  estado?: keyof typeof EstadoVehiculo;
  estadoInventario?: keyof typeof EstadoInventario;
  fechaFabricacion?: Date;
  km?: number;
  patente?: string;
  vinChasis?: string | null;
  color?: string | null;
  observaciones?: string | null;
  precio?: number;
  moneda?: IMoneda | null;
  createdDate?: Date | null;
  createdBy?: string | null;
  lastModifiedDate?: Date | null;
  lastModifiedBy?: string | null;
  version?: IVersion | null;
  motor?: IMotor | null;
  tipoVehiculo?: ITipoVehiculo | null;
}

export class Vehiculo implements IVehiculo {
  constructor(
    public id?: number,
    public estado?: keyof typeof EstadoVehiculo,
    public estadoInventario?: keyof typeof EstadoInventario,
    public fechaFabricacion?: Date,
    public km?: number,
    public patente?: string,
    public vinChasis?: string | null,
    public color?: string | null,
    public observaciones?: string | null,
    public precio?: number,
    public moneda?: IMoneda | null,
    public createdDate?: Date | null,
    public createdBy?: string | null,
    public lastModifiedDate?: Date | null,
    public lastModifiedBy?: string | null,
    public version?: IVersion | null,
    public motor?: IMotor | null,
    public tipoVehiculo?: ITipoVehiculo | null,
  ) {}
}
