import { type ICondicionIva } from '@/shared/model/condicion-iva.model';

export interface ICliente {
  id?: number;
  nombre?: string;
  apellido?: string;
  nroDocumento?: string;
  telefono?: string | null;
  email?: string | null;
  direccion?: string | null;
  ciudad?: string | null;
  provincia?: string | null;
  pais?: string | null;
  activo?: boolean;
  fechaAlta?: Date;
  condicionIva?: ICondicionIva | null;
}

export class Cliente implements ICliente {
  constructor(
    public id?: number,
    public nombre?: string,
    public apellido?: string,
    public nroDocumento?: string,
    public telefono?: string | null,
    public email?: string | null,
    public direccion?: string | null,
    public ciudad?: string | null,
    public provincia?: string | null,
    public pais?: string | null,
    public activo?: boolean,
    public fechaAlta?: Date,
    public condicionIva?: ICondicionIva | null,
  ) {
    this.activo = this.activo ?? false;
  }
}
