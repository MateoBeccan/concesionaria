export interface IEntidadFinanciera {
  id?: number;
  codigo?: string;
  nombre?: string;
  descripcion?: string | null;
  activa?: boolean;
}

export class EntidadFinanciera implements IEntidadFinanciera {
  constructor(
    public id?: number,
    public codigo?: string,
    public nombre?: string,
    public descripcion?: string | null,
    public activa?: boolean,
  ) {
    this.activa = this.activa ?? true;
  }
}
