import axios from 'axios';

const baseApiUrl = 'api/comprobantes-plan-ahorro';

export default class ComprobantePlanAhorroService {
  public async anular(id: number, motivo: string): Promise<any> {
    const res = await axios.post(`${baseApiUrl}/${id}/anular`, { motivo });
    return res.data;
  }

  public async descargarPdf(id: number): Promise<void> {
    const response = await axios.get(`${baseApiUrl}/${id}/pdf`, {
      responseType: 'blob',
    });

    const disposition = response.headers['content-disposition'] as string | undefined;
    const match = disposition?.match(/filename="?([^"]+)"?/i);
    const fileName = match?.[1] ?? `comprobante-plan-ahorro-${id}.pdf`;

    const blobUrl = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(blobUrl);
  }
}
