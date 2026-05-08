import { computed, ref, watch } from 'vue';
import axios from 'axios';

import type { ICliente } from '@/shared/model/cliente.model';
import type { IComprobante } from '@/shared/model/comprobante.model';
import type { IEntidadFinanciera } from '@/shared/model/entidad-financiera.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';
import type { IMetodoPago } from '@/shared/model/metodo-pago.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { IPago } from '@/shared/model/pago.model';
import { EstadoPago } from '@/shared/model/estado-pago.model';
import { EstadoTasacionUsado } from '@/shared/model/enumerations/estado-tasacion-usado.model';
import { EstadoComprobante } from '@/shared/model/estado-comprobante.model';
import type { ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import type { ITasacionUsado } from '@/shared/model/tasacion-usado.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import type { IVenta } from '@/shared/model/venta.model';
import type { ICotizacion } from '@/shared/model/cotizacion.model';

export interface DetalleLocal {
  _key: string;
  vehiculo: IVehiculo;
  precioUnitario: number;
  subtotal: number;
}

export interface PagoLocal {
  _key: string;
  id?: number;
  monto: number;
  montoAplicadoMonedaVenta: number;
  fecha: Date;
  referencia: string;
  numeroOperacion?: string;
  bancoEntidad?: string;
  entidadFinanciera?: IEntidadFinanciera | null;
  comprobanteExterno?: string;
  observaciones?: string;
  metodoPago: IMetodoPago | null;
  moneda: IMoneda | null;
  cotizacionUsada: number;
  tasacionUsadoId?: number | null;
  tasacionUsado?: ITasacionUsado | null;
  estado: EstadoPago;
  guardado: boolean;
  usuarioRegistro?: string | null;
  comprobanteNumero?: string | null;
}

interface ICotizacionConversion {
  montoOriginal: number;
  montoConvertido: number;
  monedaOrigenId: number;
  monedaOrigenCodigo: string;
  monedaDestinoId: number;
  monedaDestinoCodigo: string;
  fechaOperacion: string;
  cotizacionAplicada: number;
  cotizacionOrigenId?: number | null;
  cotizacionDestinoId?: number | null;
  fechaCotizacionUsada?: string | null;
  detalleReglaAplicada?: string | null;
}

const EPSILON = 0.0001;
const MONEDA_BASE_CODIGO = 'ARS';

function normalizarMoneda(value?: string | null): string {
  return (value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toUpperCase()
    .replace(/[^A-Z]/g, '');
}

function esMonedaBase(moneda?: IMoneda | null): boolean {
  const codigo = normalizarMoneda(moneda?.codigo);
  const descripcion = normalizarMoneda(moneda?.descripcion);
  return (
    codigo.includes('ARS') ||
    codigo.includes('ARG') ||
    codigo.includes('PESO') ||
    descripcion.includes('ARS') ||
    descripcion.includes('ARG') ||
    descripcion.includes('PESO')
  );
}

export function useVentaEditor() {
  const venta = ref<Partial<IVenta>>({
    fecha: new Date(),
    estado: EstadoVenta.PENDIENTE,
    cotizacion: 1,
    porcentajeImpuesto: 21,
    importeNeto: 0,
    impuesto: 0,
    total: 0,
    totalPagado: 0,
    saldo: 0,
  });

  const detalles = ref<DetalleLocal[]>([]);
  const pagos = ref<PagoLocal[]>([]);
  const guardando = ref(false);
  const error = ref<string | null>(null);
  const comprobantes = ref<IComprobante[]>([]);
  const cargandoVenta = ref(false);

  const clientes = ref<ICliente[]>([]);
  const monedas = ref<IMoneda[]>([]);
  const metodoPagos = ref<IMetodoPago[]>([]);
  const tipoComprobantes = ref<ITipoComprobante[]>([]);
  const cotizacionActiva = ref<ICotizacionConversion | null>(null);
  const cotizacionesDisponibles = ref<ICotizacion[]>([]);
  const entidadesFinancieras = ref<IEntidadFinanciera[]>([]);
  const tasacionesUsadoDisponibles = ref<ITasacionUsado[]>([]);
  const cotizacionSeleccionadaId = ref<number | null>(null);
  const cargandoCotizaciones = ref(false);
  const porcentajeMinimoReserva = ref(0.1);

  const vehiculoSeleccionado = computed(() => detalles.value[0]?.vehiculo ?? venta.value.vehiculo ?? null);
  const monedaVehiculo = computed(() => vehiculoSeleccionado.value?.moneda ?? null);
  const monedasCoinciden = computed(() => {
    const monedaVentaId = venta.value.moneda?.id;
    const monedaVehiculoId = monedaVehiculo.value?.id;
    if (!monedaVentaId || !monedaVehiculoId) {
      return false;
    }
    return monedaVentaId === monedaVehiculoId;
  });
  const requiereCotizacion = computed(() => {
    const monedaVentaId = venta.value.moneda?.id;
    const monedaVehiculoId = monedaVehiculo.value?.id;
    if (!monedaVentaId || !monedaVehiculoId) {
      return false;
    }
    return monedaVentaId !== monedaVehiculoId;
  });
  const requiereSeleccionCotizacion = computed(() => {
    if (!requiereCotizacion.value) {
      return false;
    }
    return esMonedaBase(venta.value.moneda) || esMonedaBase(monedaVehiculo.value);
  });
  const precioBaseVehiculo = computed(() => Number(vehiculoSeleccionado.value?.precio ?? 0));
  const cotizacionAplicada = computed(() => {
    if (monedasCoinciden.value) {
      return 1;
    }
    return Number(venta.value.cotizacion ?? 0);
  });
  const importeConvertidoPreview = computed(() => {
    if (!requiereCotizacion.value) {
      return precioBaseVehiculo.value;
    }
    if (!Number.isFinite(cotizacionAplicada.value) || cotizacionAplicada.value <= 0) {
      return 0;
    }
    return precioBaseVehiculo.value * cotizacionAplicada.value;
  });
  const sumaSubtotales = computed(() => importeConvertidoPreview.value);
  const sumaPagos = computed(() =>
    pagos.value
      .filter(pago => pago.estado !== EstadoPago.ANULADO)
      .reduce((acc, pago) => acc + Number(pago.montoAplicadoMonedaVenta ?? pago.monto ?? 0), 0),
  );
  const montoMinimoReserva = computed(() => Math.max(0, sumaSubtotales.value * porcentajeMinimoReserva.value));
  const tieneComprobanteActivo = computed(() =>
    comprobantes.value.some(comprobante => comprobante.estado === EstadoComprobante.EMITIDO),
  );
  const cumpleMinimoReserva = computed(() => sumaPagos.value + EPSILON >= montoMinimoReserva.value && montoMinimoReserva.value > 0);
  const estadoCalculado = computed(() => {
    if (detalles.value.length === 0) {
      return EstadoVenta.PENDIENTE;
    }

    if (Number(venta.value.saldo ?? 0) <= EPSILON) {
      return EstadoVenta.PAGADA;
    }

    if (cumpleMinimoReserva.value) {
      return EstadoVenta.RESERVADA;
    }

    return EstadoVenta.PENDIENTE;
  });

  watch([importeConvertidoPreview, () => venta.value.porcentajeImpuesto], () => {
    const neto = importeConvertidoPreview.value;
    const porcentaje = Number(venta.value.porcentajeImpuesto ?? 0) / 100;
    const impuesto = neto * porcentaje;

    venta.value.importeNeto = neto;
    venta.value.impuesto = impuesto;
    venta.value.total = neto + impuesto;
    recalcularSaldo();
  });

  watch(sumaPagos, () => recalcularSaldo());

  watch(
    () => venta.value.moneda,
    moneda => {
      if (cargandoVenta.value) {
        return;
      }
      if (moneda?.id && monedaVehiculo.value?.id && moneda.id === monedaVehiculo.value.id) {
        venta.value.cotizacion = 1;
        cotizacionActiva.value = null;
        cotizacionSeleccionadaId.value = null;
        cotizacionesDisponibles.value = [];
        return;
      }
      if (moneda?.id && requiereCotizacion.value) {
        void cargarOpcionesCotizacion();
        void cargarCotizacionActiva({ forzarValor: true });
      }
    },
  );

  watch(
    () => venta.value.fecha,
    () => {
      if (cargandoVenta.value) {
        return;
      }
      if (requiereCotizacion.value) {
        void cargarOpcionesCotizacion();
        void cargarCotizacionActiva({ forzarValor: true });
      }
    },
  );

  function recalcularSaldo() {
    venta.value.totalPagado = sumaPagos.value;
    venta.value.saldo = Math.max(0, Number(venta.value.total ?? 0) - sumaPagos.value);
    venta.value.estado = estadoCalculado.value;
  }

  function setPorcentajeMinimoReserva(porcentaje: number) {
    if (!Number.isFinite(porcentaje) || porcentaje <= 0) {
      return;
    }
    porcentajeMinimoReserva.value = porcentaje;
    recalcularSaldo();
  }

  async function cargarCotizacionActiva(options?: { forzarValor?: boolean }) {
    const monedaOrigenId = monedaVehiculo.value?.id;
    const monedaDestinoId = venta.value.moneda?.id;
    if (!monedaOrigenId || !monedaDestinoId || monedaOrigenId === monedaDestinoId) {
      cotizacionActiva.value = null;
      return;
    }
    try {
      const res = await axios.get<ICotizacionConversion>('api/cotizacions/conversion', {
        params: {
          monedaOrigenId,
          monedaDestinoId,
          fecha: venta.value.fecha ? new Date(venta.value.fecha as Date).toISOString() : undefined,
        },
      });
      cotizacionActiva.value = res.data ?? null;
      if (
        cotizacionActiva.value?.cotizacionAplicada &&
        (options?.forzarValor || !venta.value.cotizacion || Number(venta.value.cotizacion) <= 0)
      ) {
        venta.value.cotizacion = Number(cotizacionActiva.value.cotizacionAplicada);
      }
    } catch {
      cotizacionActiva.value = null;
    }
  }

  function monedaCotizableId(): number | null {
    const monedaVenta = venta.value.moneda;
    const monedaUnidad = monedaVehiculo.value;
    if (!monedaVenta?.id || !monedaUnidad?.id || monedaVenta.id === monedaUnidad.id) {
      return null;
    }

    const unidadEsBase = esMonedaBase(monedaUnidad);
    const ventaEsBase = esMonedaBase(monedaVenta);
    if (unidadEsBase) {
      return monedaVenta.id ?? null;
    }
    if (ventaEsBase) {
      return monedaUnidad.id ?? null;
    }
    return null;
  }

  async function cargarOpcionesCotizacion(options?: { aplicarPorDefecto?: boolean }) {
    if (!requiereCotizacion.value) {
      cotizacionesDisponibles.value = [];
      cotizacionSeleccionadaId.value = null;
      return;
    }
    const monedaId = monedaCotizableId();
    if (!monedaId) {
      cotizacionesDisponibles.value = [];
      cotizacionSeleccionadaId.value = null;
      return;
    }
    cargandoCotizaciones.value = true;
    try {
      const res = await axios.get<ICotizacion[]>('api/cotizacions', {
        params: {
          'monedaId.equals': monedaId,
          'activo.equals': true,
          sort: 'fecha,desc',
          page: 0,
          size: 50,
        },
      });
      cotizacionesDisponibles.value = Array.isArray(res.data) ? res.data : [];
      const seleccionActual = cotizacionesDisponibles.value.find(item => item.id === cotizacionSeleccionadaId.value);
      const aplicarPorDefecto = options?.aplicarPorDefecto ?? true;
      if (!aplicarPorDefecto && !seleccionActual && Number(venta.value.cotizacion ?? 0) > 0) {
        const cotizacionGuardada = Number(venta.value.cotizacion ?? 0);
        const coincidencia = cotizacionesDisponibles.value.find(item => {
          const aplicada = cotizacionAplicadaDesdeRegistro(item);
          return aplicada != null && Math.abs(aplicada - cotizacionGuardada) < 0.0000001;
        });
        if (coincidencia?.id) {
          cotizacionSeleccionadaId.value = coincidencia.id;
        }
      }
      if (aplicarPorDefecto && !seleccionActual && cotizacionesDisponibles.value.length > 0) {
        aplicarCotizacionSeleccionada(cotizacionesDisponibles.value[0].id ?? null);
      }
    } catch {
      cotizacionesDisponibles.value = [];
      cotizacionSeleccionadaId.value = null;
    } finally {
      cargandoCotizaciones.value = false;
    }
  }

  function aplicarCotizacionSeleccionada(cotizacionId: number | null) {
    cotizacionSeleccionadaId.value = cotizacionId;
    const cotizacion = cotizacionesDisponibles.value.find(item => item.id === cotizacionId);
    if (!cotizacion) {
      return;
    }
    const cotizacionAplicada = cotizacionAplicadaDesdeRegistro(cotizacion);
    if (cotizacionAplicada != null) {
      venta.value.cotizacion = cotizacionAplicada;
    }
  }

  function cotizacionAplicadaDesdeRegistro(cotizacion: ICotizacion): number | null {
    const valorVenta = Number(cotizacion.valorVenta ?? 0);
    if (!Number.isFinite(valorVenta) || valorVenta <= 0) {
      return null;
    }
    const monedaVenta = venta.value.moneda;
    const monedaUnidad = monedaVehiculo.value;
    if (!monedaVenta?.id || !monedaUnidad?.id) {
      return null;
    }
    const unidadEsBase = esMonedaBase(monedaUnidad);
    const ventaEsBase = esMonedaBase(monedaVenta);

    if (unidadEsBase && cotizacion.moneda?.id === monedaVenta.id) {
      return Number(valorVenta.toFixed(8));
    }
    if (ventaEsBase && cotizacion.moneda?.id === monedaUnidad.id) {
      return Number((1 / valorVenta).toFixed(8));
    }
    if (cotizacion.moneda?.id === monedaVenta.id) {
      return Number(valorVenta.toFixed(8));
    }
    return null;
  }

  function resolverMonedaBaseVenta(): IMoneda | null {
    return monedas.value.find(item => esMonedaBase(item)) ?? null;
  }

  async function resolverCotizacionPagoEnMonedaVenta(monedaPago: IMoneda | null): Promise<number> {
    const monedaVentaId = venta.value.moneda?.id;
    if (!monedaVentaId) {
      throw new Error('No se pudo resolver la moneda de la venta');
    }
    if (!monedaPago?.id || monedaPago.id === monedaVentaId) {
      return 1;
    }

    const res = await axios.get<ICotizacionConversion>('api/cotizacions/conversion', {
      params: {
        monedaOrigenId: monedaPago.id,
        monedaDestinoId: monedaVentaId,
        fecha: venta.value.fecha ? new Date(venta.value.fecha as Date).toISOString() : undefined,
      },
    });
    const cotizacion = Number(res.data?.cotizacionAplicada ?? 0);
    if (!Number.isFinite(cotizacion) || cotizacion <= 0) {
      throw new Error('No existe cotizacion valida para convertir el pago a la moneda de la venta');
    }
    return cotizacion;
  }

  function agregarVehiculo(vehiculo: IVehiculo) {
    if (detalles.value.length > 0) {
      error.value = 'Cada venta solo puede tener un vehiculo. Quita el actual antes de seleccionar otro.';
      return;
    }

    if (vehiculo.estadoInventario === 'VENDIDO') {
      error.value = `El vehiculo ${vehiculo.patente} ya fue vendido`;
      return;
    }

    const precio = Number(vehiculo.precio ?? 0);
    if (!Number.isFinite(precio) || precio <= 0) {
      error.value = `El vehiculo ${vehiculo.patente} no tiene un precio valido`;
      return;
    }
    if (!vehiculo.moneda?.id) {
      error.value = `El vehiculo ${vehiculo.patente} no tiene moneda configurada`;
      return;
    }

    error.value = null;
    detalles.value = [{
      _key: `vehiculo-${vehiculo.id ?? Date.now()}`,
      vehiculo,
      precioUnitario: precio,
      subtotal: precio,
    }];
    venta.value.vehiculo = vehiculo;
    if (!venta.value.moneda?.id) {
      venta.value.moneda = resolverMonedaBaseVenta() ?? vehiculo.moneda;
      venta.value.cotizacion = 1;
    } else if (venta.value.moneda.id === vehiculo.moneda.id) {
      venta.value.cotizacion = 1;
      cotizacionActiva.value = null;
      cotizacionSeleccionadaId.value = null;
      cotizacionesDisponibles.value = [];
    } else {
      void cargarOpcionesCotizacion();
      void cargarCotizacionActiva({ forzarValor: true });
    }
  }

  function actualizarPrecioDetalle(_key: string, nuevoPrecio: number) {
    void _key;
    void nuevoPrecio;
    error.value = 'El precio base se toma del vehiculo y no puede editarse en la venta.';
  }

  function quitarDetalle(_key: string) {
    detalles.value = [];
    venta.value.vehiculo = null;
    cotizacionActiva.value = null;
    cotizacionSeleccionadaId.value = null;
    cotizacionesDisponibles.value = [];
    error.value = null;
  }

  async function agregarPago(
    monto: number,
    metodoPago: IMetodoPago | null,
    moneda: IMoneda | null,
    tasacionUsadoId?: number | null,
    entidadFinanciera: IEntidadFinanciera | null = null,
    bancoEntidad = '',
    comprobanteExterno = '',
    observaciones = '',
  ) {
    const esEntregaUsado = (metodoPago?.codigo ?? '').toUpperCase() === 'ENTREGA_USADO';
    let tasacionSeleccionada: ITasacionUsado | null = null;
    if (esEntregaUsado) {
      const tasacionId = Number(tasacionUsadoId ?? 0);
      tasacionSeleccionada = tasacionesUsadoDisponibles.value.find(item => item.id === tasacionId) ?? null;
      if (!tasacionSeleccionada) {
        error.value = 'Debes seleccionar una tasacion aceptada para ENTREGA_USADO';
        return;
      }
      if (tasacionSeleccionada.estado !== EstadoTasacionUsado.ACEPTADA) {
        error.value = 'La tasacion debe estar aceptada para aplicarse como pago';
        return;
      }
      monto = Number(tasacionSeleccionada.montoTasacion ?? 0);
      moneda = venta.value.moneda ?? moneda ?? null;
    }
    const montoNormalizado = Number(monto ?? 0);

    if (!Number.isFinite(montoNormalizado) || montoNormalizado <= 0) {
      error.value = 'El monto debe ser mayor que 0';
      return;
    }

    const monedaPago = moneda ?? venta.value.moneda ?? null;
    let cotizacionUsada = 1;
    let montoAplicadoMonedaVenta = montoNormalizado;
    try {
      cotizacionUsada = await resolverCotizacionPagoEnMonedaVenta(monedaPago);
      montoAplicadoMonedaVenta = Number((montoNormalizado * cotizacionUsada).toFixed(2));
    } catch (e: any) {
      error.value = e?.message ?? 'No se pudo convertir el pago a la moneda de la venta';
      return;
    }

    const saldoActual = Number(venta.value.saldo ?? 0);
    if (saldoActual <= EPSILON) {
      error.value = 'No hay saldo pendiente para registrar pagos';
      return;
    }

    if (montoAplicadoMonedaVenta - saldoActual > EPSILON) {
      error.value = 'El pago convertido no puede superar el saldo pendiente';
      return;
    }
    const totalProyectado = sumaPagos.value + montoAplicadoMonedaVenta;
    if (totalProyectado > EPSILON && totalProyectado + EPSILON < montoMinimoReserva.value) {
      error.value = `La seña minima requerida es $ ${fmt(montoMinimoReserva.value)}`;
      return;
    }

    error.value = null;
    pagos.value.push({
      _key: `tmp-pago-${Date.now()}`,
      monto: montoNormalizado,
      montoAplicadoMonedaVenta,
      fecha: new Date(),
      referencia: '',
      numeroOperacion: '',
      bancoEntidad: bancoEntidad.trim(),
      entidadFinanciera,
      comprobanteExterno: comprobanteExterno.trim(),
      observaciones: observaciones.trim(),
      metodoPago,
      moneda: monedaPago,
      cotizacionUsada,
      tasacionUsadoId: tasacionSeleccionada?.id ?? null,
      tasacionUsado: tasacionSeleccionada,
      estado: EstadoPago.REGISTRADO,
      guardado: false,
      usuarioRegistro: null,
    });
  }

  function quitarPago(key: string) {
    pagos.value = pagos.value.filter(pago => pago._key !== key);
    error.value = null;
  }

  function validarVentaAntesDeGuardar() {
    if (!venta.value.cliente?.id) {
      throw new Error('Debe seleccionar un cliente');
    }

    if (!venta.value.fecha || Number.isNaN(new Date(venta.value.fecha as Date).getTime())) {
      venta.value.fecha = new Date();
    }

    if (detalles.value.length === 0) {
      throw new Error('Debe agregar al menos un vehiculo');
    }

    if (detalles.value.some(detalle => !detalle.vehiculo?.id)) {
      throw new Error('Debe seleccionar un vehiculo valido');
    }
    if (detalles.value.some(detalle => !detalle.vehiculo?.moneda?.id)) {
      throw new Error('El vehiculo seleccionado no tiene moneda configurada');
    }
    if (!venta.value.moneda?.id) {
      venta.value.moneda = resolverMonedaBaseVenta();
    }
    if (!venta.value.moneda?.id) {
      throw new Error('No se pudo resolver la moneda base de la venta');
    }

    const monedaVehiculoId = detalles.value[0]?.vehiculo?.moneda?.id;
    const monedaVentaId = venta.value.moneda.id;
      if (monedaVehiculoId && monedaVentaId === monedaVehiculoId) {
        venta.value.cotizacion = 1;
      } else {
        const cotizacion = Number(venta.value.cotizacion ?? 0);
        if (!Number.isFinite(cotizacion) || cotizacion <= 0) {
        throw new Error('La cotizacion es obligatoria cuando la moneda de la venta difiere de la moneda del vehiculo');
      }
    }

    if (detalles.value.some(detalle => detalle.vehiculo.estadoInventario === 'VENDIDO')) {
      throw new Error('No se puede confirmar una venta con vehiculos ya vendidos');
    }

    if (
      detalles.value.some(
        detalle =>
          !Number.isFinite(Number(detalle.precioUnitario)) ||
          !Number.isFinite(Number(detalle.subtotal)) ||
          Number(detalle.precioUnitario) <= 0 ||
          Number(detalle.subtotal) <= 0,
      )
    ) {
      throw new Error('El vehiculo seleccionado debe tener importes validos');
    }

    const importeNeto = Number(venta.value.importeNeto ?? 0);
    const impuesto = Number(venta.value.impuesto ?? 0);
    const total = Number(venta.value.total ?? 0);

    if (importeNeto <= 0 || total <= 0) {
      throw new Error('La venta debe tener un total mayor a 0');
    }

    const totalCalculado = Number((importeNeto + impuesto).toFixed(2));
    const totalActual = Number(total.toFixed(2));
    if (totalCalculado !== totalActual) {
      throw new Error('El total debe coincidir con importe neto + impuesto');
    }

    for (const pago of pagos.value.filter(item => item.estado !== EstadoPago.ANULADO)) {
      if (!Number.isFinite(Number(pago.monto)) || Number(pago.monto) <= 0) {
        throw new Error('Todos los pagos deben tener un monto mayor a 0');
      }

    }

    if (sumaPagos.value - total > EPSILON) {
      throw new Error('Los pagos no pueden superar el total de la venta');
    }
    if (sumaPagos.value <= EPSILON) {
      throw new Error(`Debe registrar un pago minimo de $ ${fmt(montoMinimoReserva.value)} para continuar`);
    }
    if (sumaPagos.value > EPSILON && sumaPagos.value + EPSILON < montoMinimoReserva.value) {
      throw new Error(`La venta requiere una seña minima de $ ${fmt(montoMinimoReserva.value)}`);
    }

    venta.value.estado = estadoCalculado.value;
  }

  async function guardarVenta(): Promise<IVenta> {
    validarVentaAntesDeGuardar();

    const payload = {
      id: venta.value.id,
      fecha: new Date(),
      estado: estadoCalculado.value,
      cotizacion: venta.value.cotizacion,
      porcentajeImpuesto: venta.value.porcentajeImpuesto,
      importeNeto: venta.value.importeNeto,
      impuesto: venta.value.impuesto,
      total: venta.value.total,
      totalPagado: venta.value.totalPagado,
      saldo: venta.value.saldo,
      observaciones: venta.value.observaciones,
      cliente: venta.value.cliente ? { id: venta.value.cliente.id } : null,
      moneda: venta.value.moneda ? { id: venta.value.moneda.id } : null,
      reserva: venta.value.reserva ? { id: venta.value.reserva.id } : null,
      vehiculo: detalles.value[0]?.vehiculo?.id
        ? { id: detalles.value[0].vehiculo.id }
        : venta.value.vehiculo?.id
          ? { id: venta.value.vehiculo.id }
          : null,
    };

    const res = venta.value.id
      ? await axios.put<IVenta>(`api/ventas/${venta.value.id}`, payload)
      : await axios.post<IVenta>('api/ventas', payload);

    venta.value = { ...venta.value, ...res.data, estado: estadoCalculado.value };
    return res.data;
  }

  async function guardarPagos(ventaId: number) {
    for (const pago of pagos.value.filter(item => !item.guardado && item.estado !== EstadoPago.ANULADO)) {
      const payload = {
        id: pago.id,
        monto: pago.monto,
        fecha: pago.fecha,
        referencia: pago.referencia || null,
        bancoEntidad: pago.bancoEntidad || null,
        entidadFinanciera: pago.entidadFinanciera?.id ? { id: pago.entidadFinanciera.id } : null,
        comprobanteExterno: pago.comprobanteExterno || null,
        observaciones: pago.observaciones || null,
        cotizacionUsada: pago.cotizacionUsada,
        tasacionUsadoId: pago.tasacionUsadoId ?? null,
        venta: { id: ventaId },
        metodoPago: pago.metodoPago ? { id: pago.metodoPago.id } : null,
        moneda: pago.moneda ? { id: pago.moneda.id } : null,
      };

      const res = await axios.post(`api/pagos/registrar?ventaId=${ventaId}`, payload);
      pago.id = res.data.id;
      pago.referencia = res.data.referencia ?? pago.referencia ?? '';
      pago.numeroOperacion = res.data.numeroOperacion ?? pago.numeroOperacion ?? '';
      pago.bancoEntidad = res.data.bancoEntidad ?? pago.bancoEntidad ?? '';
      pago.entidadFinanciera = res.data.entidadFinanciera ?? pago.entidadFinanciera ?? null;
      pago.comprobanteExterno = res.data.comprobanteExterno ?? pago.comprobanteExterno ?? '';
      pago.observaciones = res.data.observaciones ?? pago.observaciones ?? '';
      pago.guardado = true;
      pago.estado = res.data.estado ?? EstadoPago.REGISTRADO;
      await refrescarComprobantePago(pago);
    }
  }

  async function refrescarComprobantePago(pago: PagoLocal) {
    if (!pago.id) {
      return;
    }
    try {
      const res = await axios.get<IComprobante[]>(`api/comprobantes/by-pago/${pago.id}`);
      const emitido = (res.data ?? []).find(item => item.estado === EstadoComprobante.EMITIDO) ?? res.data?.[0];
      pago.comprobanteNumero = emitido?.numeroComprobante ?? null;
    } catch {
      pago.comprobanteNumero = null;
    }
  }

  async function generarComprobante(ventaId: number, tipoComprobante: ITipoComprobante): Promise<IComprobante> {
    if (!tipoComprobante?.id) {
      throw new Error('Debe seleccionar un tipo de comprobante');
    }
    if (tieneComprobanteActivo.value) {
      throw new Error('La venta ya tiene un comprobante activo emitido. Debes anularlo antes de emitir otro.');
    }

    const res = await axios.post<IComprobante>(`api/comprobantes/emitir?ventaId=${ventaId}&tipoComprobanteId=${tipoComprobante.id}`);
    const emitido = res.data;
    comprobantes.value = [emitido, ...comprobantes.value.filter(item => item.id !== emitido.id)];
    return emitido;
  }

  async function confirmar(tipoComprobante?: ITipoComprobante): Promise<{ venta: IVenta; comprobante?: IComprobante }> {
    guardando.value = true;
    error.value = null;

    try {
      validarVentaAntesDeGuardar();
      const ventaGuardada = await guardarVenta();
      await guardarPagos(ventaGuardada.id!);

      let comprobante: IComprobante | undefined;
      if (tipoComprobante) {
        comprobante = await generarComprobante(ventaGuardada.id!, tipoComprobante);
      }

      return { venta: ventaGuardada, comprobante };
    } finally {
      guardando.value = false;
    }
  }

  async function cargarVenta(ventaId: number) {
    cargandoVenta.value = true;
    try {
      const [ventaRes, pagosRes, comprobantesRes] = await Promise.all([
        axios.get<IVenta>(`api/ventas/${ventaId}`),
        axios.get(`api/pagos/by-venta/${ventaId}`),
        axios.get(`api/comprobantes/by-venta/${ventaId}`),
      ]);

      venta.value = ventaRes.data;
      if (ventaRes.data.vehiculo?.id) {
        detalles.value = [{
          _key: `vehiculo-${ventaRes.data.vehiculo.id}`,
          vehiculo: ventaRes.data.vehiculo,
          precioUnitario: Number(ventaRes.data.vehiculo.precio ?? ventaRes.data.importeNeto ?? 0),
          subtotal: Number(ventaRes.data.importeConvertido ?? ventaRes.data.importeNeto ?? 0),
        }];
        await cargarOpcionesCotizacion({ aplicarPorDefecto: false });
      } else {
        detalles.value = [];
      }

      pagos.value = (pagosRes.data as IPago[]).map(pago => ({
        _key: `loaded-${pago.id}`,
        id: pago.id,
        monto: Number(pago.monto),
      montoAplicadoMonedaVenta: Number(pago.montoAplicadoVenta ?? pago.monto ?? 0),
        fecha: new Date(pago.fecha!),
        referencia: pago.referencia ?? '',
        numeroOperacion: pago.numeroOperacion ?? '',
        bancoEntidad: pago.bancoEntidad ?? '',
        entidadFinanciera: pago.entidadFinanciera ?? null,
        comprobanteExterno: pago.comprobanteExterno ?? '',
        observaciones: pago.observaciones ?? '',
        metodoPago: pago.metodoPago ?? null,
        moneda: pago.moneda ?? null,
        cotizacionUsada: Number(pago.cotizacionUsada ?? 1),
        tasacionUsadoId: pago.tasacionUsadoId ?? pago.tasacionUsado?.id ?? null,
        tasacionUsado: pago.tasacionUsado ?? null,
        estado: pago.estado ?? EstadoPago.REGISTRADO,
        guardado: true,
        usuarioRegistro: pago.usuarioRegistro ?? null,
        comprobanteNumero: null,
      }));
      await Promise.all(pagos.value.map(item => refrescarComprobantePago(item)));

      comprobantes.value = comprobantesRes.data as IComprobante[];

      recalcularSaldo();
    } finally {
      cargandoVenta.value = false;
    }
  }

  async function anularPago(key: string, motivo: string) {
    const pago = pagos.value.find(item => item._key === key);
    if (!pago) {
      return;
    }

    if (!pago.guardado) {
      quitarPago(key);
      return;
    }

    if (!pago.id || pago.estado === EstadoPago.ANULADO) {
      return;
    }

    await axios.post(`api/pagos/${pago.id}/anular`, { motivo });
    pago.estado = EstadoPago.ANULADO;

    if (venta.value.id) {
      const [ventaRes, pagosRes] = await Promise.all([
        axios.get<IVenta>(`api/ventas/${venta.value.id}`),
        axios.get(`api/pagos/by-venta/${venta.value.id}`),
      ]);
      venta.value = ventaRes.data;
      pagos.value = (pagosRes.data as IPago[]).map(item => ({
        _key: `loaded-${item.id}`,
        id: item.id,
        monto: Number(item.monto),
      montoAplicadoMonedaVenta: Number(item.montoAplicadoVenta ?? item.monto ?? 0),
        fecha: new Date(item.fecha!),
        referencia: item.referencia ?? '',
        numeroOperacion: item.numeroOperacion ?? '',
        bancoEntidad: item.bancoEntidad ?? '',
        entidadFinanciera: item.entidadFinanciera ?? null,
        comprobanteExterno: item.comprobanteExterno ?? '',
        observaciones: item.observaciones ?? '',
        metodoPago: item.metodoPago ?? null,
        moneda: item.moneda ?? null,
        cotizacionUsada: Number(item.cotizacionUsada ?? 1),
        tasacionUsadoId: item.tasacionUsadoId ?? item.tasacionUsado?.id ?? null,
        tasacionUsado: item.tasacionUsado ?? null,
        estado: item.estado ?? EstadoPago.REGISTRADO,
        guardado: true,
        usuarioRegistro: item.usuarioRegistro ?? null,
        comprobanteNumero: null,
      }));
      await Promise.all(pagos.value.map(entry => refrescarComprobantePago(entry)));
    }

    recalcularSaldo();
  }

  async function cargarTasacionesUsadoCliente() {
    const clienteId = venta.value.cliente?.id;
    if (!clienteId) {
      tasacionesUsadoDisponibles.value = [];
      return;
    }
    const res = await axios.get<ITasacionUsado[]>('api/tasacion-usados/aceptadas-disponibles', { params: { clienteId } });
    tasacionesUsadoDisponibles.value = Array.isArray(res.data) ? res.data : [];
  }

  const fmt = (n?: number | null) => Number(n ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  return {
    venta,
    detalles,
    pagos,
    guardando,
    error,
    clientes,
    monedas,
    metodoPagos,
    tipoComprobantes,
    cotizacionActiva,
    cotizacionesDisponibles,
    entidadesFinancieras,
    tasacionesUsadoDisponibles,
    cotizacionSeleccionadaId,
    cargandoCotizaciones,
    porcentajeMinimoReserva,
    montoMinimoReserva,
    tieneComprobanteActivo,
    cumpleMinimoReserva,
    estadoCalculado,
    sumaSubtotales,
    sumaPagos,
    vehiculoSeleccionado,
    monedaVehiculo,
    precioBaseVehiculo,
    importeConvertidoPreview,
    cotizacionAplicada,
    requiereCotizacion,
    requiereSeleccionCotizacion,
    monedasCoinciden,
    agregarVehiculo,
    actualizarPrecioDetalle,
    quitarDetalle,
    agregarPago,
    quitarPago,
    anularPago,
    confirmar,
    cargarVenta,
    cargarCotizacionActiva,
    cargarOpcionesCotizacion,
    aplicarCotizacionSeleccionada,
    setPorcentajeMinimoReserva,
    cargarTasacionesUsadoCliente,
    validarVentaAntesDeGuardar,
    fmt,
  };
}
