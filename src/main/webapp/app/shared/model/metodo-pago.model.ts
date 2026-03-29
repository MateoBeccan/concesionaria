export interface IMetodoPago {
  id?: number;
  codigo?: string;
  descripcion?: string | null;
  activo?: boolean;
  requiereReferencia?: boolean;
}

export class MetodoPago implements IMetodoPago {
  constructor(
    public id?: number,
    public codigo?: string,
    public descripcion?: string | null,
    public activo?: boolean,
    public requiereReferencia?: boolean,
  ) {
    this.activo = this.activo ?? false;
    this.requiereReferencia = this.requiereReferencia ?? false;
  }
}
