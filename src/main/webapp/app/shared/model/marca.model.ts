export interface IMarca {
  id?: number;
  nombre?: string;
  paisOrigen?: string | null;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;
}

export class Marca implements IMarca {
  constructor(
    public id?: number,
    public nombre?: string,
    public paisOrigen?: string | null,
    public createdDate?: Date | null,
    public lastModifiedDate?: Date | null,
  ) {}
}
