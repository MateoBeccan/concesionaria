export interface ITipoVehiculo {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
}

export class TipoVehiculo implements ITipoVehiculo {
  constructor(
    public id?: number,
    public nombre?: string,
    public descripcion?: string | null,
  ) {}
}
