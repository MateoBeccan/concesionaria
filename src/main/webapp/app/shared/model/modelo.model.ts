import { type ICarroceria } from '@/shared/model/carroceria.model';
import { type IMarca } from '@/shared/model/marca.model';

export interface IModelo {
  id?: number;
  nombre?: string;
  anioLanzamiento?: number;
  createdDate?: Date | null;
  lastModifiedDate?: Date | null;
  marca?: IMarca | null;
  carroceria?: ICarroceria | null;
}

export class Modelo implements IModelo {
  constructor(
    public id?: number,
    public nombre?: string,
    public anioLanzamiento?: number,
    public createdDate?: Date | null,
    public lastModifiedDate?: Date | null,
    public marca?: IMarca | null,
    public carroceria?: ICarroceria | null,
  ) {}
}
