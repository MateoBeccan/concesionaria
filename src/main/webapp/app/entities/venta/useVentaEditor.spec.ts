import { beforeEach, describe, expect, it } from 'vitest';

import axios from 'axios';
import sinon from 'sinon';

import { EstadoPago } from '@/shared/model/estado-pago.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';
import type { IMetodoPago } from '@/shared/model/metodo-pago.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import type { IVenta } from '@/shared/model/venta.model';

import { useVentaEditor } from './useVentaEditor';

const axiosStub = {
  post: sinon.stub(axios, 'post'),
};

describe('useVentaEditor', () => {
  beforeEach(() => {
    axiosStub.post.reset();
  });

  it('mantiene cableada la confirmacion del editor al workflow transaccional', async () => {
    const editor = crearEditorListoParaConfirmar();
    const ventaFinal: IVenta = { id: 10, totalPagado: 100, saldo: 0, estado: EstadoVenta.PAGADA };
    axiosStub.post.resolves({ data: { venta: ventaFinal, pagos: [], comprobante: null } });

    const result = await editor.confirmar();

    expect(axiosStub.post.calledOnce).toBeTruthy();
    const [url, payload] = axiosStub.post.firstCall.args;
    expect(url).toBe('api/ventas/confirmar');
    expect(payload.pagos).toHaveLength(1);
    expect(editor.venta.value).toMatchObject(ventaFinal);
    expect(result.venta).toBe(ventaFinal);
  });
});

function crearEditorListoParaConfirmar() {
  const editor = useVentaEditor();
  const moneda: IMoneda = { id: 1, codigo: 'ARS' };
  const metodoPago: IMetodoPago = { id: 1, codigo: 'EFECTIVO' };
  const vehiculo: IVehiculo = {
    id: 1,
    precio: 100,
    moneda,
    estadoInventario: 'DISPONIBLE' as any,
  };

  editor.venta.value = {
    fecha: new Date(),
    cliente: { id: 1 },
    moneda,
    cotizacion: 1,
    porcentajeImpuesto: 0,
    importeNeto: 100,
    impuesto: 0,
    total: 100,
    totalPagado: 100,
    saldo: 0,
  };
  editor.detalles.value = [{ _key: 'vehiculo-1', vehiculo, precioUnitario: 100, subtotal: 100 }];
  editor.pagos.value = [
    {
      _key: 'tmp-pago-1',
      monto: 100,
      montoAplicadoMonedaVenta: 100,
      fecha: new Date(),
      referencia: '',
      metodoPago,
      moneda,
      cotizacionUsada: 1,
      estado: EstadoPago.REGISTRADO,
      guardado: false,
    },
  ];
  return editor;
}
