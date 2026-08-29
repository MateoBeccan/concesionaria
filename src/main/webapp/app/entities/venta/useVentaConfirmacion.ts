import type { Ref } from 'vue';

import type { IComprobante } from '@/shared/model/comprobante.model';
import { EstadoPago } from '@/shared/model/estado-pago.model';
import type { IPago } from '@/shared/model/pago.model';
import type { ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import type { IVenta } from '@/shared/model/venta.model';

import type { DetalleLocal, PagoLocal } from './useVentaEditor';
import VentaService, { type IConfirmarVentaRequest } from './venta.service';

interface VentaConfirmacionState {
  venta: Ref<Partial<IVenta>>;
  detalles: Ref<DetalleLocal[]>;
  pagos: Ref<PagoLocal[]>;
  comprobantes: Ref<IComprobante[]>;
  guardando: Ref<boolean>;
  error: Ref<string | null>;
}

interface VentaConfirmacionRules {
  tieneComprobanteActivo: Ref<boolean>;
  validarVentaAntesDeGuardar: () => void;
}

interface UseVentaConfirmacionOptions {
  state: VentaConfirmacionState;
  rules: VentaConfirmacionRules;
  ventaService?: Pick<VentaService, 'confirmarVenta'>;
  pagoDtoToLocal: (pago: IPago) => PagoLocal;
  createIdempotencyKey?: () => string;
}

export function pagoLocalToPayload(pago: PagoLocal, ventaId?: number): IPago {
  return {
    monto: pago.monto,
    fecha: pago.fecha,
    referencia: pago.referencia || null,
    bancoEntidad: pago.bancoEntidad || null,
    entidadFinanciera: pago.entidadFinanciera?.id ? { id: pago.entidadFinanciera.id } : null,
    comprobanteExterno: pago.comprobanteExterno || null,
    observaciones: pago.observaciones || null,
    cotizacionUsada: pago.cotizacionUsada,
    tasacionUsadoId: pago.tasacionUsadoId ?? null,
    venta: ventaId ? { id: ventaId } : null,
    metodoPago: pago.metodoPago ? { id: pago.metodoPago.id } : null,
    moneda: pago.moneda ? { id: pago.moneda.id } : null,
  };
}

export function useVentaConfirmacion(options: UseVentaConfirmacionOptions) {
  const { state, rules, pagoDtoToLocal } = options;
  const ventaService = options.ventaService ?? new VentaService();
  let confirmacionEnCurso: Promise<{ venta: IVenta; comprobante?: IComprobante }> | null = null;
  let idempotencyKeyActual: string | null = null;
  let payloadFingerprintActual: string | null = null;

  function ventaPayload(): IVenta {
    return {
      id: state.venta.value.id,
      fecha: state.venta.value.fecha ?? new Date(),
      cotizacion: state.venta.value.cotizacion,
      fechaCotizacionUsada: state.venta.value.fechaCotizacionUsada,
      cotizacionId: state.venta.value.cotizacionId,
      porcentajeImpuesto: state.venta.value.porcentajeImpuesto,
      importeNeto: state.venta.value.importeNeto,
      impuesto: state.venta.value.impuesto,
      total: state.venta.value.total,
      observaciones: state.venta.value.observaciones,
      cliente: state.venta.value.cliente ? { id: state.venta.value.cliente.id } : null,
      moneda: state.venta.value.moneda ? { id: state.venta.value.moneda.id } : null,
      reserva: state.venta.value.reserva ? { id: state.venta.value.reserva.id } : null,
      vehiculo: state.detalles.value[0]?.vehiculo?.id
        ? { id: state.detalles.value[0].vehiculo.id }
        : state.venta.value.vehiculo?.id
          ? { id: state.venta.value.vehiculo.id }
          : null,
      tasacionUsado: state.venta.value.tasacionUsado?.id ? { id: state.venta.value.tasacionUsado.id } : null,
    };
  }

  function pagosNuevosParaConfirmar(): IPago[] {
    return state.pagos.value.filter(item => !item.guardado && item.estado !== EstadoPago.ANULADO).map(item => pagoLocalToPayload(item));
  }

  async function confirmar(tipoComprobante?: ITipoComprobante): Promise<{ venta: IVenta; comprobante?: IComprobante }> {
    if (confirmacionEnCurso) {
      return confirmacionEnCurso;
    }
    confirmacionEnCurso = ejecutarConfirmacion(tipoComprobante);
    try {
      return await confirmacionEnCurso;
    } finally {
      confirmacionEnCurso = null;
    }
  }

  async function ejecutarConfirmacion(tipoComprobante?: ITipoComprobante): Promise<{ venta: IVenta; comprobante?: IComprobante }> {
    state.guardando.value = true;
    state.error.value = null;

    try {
      rules.validarVentaAntesDeGuardar();
      if (tipoComprobante && rules.tieneComprobanteActivo.value) {
        throw new Error('La venta ya tiene un comprobante activo emitido. Debes anularlo antes de emitir otro.');
      }

      const payload: IConfirmarVentaRequest = {
        venta: ventaPayload(),
        pagos: pagosNuevosParaConfirmar(),
        tipoComprobanteId: tipoComprobante?.id ?? null,
      };
      const payloadFingerprint = fingerprintPayload(payload);
      const idempotencyKey = resolverIdempotencyKey(payloadFingerprint);
      const response = await ventaService.confirmarVenta(payload, idempotencyKey);
      state.venta.value = response.venta;
      limpiarIdempotencyKey();

      const pagosPersistidos = state.pagos.value.filter(item => item.guardado);
      const pagosConfirmados = (response.pagos ?? []).map(pagoDtoToLocal);
      state.pagos.value = [...pagosPersistidos, ...pagosConfirmados];

      const comprobante = response.comprobante ?? undefined;
      if (comprobante) {
        state.comprobantes.value = [comprobante, ...state.comprobantes.value.filter(item => item.id !== comprobante.id)];
      }

      return { venta: response.venta, comprobante };
    } catch (e: any) {
      state.error.value = e?.response?.data?.detail ?? e?.response?.data?.message ?? e?.message ?? 'No se pudo confirmar la venta.';
      throw e;
    } finally {
      state.guardando.value = false;
    }
  }

  return {
    confirmar,
  };

  function resolverIdempotencyKey(payloadFingerprint: string): string {
    if (!idempotencyKeyActual || payloadFingerprintActual !== payloadFingerprint) {
      idempotencyKeyActual = crearIdempotencyKey();
      payloadFingerprintActual = payloadFingerprint;
    }
    return idempotencyKeyActual;
  }

  function limpiarIdempotencyKey() {
    idempotencyKeyActual = null;
    payloadFingerprintActual = null;
  }

  function crearIdempotencyKey(): string {
    return options.createIdempotencyKey?.() ?? globalThis.crypto.randomUUID();
  }
}

function fingerprintPayload(payload: IConfirmarVentaRequest): string {
  return JSON.stringify(sortObjectKeys(payload));
}

function sortObjectKeys(value: unknown): unknown {
  if (Array.isArray(value)) {
    return value.map(sortObjectKeys);
  }
  if (value instanceof Date) {
    return value.toISOString();
  }
  if (value && typeof value === 'object') {
    return Object.keys(value)
      .sort()
      .reduce<Record<string, unknown>>((acc, key) => {
        acc[key] = sortObjectKeys((value as Record<string, unknown>)[key]);
        return acc;
      }, {});
  }
  return value;
}
