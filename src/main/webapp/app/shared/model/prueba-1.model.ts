export interface IPrueba1 {
  id?: number;
}

export class Prueba1 implements IPrueba1 {
  constructor(public id?: number) {}
}
