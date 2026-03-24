import { type IConfiguracionAuto } from '@/shared/model/configuracion-auto.model';
import { type CondicionAuto } from '@/shared/model/enumerations/condicion-auto.model';
import { type EstadoAuto } from '@/shared/model/enumerations/estado-auto.model';
export interface IAuto {
  id?: number;
  estado?: keyof typeof EstadoAuto;
  condicion?: keyof typeof CondicionAuto;
  fechaFabricacion?: Date | null;
  km?: number | null;
  patente?: string | null;
  precio?: number | null;
  configuracion?: IConfiguracionAuto | null;
}

export class Auto implements IAuto {
  constructor(
    public id?: number,
    public estado?: keyof typeof EstadoAuto,
    public condicion?: keyof typeof CondicionAuto,
    public fechaFabricacion?: Date | null,
    public km?: number | null,
    public patente?: string | null,
    public precio?: number | null,
    public configuracion?: IConfiguracionAuto | null,
  ) {}
}
