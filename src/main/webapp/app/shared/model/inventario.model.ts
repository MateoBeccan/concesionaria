import { type EstadoOperativoDocumental } from '@/shared/model/enumerations/estado-operativo-documental.model';
import { type EstadoInventario } from '@/shared/model/enumerations/estado-inventario.model';
import { type OrigenVehiculo } from '@/shared/model/enumerations/origen-vehiculo.model';
import { type TipoTenenciaInventario } from '@/shared/model/enumerations/tipo-tenencia-inventario.model';
import { type IUbicacionStock } from '@/shared/model/ubicacion-stock.model';
import { type IVehiculo } from '@/shared/model/vehiculo.model';
export interface IInventario {
  id?: number;
  fechaIngreso?: Date;
  codigoInternoStock?: string | null;
  costoAdquisicion?: number | null;
  fechaEgreso?: Date | null;
  tipoOrigenIngreso?: string | null;
  origenVehiculo?: OrigenVehiculo | null;
  tipoTenencia?: TipoTenenciaInventario | null;
  estadoOperativoDocumental?: EstadoOperativoDocumental | null;
  proveedorReferencia?: string | null;
  numeroInternoStock?: string | null;
  ubicacionStock?: IUbicacionStock | null;
  estadoInventario?: keyof typeof EstadoInventario;
  observaciones?: string | null;
  createdDate?: Date | null;
  createdBy?: string | null;
  lastModifiedDate?: Date | null;
  lastModifiedBy?: string | null;
  vehiculo?: IVehiculo | null;
}

export class Inventario implements IInventario {
  constructor(
    public id?: number,
    public fechaIngreso?: Date,
    public codigoInternoStock?: string | null,
    public costoAdquisicion?: number | null,
    public fechaEgreso?: Date | null,
    public tipoOrigenIngreso?: string | null,
    public origenVehiculo?: OrigenVehiculo | null,
    public tipoTenencia?: TipoTenenciaInventario | null,
    public estadoOperativoDocumental?: EstadoOperativoDocumental | null,
    public proveedorReferencia?: string | null,
    public numeroInternoStock?: string | null,
    public ubicacionStock?: IUbicacionStock | null,
    public estadoInventario?: keyof typeof EstadoInventario,
    public observaciones?: string | null,
    public createdDate?: Date | null,
    public createdBy?: string | null,
    public lastModifiedDate?: Date | null,
    public lastModifiedBy?: string | null,
    public vehiculo?: IVehiculo | null,
  ) {}
}
