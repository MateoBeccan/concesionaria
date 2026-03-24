import { type IMarca } from '@/shared/model/marca.model';

export interface IModelo {
  id?: number;
  nombre?: string;
  anioLanzamiento?: number | null;
  carroceria?: string | null;
  marca?: IMarca | null;
}

export class Modelo implements IModelo {
  constructor(
    public id?: number,
    public nombre?: string,
    public anioLanzamiento?: number | null,
    public carroceria?: string | null,
    public marca?: IMarca | null,
  ) {}
}
