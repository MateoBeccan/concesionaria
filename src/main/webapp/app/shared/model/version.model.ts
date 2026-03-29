import { type IModelo } from '@/shared/model/modelo.model';

export interface IVersion {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
  anioInicio?: number;
  anioFin?: number | null;
  modelo?: IModelo | null;
}

export class Version implements IVersion {
  constructor(
    public id?: number,
    public nombre?: string,
    public descripcion?: string | null,
    public anioInicio?: number,
    public anioFin?: number | null,
    public modelo?: IModelo | null,
  ) {}
}
