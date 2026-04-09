import { ref, computed, watch } from 'vue';
import axios from 'axios';
import type { IVenta } from '@/shared/model/venta.model';
import type { IDetalleVenta } from '@/shared/model/detalle-venta.model';
import type { IPago } from '@/shared/model/pago.model';
import type { IComprobante } from '@/shared/model/comprobante.model';
import type { IVehiculo } from '@/shared/model/vehiculo.model';
import type { ICliente } from '@/shared/model/cliente.model';
import type { IMoneda } from '@/shared/model/moneda.model';
import type { ICotizacion } from '@/shared/model/cotizacion.model';
import type { IMetodoPago } from '@/shared/model/metodo-pago.model';
import type { ITipoComprobante } from '@/shared/model/tipo-comprobante.model';
import { EstadoVenta } from '@/shared/model/estado-venta.model';

// ─── Tipos internos ────────────────────────────────────────────────────────────

export interface DetalleLocal {
  _key: string;           // clave temporal para v-for antes de persistir
  id?: number;            // id real si ya fue guardado
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

// ─── Composable ────────────────────────────────────────────────────────────────

export function useVentaEditor() {

  // Estado principal de la venta
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

  const detalles  = ref<DetalleLocal[]>([]);
  const pagos     = ref<PagoLocal[]>([]);
  const guardando = ref(false);
  const error     = ref<string | null>(null);

  // Catálogos
  const clientes         = ref<ICliente[]>([]);
  const monedas          = ref<IMoneda[]>([]);
  const metodoPagos      = ref<IMetodoPago[]>([]);
  const tipoComprobantes = ref<ITipoComprobante[]>([]);
  const cotizacionActiva  = ref<ICotizacion | null>(null);

  // ─── Cálculos reactivos ──────────────────────────────────────────────────────

  const sumaSubtotales = computed(() =>
    detalles.value.reduce((acc, d) => acc + d.subtotal, 0),
  );

  const sumaPagos = computed(() =>
    pagos.value.reduce((acc, p) => acc + p.monto, 0),
  );

  // Recalcular venta cuando cambian detalles o % impuesto
  watch([sumaSubtotales, () => venta.value.porcentajeImpuesto], () => {
    const neto = sumaSubtotales.value;
    const pct  = Number(venta.value.porcentajeImpuesto ?? 0) / 100;
    const imp  = neto * pct;
    venta.value.importeNeto = neto;
    venta.value.impuesto    = imp;
    venta.value.total       = neto + imp;
    recalcularSaldo();
  });

  // Recalcular saldo cuando cambian pagos
  watch(sumaPagos, () => recalcularSaldo());

  function recalcularSaldo() {
    venta.value.totalPagado = sumaPagos.value;
    venta.value.saldo = Math.max(0, (venta.value.total ?? 0) - sumaPagos.value);


    if (venta.value.saldo === 0 && detalles.value.length > 0) {
      venta.value.estado = EstadoVenta.PAGADA;
    } else if (sumaPagos.value > 0) {
      venta.value.estado = EstadoVenta.RESERVADA;
    } else {
      venta.value.estado = EstadoVenta.PENDIENTE;
    }
  }

  // ─── Cotización ──────────────────────────────────────────────────────────────

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

  watch(() => venta.value.moneda, (m) => {
    if (m?.id) cargarCotizacionActiva(m.id);
  });

  // ─── Detalles (vehículos) ────────────────────────────────────────────────────

  function agregarVehiculo(vehiculo: IVehiculo) {
    // Verificar que no esté ya en la lista
    if (detalles.value.some(d => d.vehiculo.id === vehiculo.id)) {
      error.value = `El vehículo ${vehiculo.patente} ya está en la venta`;
      return;
    }
    if (vehiculo.condicion === 'VENDIDO') {
      error.value = `El vehículo ${vehiculo.patente} ya fue vendido`;
      return;
    }
    error.value = null;
    const precio = Number(vehiculo.precio ?? 0);
    detalles.value.push({
      _key:           `tmp-${Date.now()}-${vehiculo.id}`,
      vehiculo,
      precioUnitario: precio,
      cantidad:       1,
      subtotal:       precio,
      guardado:       false,
    });
  }

  function actualizarPrecioDetalle(key: string, nuevoPrecio: number) {
    const d = detalles.value.find(x => x._key === key);
    if (d) {
      d.precioUnitario = nuevoPrecio;
      d.subtotal       = nuevoPrecio * d.cantidad;
      d.guardado       = false;
    }
  }

  function quitarDetalle(key: string) {
    detalles.value = detalles.value.filter(d => d._key !== key);
  }

  // ─── Pagos ───────────────────────────────────────────────────────────────────

  function agregarPago(monto: number, metodoPago: IMetodoPago | null, moneda: IMoneda | null, referencia = '') {
    if (monto <= 0) { error.value = 'El monto debe ser mayor que 0'; return; }
    error.value = null;
    pagos.value.push({
      _key:       `tmp-pago-${Date.now()}`,
      monto,
      fecha:      new Date(),
      referencia,
      metodoPago,
      moneda,
      guardado:   false,
    });
  }

  function quitarPago(key: string) {
    pagos.value = pagos.value.filter(p => p._key !== key);
  }

  // ─── Persistencia ────────────────────────────────────────────────────────────

  async function guardarVenta(): Promise<IVenta> {
    const payload = {
      fecha: venta.value.fecha ?? new Date(),
      estado: venta.value.estado ?? EstadoVenta.PENDIENTE,
      cotizacion: venta.value.cotizacion,
      porcentajeImpuesto: venta.value.porcentajeImpuesto,
      importeNeto: venta.value.importeNeto,
      impuesto: venta.value.impuesto,
      total: venta.value.total,
      totalPagado: venta.value.totalPagado,
      saldo: venta.value.saldo,


      cliente: venta.value.cliente ? { id: venta.value.cliente.id } : null,
      moneda: venta.value.moneda ? { id: venta.value.moneda.id } : null,
    };
    const res = venta.value.id
      ? await axios.put<IVenta>(`api/ventas/${venta.value.id}`, payload)
      : await axios.post<IVenta>('api/ventas', payload);
    venta.value = { ...venta.value, ...res.data };
    return res.data;
  }

  async function guardarDetalles(ventaId: number) {
    for (const d of detalles.value.filter(x => !x.guardado)) {
      const payload = {
        id:             d.id,
        precioUnitario: d.precioUnitario,
        cantidad:       d.cantidad,
        subtotal:       d.subtotal,
        venta:          { id: ventaId },
        vehiculo:       { id: d.vehiculo.id },
      };
      const res = d.id
        ? await axios.put(`api/detalle-ventas/${d.id}`, payload)
        : await axios.post('api/detalle-ventas', payload);
      d.id      = res.data.id;
      d.guardado = true;
    }
  }

  async function guardarPagos(ventaId: number) {
    for (const p of pagos.value.filter(x => !x.guardado)) {
      const payload = {
        id:         p.id,
        monto:      p.monto,
        fecha:      p.fecha,
        referencia: p.referencia || null,
        venta:      { id: ventaId },
        metodoPago: p.metodoPago ? { id: p.metodoPago.id } : null,
        moneda:     p.moneda     ? { id: p.moneda.id }     : null,
      };
      const res = p.id
        ? await axios.put(`api/pagos/${p.id}`, payload)
        : await axios.post('api/pagos', payload);
      p.id      = res.data.id;
      p.guardado = true;
    }
  }

  async function generarComprobante(ventaId: number, tipoComprobante: ITipoComprobante, moneda: IMoneda | null): Promise<IComprobante> {
    const numero = `0001-${String(ventaId).padStart(8, '0')}`;
    const payload: Partial<IComprobante> = {
      numeroComprobante: numero,
      fechaEmision:      new Date(),
      importeNeto:       venta.value.importeNeto,
      impuesto:          venta.value.impuesto,
      total:             venta.value.total,
      venta:             { id: ventaId } as any,
      tipoComprobante:   { id: tipoComprobante.id } as any,
      moneda:            moneda ? { id: moneda.id } as any : null,
    };
    const res = await axios.post<IComprobante>('api/comprobantes', payload);
    return res.data;
  }

  // ─── Flujo completo ──────────────────────────────────────────────────────────

  async function confirmar(tipoComprobante?: ITipoComprobante): Promise<{ venta: IVenta; comprobante?: IComprobante }> {
    if (detalles.value.length === 0) throw new Error('Debe agregar al menos un vehículo');
    if (!venta.value.cliente?.id)    throw new Error('Debe seleccionar un cliente');

    guardando.value = true;
    error.value     = null;

    try {
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

  // ─── Cargar venta existente ──────────────────────────────────────────────────

  async function cargarVenta(ventaId: number) {
    const [ventaRes, detallesRes, pagosRes] = await Promise.all([
      axios.get<IVenta>(`api/ventas/${ventaId}`),
      axios.get(`api/detalle-ventas?ventaId.equals=${ventaId}&page=0&size=50`),
      axios.get(`api/pagos?ventaId.equals=${ventaId}&page=0&size=50`),
    ]);

    venta.value = ventaRes.data;

    detalles.value = (detallesRes.data as IDetalleVenta[]).map(d => ({
      _key:           `loaded-${d.id}`,
      id:             d.id,
      vehiculo:       d.vehiculo!,
      precioUnitario: Number(d.precioUnitario),
      cantidad:       d.cantidad ?? 1,
      subtotal:       Number(d.subtotal),
      guardado:       true,
    }));

    pagos.value = (pagosRes.data as IPago[]).map(p => ({
      _key:       `loaded-${p.id}`,
      id:         p.id,
      monto:      Number(p.monto),
      fecha:      new Date(p.fecha!),
      referencia: p.referencia ?? '',
      metodoPago: p.metodoPago ?? null,
      moneda:     p.moneda ?? null,
      guardado:   true,
    }));
  }

  // ─── Helpers de formato ──────────────────────────────────────────────────────

  const fmt = (n?: number | null) =>
    Number(n ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  return {
    // Estado
    venta, detalles, pagos, guardando, error,
    // Catálogos
    clientes, monedas, metodoPagos, tipoComprobantes, cotizacionActiva,
    // Computed
    sumaSubtotales, sumaPagos,
    // Acciones detalles
    agregarVehiculo, actualizarPrecioDetalle, quitarDetalle,
    // Acciones pagos
    agregarPago, quitarPago,
    // Flujo
    confirmar, cargarVenta, cargarCotizacionActiva,
    // Utils
    fmt,
  };
}
