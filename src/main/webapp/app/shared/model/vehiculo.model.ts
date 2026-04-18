import { type EstadoVehiculo } from '@/shared/model/enumerations/estado-vehiculo.model';
import { type CondicionVehiculo } from '@/shared/model/enumerations/condicion-vehiculo.model';
import { type EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import { type IMotor } from '@/shared/model/motor.model';
import { type ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';
import { type IVersion } from '@/shared/model/version.model';

export interface IVehiculo {
  id?: number;

  // Estado físico: NUEVO / USADO
  estado?: keyof typeof EstadoVehiculo;

  // Condición comercial: EN_VENTA / RESERVADO / VENDIDO
  condicion?: keyof typeof CondicionVehiculo;
  estadoInventario?: keyof typeof EstadoInventario;

  fechaFabricacion?: Date;
  km?: number;
  patente?: string;
  precio?: number;

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
    public condicion?: keyof typeof CondicionVehiculo,
    public estadoInventario?: keyof typeof EstadoInventario,
    public fechaFabricacion?: Date,
    public km?: number,
    public patente?: string,
    public precio?: number,
    public createdDate?: Date | null,
    public createdBy?: string | null,
    public lastModifiedDate?: Date | null,
    public lastModifiedBy?: string | null,
    public version?: IVersion | null,
    public motor?: IMotor | null,
    public tipoVehiculo?: ITipoVehiculo | null,
  ) {}
}
