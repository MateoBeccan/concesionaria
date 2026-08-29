import { describe, expect, it } from 'vitest';
import { computed, ref } from 'vue';
import sinon from 'sinon';

import { EstadoComprobante } from '@/shared/model/estado-comprobante.model';
import { EstadoPago } from '@/shared/model/estado-pago.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';
import type { IComprobante } from '@/shared/model/comprobante.model';
import type { IMetodoPago } from '@/shared/model/metodo-pago.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { IPago } from '@/shared/model/pago.model';
import type { ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import type { IVenta } from '@/shared/model/venta.model';

import type { DetalleLocal, PagoLocal } from './useVentaEditor';
import { useVentaConfirmacion } from './useVentaConfirmacion';

describe('useVentaConfirmacion', () => {
  it('confirma con el workflow y usa la venta devuelta por backend', async () => {
    const context = crearContextoConfirmacion();
    context.state.pagos.value.push(pagoLocal({ id: 77, guardado: true, monto: 15 }));
    context.state.pagos.value.push(pagoLocal({ guardado: false, monto: 5, estado: EstadoPago.ANULADO }));
    context.state.venta.value.importeNeto = 115;
    context.state.venta.value.total = 115;
    context.state.venta.value.totalPagado = 115;
    const ventaFinal: IVenta = { id: 10, totalPagado: 115, saldo: 0, estado: EstadoVenta.PAGADA };
    const pagoFinal: IPago = { id: 20, monto: 100, montoAplicadoVenta: 100, fecha: new Date(), estado: EstadoPago.REGISTRADO };
    const comprobante: IComprobante = { id: 30, numeroComprobante: 'FAC-0001', estado: EstadoComprobante.EMITIDO };
    const confirmarVenta = sinon.stub().resolves({ venta: ventaFinal, pagos: [pagoFinal], comprobante });

    const confirmacion = useVentaConfirmacion({
      ...context,
      ventaService: { confirmarVenta },
      createIdempotencyKey: keyFactory('key-a'),
    });

    const result = await confirmacion.confirmar({ id: 40 } as ITipoComprobante);

    expect(confirmarVenta.calledOnce).toBeTruthy();
    const [payload, idempotencyKey] = confirmarVenta.firstCall.args;
    expect(idempotencyKey).toBe('key-a');
    expect(payload).toMatchObject({
      venta: {
        cliente: { id: 1 },
        vehiculo: { id: 1 },
        moneda: { id: 1 },
        total: 115,
      },
      tipoComprobanteId: 40,
    });
    expect(payload.venta.estado).toBeUndefined();
    expect(payload.venta.totalPagado).toBeUndefined();
    expect(payload.venta.saldo).toBeUndefined();
    expect(payload.pagos).toHaveLength(1);
    expect(payload.pagos[0].id).toBeUndefined();
    expect(payload.pagos[0]).toMatchObject({ monto: 100, metodoPago: { id: 1 }, moneda: { id: 1 } });
    expect(context.state.venta.value).toMatchObject(ventaFinal);
    expect(context.state.pagos.value).toHaveLength(2);
    expect(context.state.pagos.value[1]).toMatchObject({ id: 20, guardado: true });
    expect(context.state.comprobantes.value[0]).toMatchObject(comprobante);
    expect(result.venta).toBe(ventaFinal);
  });

  it('evita una segunda confirmacion concurrente', async () => {
    const context = crearContextoConfirmacion();
    let resolver!: (value: unknown) => void;
    const pendiente = new Promise(resolve => {
      resolver = resolve;
    });
    const confirmarVenta = sinon.stub().returns(pendiente);
    const confirmacion = useVentaConfirmacion({
      ...context,
      ventaService: { confirmarVenta },
      createIdempotencyKey: keyFactory('key-a'),
    });

    const primera = confirmacion.confirmar();
    const segunda = confirmacion.confirmar();

    expect(context.state.guardando.value).toBe(true);
    expect(confirmarVenta.calledOnce).toBeTruthy();

    const ventaFinal: IVenta = { id: 10, totalPagado: 100, saldo: 0, estado: EstadoVenta.PAGADA };
    resolver({ venta: ventaFinal, pagos: [], comprobante: null });
    await Promise.all([primera, segunda]);

    expect(confirmarVenta.calledOnce).toBeTruthy();
    expect(context.state.guardando.value).toBe(false);
  });

  it('libera loading y conserva un error coherente si falla el workflow', async () => {
    const context = crearContextoConfirmacion();
    const confirmarVenta = sinon.stub().rejects({ response: { data: { detail: 'Pago invalido' } } });
    const confirmacion = useVentaConfirmacion({
      ...context,
      ventaService: { confirmarVenta },
      createIdempotencyKey: keyFactory('key-a'),
    });

    await expect(confirmacion.confirmar()).rejects.toBeTruthy();

    expect(context.state.guardando.value).toBe(false);
    expect(context.state.error.value).toBe('Pago invalido');
  });

  it('reutiliza la misma key si falla y se reintenta el mismo payload', async () => {
    const context = crearContextoConfirmacion();
    const ventaFinal: IVenta = { id: 10, totalPagado: 100, saldo: 0, estado: EstadoVenta.PAGADA };
    const confirmarVenta = sinon.stub();
    confirmarVenta.onFirstCall().rejects(new Error('timeout'));
    confirmarVenta.onSecondCall().resolves({ venta: ventaFinal, pagos: [], comprobante: null });
    const confirmacion = useVentaConfirmacion({
      ...context,
      ventaService: { confirmarVenta },
      createIdempotencyKey: keyFactory('key-a', 'key-b'),
    });

    await expect(confirmacion.confirmar()).rejects.toThrow('timeout');
    await confirmacion.confirmar();

    expect(confirmarVenta.firstCall.args[1]).toBe('key-a');
    expect(confirmarVenta.secondCall.args[1]).toBe('key-a');
  });

  it('genera una nueva key si el payload cambia despues de un error', async () => {
    const context = crearContextoConfirmacion();
    const ventaFinal: IVenta = { id: 10, totalPagado: 120, saldo: 0, estado: EstadoVenta.PAGADA };
    const confirmarVenta = sinon.stub();
    confirmarVenta.onFirstCall().rejects(new Error('timeout'));
    confirmarVenta.onSecondCall().resolves({ venta: ventaFinal, pagos: [], comprobante: null });
    const confirmacion = useVentaConfirmacion({
      ...context,
      ventaService: { confirmarVenta },
      createIdempotencyKey: keyFactory('key-a', 'key-b'),
    });

    await expect(confirmacion.confirmar()).rejects.toThrow('timeout');
    context.state.venta.value.total = 120;
    await confirmacion.confirmar();

    expect(confirmarVenta.firstCall.args[1]).toBe('key-a');
    expect(confirmarVenta.secondCall.args[1]).toBe('key-b');
  });

  it('limpia la key despues de un success para la proxima operacion logica', async () => {
    const context = crearContextoConfirmacion();
    const confirmarVenta = sinon.stub();
    confirmarVenta.onFirstCall().resolves({ venta: { id: 10, totalPagado: 100, saldo: 0 }, pagos: [], comprobante: null });
    confirmarVenta.onSecondCall().resolves({ venta: { id: 11, totalPagado: 110, saldo: 0 }, pagos: [], comprobante: null });
    const confirmacion = useVentaConfirmacion({
      ...context,
      ventaService: { confirmarVenta },
      createIdempotencyKey: keyFactory('key-a', 'key-b'),
    });

    await confirmacion.confirmar();
    context.state.pagos.value = [pagoLocal({ guardado: false, monto: 110 })];
    context.state.venta.value.total = 110;
    await confirmacion.confirmar();

    expect(confirmarVenta.firstCall.args[1]).toBe('key-a');
    expect(confirmarVenta.secondCall.args[1]).toBe('key-b');
  });
});

function crearContextoConfirmacion() {
  const moneda: IMoneda = { id: 1, codigo: 'ARS' };
  const metodoPago: IMetodoPago = { id: 1, codigo: 'EFECTIVO' };
  const vehiculo: IVehiculo = {
    id: 1,
    precio: 100,
    moneda,
    estadoInventario: 'DISPONIBLE' as any,
  };
  const state = {
    venta: ref<Partial<IVenta>>({
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
    }),
    detalles: ref<DetalleLocal[]>([{ _key: 'vehiculo-1', vehiculo, precioUnitario: 100, subtotal: 100 }]),
    pagos: ref<PagoLocal[]>([pagoLocal({ guardado: false, monto: 100, metodoPago, moneda })]),
    comprobantes: ref<IComprobante[]>([]),
    guardando: ref(false),
    error: ref<string | null>(null),
  };

  return {
    state,
    rules: {
      tieneComprobanteActivo: computed(() =>
        state.comprobantes.value.some(comprobante => comprobante.estado === EstadoComprobante.EMITIDO),
      ),
      validarVentaAntesDeGuardar: sinon.stub(),
    },
    pagoDtoToLocal,
  };
}

function pagoDtoToLocal(pago: IPago): PagoLocal {
  return {
    _key: `loaded-${pago.id}`,
    id: pago.id,
    monto: Number(pago.monto),
    montoAplicadoMonedaVenta: Number(pago.montoAplicadoVenta ?? pago.monto ?? 0),
    fecha: new Date(pago.fecha!),
    referencia: pago.referencia ?? '',
    metodoPago: pago.metodoPago ?? null,
    moneda: pago.moneda ?? null,
    cotizacionUsada: Number(pago.cotizacionUsada ?? 1),
    estado: pago.estado ?? EstadoPago.REGISTRADO,
    guardado: true,
  };
}

function pagoLocal(options: {
  id?: number;
  guardado: boolean;
  monto: number;
  metodoPago?: IMetodoPago;
  moneda?: IMoneda;
  estado?: EstadoPago;
}): PagoLocal {
  return {
    _key: options.id ? `loaded-${options.id}` : `tmp-pago-${options.monto}`,
    id: options.id,
    monto: options.monto,
    montoAplicadoMonedaVenta: options.monto,
    fecha: new Date(),
    referencia: '',
    metodoPago: options.metodoPago ?? { id: 1, codigo: 'EFECTIVO' },
    moneda: options.moneda ?? { id: 1, codigo: 'ARS' },
    cotizacionUsada: 1,
    estado: options.estado ?? EstadoPago.REGISTRADO,
    guardado: options.guardado,
  };
}

function keyFactory(...keys: string[]) {
  let index = 0;
  return () => keys[index++] ?? `key-${index}`;
}
