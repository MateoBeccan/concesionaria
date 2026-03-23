export interface IEstadoVenta {
  id?: number;
  codigo?: string;
  descripcion?: string | null;
}

export class EstadoVenta implements IEstadoVenta {
  constructor(
    public id?: number,
    public codigo?: string,
    public descripcion?: string | null,
  ) {}
}
