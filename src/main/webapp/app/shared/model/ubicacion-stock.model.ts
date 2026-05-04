import { type TipoUbicacionStock } from '@/shared/model/enumerations/tipo-ubicacion-stock.model';

export interface IUbicacionStock {
  id?: number;
  codigo?: string;
  nombre?: string;
  tipoUbicacion?: TipoUbicacionStock;
  direccion?: string | null;
  descripcion?: string | null;
  activa?: boolean;
}

export class UbicacionStock implements IUbicacionStock {
  constructor(
    public id?: number,
    public codigo?: string,
    public nombre?: string,
    public tipoUbicacion?: TipoUbicacionStock,
    public direccion?: string | null,
    public descripcion?: string | null,
    public activa?: boolean,
  ) {}
}
