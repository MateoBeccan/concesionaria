export interface IMoneda {
  id?: number;
  codigo?: string;
  descripcion?: string | null;
  simbolo?: string | null;
  activo?: boolean;
}

export class Moneda implements IMoneda {
  constructor(
    public id?: number,
    public codigo?: string,
    public descripcion?: string | null,
    public simbolo?: string | null,
    public activo?: boolean,
  ) {
    this.activo = this.activo ?? false;
  }
}
