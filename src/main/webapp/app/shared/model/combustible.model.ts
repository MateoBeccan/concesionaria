export interface ICombustible {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
}

export class Combustible implements ICombustible {
  constructor(
    public id?: number,
    public nombre?: string,
    public descripcion?: string | null,
  ) {}
}
