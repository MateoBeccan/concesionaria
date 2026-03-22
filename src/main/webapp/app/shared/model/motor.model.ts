import { type IVersion } from '@/shared/model/version.model';

export interface IMotor {
  id?: number;
  nombre?: string;
  cilindradaCc?: number | null;
  cilindroCant?: number | null;
  potenciaHp?: number | null;
  turbo?: boolean | null;
  versioneses?: IVersion[] | null;
}

export class Motor implements IMotor {
  constructor(
    public id?: number,
    public nombre?: string,
    public cilindradaCc?: number | null,
    public cilindroCant?: number | null,
    public potenciaHp?: number | null,
    public turbo?: boolean | null,
    public versioneses?: IVersion[] | null,
  ) {
    this.turbo = this.turbo ?? false;
  }
}
