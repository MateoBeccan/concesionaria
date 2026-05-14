export interface IEntregaChecklistItem {
  id?: number;
  codigo?: string;
  descripcion?: string;
  obligatorio?: boolean;
  completado?: boolean;
  observaciones?: string | null;
}

