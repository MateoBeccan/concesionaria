export interface ITipoCaja {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
}

export class TipoCaja implements ITipoCaja {
  constructor(
    public id?: number,
    public nombre?: string,
    public descripcion?: string | null,
  ) {}
}
