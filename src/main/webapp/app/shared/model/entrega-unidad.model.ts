import type { ICliente } from './cliente.model';
import type { IInventario } from './inventario.model';
import type { IVehiculo } from './vehiculo.model';
import type { IVenta } from './venta.model';
import type { IEntregaChecklistItem } from './entrega-checklist-item.model';
import type { EstadoEntregaUnidad } from './enumerations/estado-entrega-unidad.model';

export interface IEntregaUnidad {
  id?: number;
  venta?: IVenta;
  cliente?: ICliente;
  vehiculo?: IVehiculo;
  inventario?: IInventario;
  fechaProgramada?: Date;
  fechaEntrega?: Date | null;
  estado?: EstadoEntregaUnidad;
  usuarioProgramacion?: string | null;
  usuarioEntrega?: string | null;
  kilometrajeEntrega?: number | null;
  nivelCombustible?: string | null;
  observaciones?: string | null;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;
  checklist?: IEntregaChecklistItem[];
}

