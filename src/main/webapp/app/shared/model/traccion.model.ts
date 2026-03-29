export interface ITraccion {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
}

export class Traccion implements ITraccion {
  constructor(
    public id?: number,
    public nombre?: string,
    public descripcion?: string | null,
  ) {}
}
