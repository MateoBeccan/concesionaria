export interface ICondicionIva {
  id?: number;
  codigo?: string;
  descripcion?: string | null;
}

export class CondicionIva implements ICondicionIva {
  constructor(
    public id?: number,
    public codigo?: string,
    public descripcion?: string | null,
  ) {}
}
