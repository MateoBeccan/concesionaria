import { computed, ref, watch } from 'vue';
import axios from 'axios';

import type { ICliente } from '@/shared/model/cliente.model';
import type { IComprobante } from '@/shared/model/comprobante.model';
import type { ICotizacion } from '@/shared/model/cotizacion.model';
import type { IDetalleVenta } from '@/shared/model/detalle-venta.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';
import type { IMetodoPago } from '@/shared/model/metodo-pago.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { IPago } from '@/shared/model/pago.model';
import type { ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import type { IVenta } from '@/shared/model/venta.model';

export interface DetalleLocal {
  _key: string;
  id?: number;
  vehiculo: IVehiculo;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
  guardado: boolean;
}

export interface PagoLocal {
  _key: string;
  id?: number;
  monto: number;
  fecha: Date;
  referencia: string;
  metodoPago: IMetodoPago | null;
  moneda: IMoneda | null;
  guardado: boolean;
}

const EPSILON = 0.0001;

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

  const clientes = ref<ICliente[]>([]);
  const monedas = ref<IMoneda[]>([]);
  const metodoPagos = ref<IMetodoPago[]>([]);
  const tipoComprobantes = ref<ITipoComprobante[]>([]);
  const cotizacionActiva = ref<ICotizacion | null>(null);

  const sumaSubtotales = computed(() => detalles.value.reduce((acc, detalle) => acc + detalle.subtotal, 0));
  const sumaPagos = computed(() => pagos.value.reduce((acc, pago) => acc + pago.monto, 0));
  const estadoCalculado = computed(() => {
    if (detalles.value.length === 0) {
      return EstadoVenta.PENDIENTE;
    }

    if (Number(venta.value.saldo ?? 0) <= EPSILON) {
      return EstadoVenta.PAGADA;
    }

    if (sumaPagos.value > 0) {
      return EstadoVenta.RESERVADA;
    }

    return EstadoVenta.PENDIENTE;
  });

  watch([sumaSubtotales, () => venta.value.porcentajeImpuesto], () => {
    const neto = sumaSubtotales.value;
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
      if (moneda?.id) {
        cargarCotizacionActiva(moneda.id);
      }
    },
  );

  function recalcularSaldo() {
    venta.value.totalPagado = sumaPagos.value;
    venta.value.saldo = Math.max(0, Number(venta.value.total ?? 0) - sumaPagos.value);
    venta.value.estado = estadoCalculado.value;
  }

  async function cargarCotizacionActiva(monedaId: number) {
    try {
      const res = await axios.get('api/cotizacions', {
        params: { 'monedaId.equals': monedaId, 'activo.equals': true, page: 0, size: 1, sort: 'fecha,desc' },
      });

      cotizacionActiva.value = res.data?.[0] ?? null;
      if (cotizacionActiva.value) {
        venta.value.cotizacion = cotizacionActiva.value.valorVenta;
      }
    } catch {
      cotizacionActiva.value = null;
    }
  }

  function agregarVehiculo(vehiculo: IVehiculo) {
    if (detalles.value.some(detalle => detalle.vehiculo.id === vehiculo.id)) {
      error.value = `El vehiculo ${vehiculo.patente} ya esta agregado en la venta`;
      return;
    }

    if (vehiculo.condicion === 'VENDIDO') {
      error.value = `El vehiculo ${vehiculo.patente} ya fue vendido`;
      return;
    }

    const precio = Number(vehiculo.precio ?? 0);
    if (!Number.isFinite(precio) || precio <= 0) {
      error.value = `El vehiculo ${vehiculo.patente} no tiene un precio valido`;
      return;
    }

    error.value = null;
    detalles.value.push({
      _key: `tmp-${Date.now()}-${vehiculo.id}`,
      vehiculo,
      precioUnitario: precio,
      cantidad: 1,
      subtotal: precio,
      guardado: false,
    });
  }

  function actualizarPrecioDetalle(key: string, nuevoPrecio: number) {
    const detalle = detalles.value.find(item => item._key === key);
    if (!detalle) return;

    if (!Number.isFinite(nuevoPrecio) || nuevoPrecio <= 0) {
      error.value = 'El precio del vehiculo debe ser mayor a 0';
      return;
    }

    detalle.precioUnitario = nuevoPrecio;
    detalle.subtotal = nuevoPrecio * detalle.cantidad;
    detalle.guardado = false;
    error.value = null;
  }

  function quitarDetalle(key: string) {
    detalles.value = detalles.value.filter(detalle => detalle._key !== key);
    error.value = null;
  }

  function agregarPago(monto: number, metodoPago: IMetodoPago | null, moneda: IMoneda | null, referencia = '') {
    const montoNormalizado = Number(monto ?? 0);
    const saldoActual = Number(venta.value.saldo ?? 0);
    const referenciaNormalizada = referencia.trim();

    if (!Number.isFinite(montoNormalizado) || montoNormalizado <= 0) {
      error.value = 'El monto debe ser mayor que 0';
      return;
    }

    if (saldoActual <= EPSILON) {
      error.value = 'No hay saldo pendiente para registrar pagos';
      return;
    }

    if (montoNormalizado - saldoActual > EPSILON) {
      error.value = 'El pago no puede superar el saldo pendiente';
      return;
    }

    if (metodoPago?.requiereReferencia && !referenciaNormalizada) {
      error.value = `La referencia es obligatoria para ${metodoPago.descripcion ?? metodoPago.codigo ?? 'el metodo de pago seleccionado'}`;
      return;
    }

    error.value = null;
    pagos.value.push({
      _key: `tmp-pago-${Date.now()}`,
      monto: montoNormalizado,
      fecha: new Date(),
      referencia: referenciaNormalizada,
      metodoPago,
      moneda,
      guardado: false,
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
      throw new Error('La fecha de la venta es obligatoria');
    }

    if (detalles.value.length === 0) {
      throw new Error('Debe agregar al menos un vehiculo');
    }

    if (detalles.value.some(detalle => !detalle.vehiculo?.id)) {
      throw new Error('Todos los detalles deben tener un vehiculo asociado');
    }

    if (detalles.value.some((detalle, index) => detalles.value.findIndex(item => item.vehiculo.id === detalle.vehiculo.id) !== index)) {
      throw new Error('No se puede registrar el mismo vehiculo mas de una vez');
    }

    if (detalles.value.some(detalle => detalle.vehiculo.condicion === 'VENDIDO')) {
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
      throw new Error('Todos los detalles deben tener importes validos');
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

    for (const pago of pagos.value) {
      if (!Number.isFinite(Number(pago.monto)) || Number(pago.monto) <= 0) {
        throw new Error('Todos los pagos deben tener un monto mayor a 0');
      }

      if (pago.metodoPago?.requiereReferencia && !pago.referencia.trim()) {
        throw new Error(
          `La referencia es obligatoria para ${pago.metodoPago.descripcion ?? pago.metodoPago.codigo ?? 'el metodo de pago seleccionado'}`,
        );
      }
    }

    if (sumaPagos.value - total > EPSILON) {
      throw new Error('Los pagos no pueden superar el total de la venta');
    }

    venta.value.estado = estadoCalculado.value;
  }

  async function guardarVenta(): Promise<IVenta> {
    validarVentaAntesDeGuardar();

    const payload = {
      id: venta.value.id,
      fecha: venta.value.fecha ?? new Date(),
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
    };

    const res = venta.value.id
      ? await axios.put<IVenta>(`api/ventas/${venta.value.id}`, payload)
      : await axios.post<IVenta>('api/ventas', payload);

    venta.value = { ...venta.value, ...res.data, estado: estadoCalculado.value };
    return res.data;
  }

  async function guardarDetalles(ventaId: number) {
    for (const detalle of detalles.value.filter(item => !item.guardado)) {
      const payload = {
        id: detalle.id,
        precioUnitario: detalle.precioUnitario,
        cantidad: detalle.cantidad,
        subtotal: detalle.subtotal,
        venta: { id: ventaId },
        vehiculo: { id: detalle.vehiculo.id },
      };

      const res = detalle.id
        ? await axios.put(`api/detalle-ventas/${detalle.id}`, payload)
        : await axios.post('api/detalle-ventas', payload);

      detalle.id = res.data.id;
      detalle.guardado = true;
    }
  }

  async function guardarPagos(ventaId: number) {
    for (const pago of pagos.value.filter(item => !item.guardado)) {
      const payload = {
        id: pago.id,
        monto: pago.monto,
        fecha: pago.fecha,
        referencia: pago.referencia || null,
        venta: { id: ventaId },
        metodoPago: pago.metodoPago ? { id: pago.metodoPago.id } : null,
        moneda: pago.moneda ? { id: pago.moneda.id } : null,
      };

      const res = pago.id ? await axios.put(`api/pagos/${pago.id}`, payload) : await axios.post(`api/pagos`, payload);
      pago.id = res.data.id;
      pago.guardado = true;
    }
  }

  async function generarComprobante(ventaId: number, tipoComprobante: ITipoComprobante, moneda: IMoneda | null): Promise<IComprobante> {
    const numero = `0001-${String(ventaId).padStart(8, '0')}`;
    const payload: Partial<IComprobante> = {
      numeroComprobante: numero,
      fechaEmision: new Date(),
      importeNeto: venta.value.importeNeto,
      impuesto: venta.value.impuesto,
      total: venta.value.total,
      venta: { id: ventaId } as IComprobante['venta'],
      tipoComprobante: { id: tipoComprobante.id } as IComprobante['tipoComprobante'],
      moneda: moneda ? ({ id: moneda.id } as IComprobante['moneda']) : null,
    };

    const res = await axios.post<IComprobante>('api/comprobantes', payload);
    return res.data;
  }

  async function confirmar(tipoComprobante?: ITipoComprobante): Promise<{ venta: IVenta; comprobante?: IComprobante }> {
    guardando.value = true;
    error.value = null;

    try {
      validarVentaAntesDeGuardar();
      const ventaGuardada = await guardarVenta();
      await guardarDetalles(ventaGuardada.id!);
      await guardarPagos(ventaGuardada.id!);

      let comprobante: IComprobante | undefined;
      if (tipoComprobante) {
        comprobante = await generarComprobante(ventaGuardada.id!, tipoComprobante, venta.value.moneda ?? null);
      }

      return { venta: ventaGuardada, comprobante };
    } finally {
      guardando.value = false;
    }
  }

  async function cargarVenta(ventaId: number) {
    const [ventaRes, detallesRes, pagosRes] = await Promise.all([
      axios.get<IVenta>(`api/ventas/${ventaId}`),
      axios.get(`api/detalle-ventas?ventaId.equals=${ventaId}&page=0&size=50`),
      axios.get(`api/pagos?ventaId.equals=${ventaId}&page=0&size=50`),
    ]);

    venta.value = ventaRes.data;

    detalles.value = (detallesRes.data as IDetalleVenta[]).map(detalle => ({
      _key: `loaded-${detalle.id}`,
      id: detalle.id,
      vehiculo: detalle.vehiculo!,
      precioUnitario: Number(detalle.precioUnitario),
      cantidad: detalle.cantidad ?? 1,
      subtotal: Number(detalle.subtotal),
      guardado: true,
    }));

    pagos.value = (pagosRes.data as IPago[]).map(pago => ({
      _key: `loaded-${pago.id}`,
      id: pago.id,
      monto: Number(pago.monto),
      fecha: new Date(pago.fecha!),
      referencia: pago.referencia ?? '',
      metodoPago: pago.metodoPago ?? null,
      moneda: pago.moneda ?? null,
      guardado: true,
    }));

    recalcularSaldo();
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
    estadoCalculado,
    sumaSubtotales,
    sumaPagos,
    agregarVehiculo,
    actualizarPrecioDetalle,
    quitarDetalle,
    agregarPago,
    quitarPago,
    confirmar,
    cargarVenta,
    cargarCotizacionActiva,
    validarVentaAntesDeGuardar,
    fmt,
  };
}
