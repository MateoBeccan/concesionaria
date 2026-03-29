export interface ICarroceria {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
}

export class Carroceria implements ICarroceria {
  constructor(
    public id?: number,
    public nombre?: string,
    public descripcion?: string | null,
  ) {}
}
