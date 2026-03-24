export interface IVersion {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
  anioInicio?: number | null;
  anioFin?: number | null;
}

export class Version implements IVersion {
  constructor(
    public id?: number,
    public nombre?: string,
    public descripcion?: string | null,
    public anioInicio?: number | null,
    public anioFin?: number | null,
  ) {}
}
