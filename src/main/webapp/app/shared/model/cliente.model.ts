import { type ICondicionIva } from '@/shared/model/condicion-iva.model';
import { type ITipoDocumento } from '@/shared/model/tipo-documento.model';

export interface ICliente {
  id?: number;
  nombre?: string;
  apellido?: string;
  nroDocumento?: string;
  telefono?: string | null;
  email?: string;
  direccion?: string | null;
  ciudad?: string | null;
  provincia?: string | null;
  pais?: string | null;
  activo?: boolean;
  fechaAlta?: Date;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;
  condicionIva?: ICondicionIva | null;
  tipoDocumento?: ITipoDocumento | null;
}

export class Cliente implements ICliente {
  constructor(
    public id?: number,
    public nombre?: string,
    public apellido?: string,
    public nroDocumento?: string,
    public telefono?: string | null,
    public email?: string,
    public direccion?: string | null,
    public ciudad?: string | null,
    public provincia?: string | null,
    public pais?: string | null,
    public activo?: boolean,
    public fechaAlta?: Date,
    public createdDate?: Date | null,
    public lastModifiedDate?: Date | null,
    public condicionIva?: ICondicionIva | null,
    public tipoDocumento?: ITipoDocumento | null,
  ) {
    this.activo = this.activo ?? false;
  }
}
