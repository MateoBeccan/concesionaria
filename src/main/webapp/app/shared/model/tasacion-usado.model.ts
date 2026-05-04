import { type ICliente } from '@/shared/model/cliente.model';
import { type EstadoTasacionUsado } from '@/shared/model/enumerations/estado-tasacion-usado.model';
import { type IInventario } from '@/shared/model/inventario.model';
import { type IMotor } from '@/shared/model/motor.model';
import { type ITipoVehiculo } from '@/shared/model/tipo-vehiculo.model';
import { type IUser } from '@/shared/model/user.model';
import { type IVersion } from '@/shared/model/version.model';

export interface ITasacionUsado {
  id?: number;
  fechaTasacion?: Date;
  montoTasacion?: number;
  estado?: EstadoTasacionUsado;
  marcaModeloUsado?: string | null;
  patenteUsado?: string | null;
  vinChasisUsado?: string | null;
  anioUsado?: number | null;
  kmUsado?: number | null;
  colorUsado?: string | null;
  usuarioTasador?: string | null;
  observaciones?: string | null;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;
  cliente?: ICliente | null;
  inventarioGenerado?: IInventario | null;
  version?: IVersion | null;
  motor?: IMotor | null;
  tipoVehiculo?: ITipoVehiculo | null;
  tasadorUser?: IUser | null;
  ventaAplicadaId?: number | null;
}

export class TasacionUsado implements ITasacionUsado {
  constructor(
    public id?: number,
    public fechaTasacion?: Date,
    public montoTasacion?: number,
    public estado?: EstadoTasacionUsado,
    public marcaModeloUsado?: string | null,
    public patenteUsado?: string | null,
    public vinChasisUsado?: string | null,
    public anioUsado?: number | null,
    public kmUsado?: number | null,
    public colorUsado?: string | null,
    public usuarioTasador?: string | null,
    public observaciones?: string | null,
    public createdDate?: Date | null,
    public lastModifiedDate?: Date | null,
    public cliente?: ICliente | null,
    public inventarioGenerado?: IInventario | null,
    public version?: IVersion | null,
    public motor?: IMotor | null,
    public tipoVehiculo?: ITipoVehiculo | null,
    public tasadorUser?: IUser | null,
    public ventaAplicadaId?: number | null,
  ) {}
}
