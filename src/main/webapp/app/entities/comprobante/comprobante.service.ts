import axios from 'axios';

import { type IComprobante } from '@/shared/model/comprobante.model';
import buildPaginationQueryOpts from '@/shared/sort/sorts';

const baseApiUrl = 'api/comprobantes';

export default class ComprobanteService {
  find(id: number): Promise<IComprobante> {
    return new Promise<IComprobante>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${id}`)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  retrieve(paginationQuery?: any): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}?${buildPaginationQueryOpts(paginationQuery)}`)
        .then(res => {
          resolve(res);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  findByVentaId(ventaId: number): Promise<IComprobante[]> {
    return new Promise<IComprobante[]>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/by-venta/${ventaId}`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  async descargarPdf(id: number): Promise<void> {
    const response = await axios.get(`${baseApiUrl}/${id}/pdf`, { responseType: 'blob' });
    const blob = new Blob([response.data], { type: 'application/pdf' });
    const header = response.headers?.['content-disposition'] ?? '';
    const match = /filename="?([^"]+)"?/i.exec(header);
    const fileName = match?.[1] ?? `comprobante-${id}.pdf`;

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.target = '_blank';
    link.click();
    window.URL.revokeObjectURL(url);
  }

  emitir(ventaId: number, tipoComprobanteId: number): Promise<IComprobante> {
    return new Promise<IComprobante>((resolve, reject) => {
      axios
        .post(`${baseApiUrl}/emitir?ventaId=${ventaId}&tipoComprobanteId=${tipoComprobanteId}`)
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  anular(id: number, motivo: string): Promise<IComprobante> {
    return new Promise<IComprobante>((resolve, reject) => {
      axios
        .post(`${baseApiUrl}/${id}/anular`, { motivo })
        .then(res => resolve(res.data))
        .catch(err => reject(err));
    });
  }

  delete(id: number): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .delete(`${baseApiUrl}/${id}`)
        .then(res => {
          resolve(res);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  create(entity: IComprobante): Promise<IComprobante> {
    return new Promise<IComprobante>((resolve, reject) => {
      axios
        .post(baseApiUrl, entity)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  update(entity: IComprobante): Promise<IComprobante> {
    return new Promise<IComprobante>((resolve, reject) => {
      axios
        .put(`${baseApiUrl}/${entity.id}`, entity)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  partialUpdate(entity: IComprobante): Promise<IComprobante> {
    return new Promise<IComprobante>((resolve, reject) => {
      axios
        .patch(`${baseApiUrl}/${entity.id}`, entity)
        .then(res => {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }
}
