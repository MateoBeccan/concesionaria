export interface ITipoDocumento {
  id?: number;
  codigo?: string;
  descripcion?: string | null;
}

export class TipoDocumento implements ITipoDocumento {
  constructor(
    public id?: number,
    public codigo?: string,
    public descripcion?: string | null,
  ) {}
}
