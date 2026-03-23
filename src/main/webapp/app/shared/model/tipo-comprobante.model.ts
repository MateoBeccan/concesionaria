export interface ITipoComprobante {
  id?: number;
  codigo?: string;
  descripcion?: string | null;
}

export class TipoComprobante implements ITipoComprobante {
  constructor(
    public id?: number,
    public codigo?: string,
    public descripcion?: string | null,
  ) {}
}
