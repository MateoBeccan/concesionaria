import axios from 'axios';

const baseApiUrl = 'api/comprobantes-plan-ahorro';

export default class ComprobantePlanAhorroService {
  public async anular(id: number, motivo: string): Promise<any> {
    const res = await axios.post(`${baseApiUrl}/${id}/anular`, { motivo });
    return res.data;
  }

  public descargarPdf(id: number): void {
    window.open(`/api/comprobantes-plan-ahorro/${id}/pdf`, '_blank');
  }
}

