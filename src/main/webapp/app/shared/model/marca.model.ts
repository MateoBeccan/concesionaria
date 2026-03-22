export interface IMarca {
  id?: number;
  nombre?: string;
  paisOrigen?: string | null;
}

export class Marca implements IMarca {
  constructor(
    public id?: number,
    public nombre?: string,
    public paisOrigen?: string | null,
  ) {}
}
