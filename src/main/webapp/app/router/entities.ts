import { Authority } from '@/shared/jhipster/constants';
const Entities = () => import('@/entities/entities.vue');

const Marca = () => import('@/entities/marca/marca.vue');
const MarcaUpdate = () => import('@/entities/marca/marca-update.vue');
const MarcaDetails = () => import('@/entities/marca/marca-details.vue');

const Modelo = () => import('@/entities/modelo/modelo.vue');
const ModeloUpdate = () => import('@/entities/modelo/modelo-update.vue');
const ModeloDetails = () => import('@/entities/modelo/modelo-details.vue');

const Version = () => import('@/entities/version/version.vue');
const VersionUpdate = () => import('@/entities/version/version-update.vue');
const VersionDetails = () => import('@/entities/version/version-details.vue');

const Motor = () => import('@/entities/motor/motor.vue');
const MotorUpdate = () => import('@/entities/motor/motor-update.vue');
const MotorDetails = () => import('@/entities/motor/motor-details.vue');

const Combustible = () => import('@/entities/combustible/combustible.vue');
const CombustibleUpdate = () => import('@/entities/combustible/combustible-update.vue');
const CombustibleDetails = () => import('@/entities/combustible/combustible-details.vue');

const Cliente = () => import('@/entities/cliente/cliente.vue');
const ClienteUpdate = () => import('@/entities/cliente/cliente-update.vue');
const ClienteDetails = () => import('@/entities/cliente/cliente-details.vue');

const CondicionIva = () => import('@/entities/condicion-iva/condicion-iva.vue');
const CondicionIvaUpdate = () => import('@/entities/condicion-iva/condicion-iva-update.vue');
const CondicionIvaDetails = () => import('@/entities/condicion-iva/condicion-iva-details.vue');

const Venta = () => import('@/entities/venta/venta-list.vue');
const VentaDetails = () => import('@/entities/venta/venta-details.vue');

const DetalleVenta = () => import('@/entities/detalle-venta/detalle-venta.vue');
const DetalleVentaUpdate = () => import('@/entities/detalle-venta/detalle-venta-update.vue');
const DetalleVentaDetails = () => import('@/entities/detalle-venta/detalle-venta-details.vue');

const Comprobante = () => import('@/entities/comprobante/comprobante.vue');
const ComprobanteUpdate = () => import('@/entities/comprobante/comprobante-update.vue');
const ComprobanteDetails = () => import('@/entities/comprobante/comprobante-details.vue');

const TipoComprobante = () => import('@/entities/tipo-comprobante/tipo-comprobante.vue');
const TipoComprobanteUpdate = () => import('@/entities/tipo-comprobante/tipo-comprobante-update.vue');
const TipoComprobanteDetails = () => import('@/entities/tipo-comprobante/tipo-comprobante-details.vue');

const Pago = () => import('@/entities/pago/pago.vue');
const PagoUpdate = () => import('@/entities/pago/pago-update.vue');
const PagoDetails = () => import('@/entities/pago/pago-details.vue');

const MetodoPago = () => import('@/entities/metodo-pago/metodo-pago.vue');
const MetodoPagoUpdate = () => import('@/entities/metodo-pago/metodo-pago-update.vue');
const MetodoPagoDetails = () => import('@/entities/metodo-pago/metodo-pago-details.vue');

const Moneda = () => import('@/entities/moneda/moneda.vue');
const MonedaUpdate = () => import('@/entities/moneda/moneda-update.vue');
const MonedaDetails = () => import('@/entities/moneda/moneda-details.vue');

const Cotizacion = () => import('@/entities/cotizacion/cotizacion.vue');
const CotizacionUpdate = () => import('@/entities/cotizacion/cotizacion-update.vue');
const CotizacionDetails = () => import('@/entities/cotizacion/cotizacion-details.vue');

const Carroceria = () => import('@/entities/carroceria/carroceria.vue');
const CarroceriaUpdate = () => import('@/entities/carroceria/carroceria-update.vue');
const CarroceriaDetails = () => import('@/entities/carroceria/carroceria-details.vue');

const TipoCaja = () => import('@/entities/tipo-caja/tipo-caja.vue');
const TipoCajaUpdate = () => import('@/entities/tipo-caja/tipo-caja-update.vue');
const TipoCajaDetails = () => import('@/entities/tipo-caja/tipo-caja-details.vue');

const Traccion = () => import('@/entities/traccion/traccion.vue');
const TraccionUpdate = () => import('@/entities/traccion/traccion-update.vue');
const TraccionDetails = () => import('@/entities/traccion/traccion-details.vue');

const TipoVehiculo = () => import('@/entities/tipo-vehiculo/tipo-vehiculo.vue');
const TipoVehiculoUpdate = () => import('@/entities/tipo-vehiculo/tipo-vehiculo-update.vue');
const TipoVehiculoDetails = () => import('@/entities/tipo-vehiculo/tipo-vehiculo-details.vue');

const Vehiculo = () => import('@/entities/vehiculo/vehiculo.vue');
const VehiculoUpdate = () => import('@/entities/vehiculo/vehiculo-update.vue');
const VehiculoDetails = () => import('@/entities/vehiculo/vehiculo-details.vue');
const VentaEditor = () => import('@/entities/venta/VentaEditor.vue');

const Inventario = () => import('@/entities/inventario/inventario.vue');
const InventarioUpdate = () => import('@/entities/inventario/inventario-update.vue');
const InventarioDetails = () => import('@/entities/inventario/inventario-details.vue');

const TipoDocumento = () => import('@/entities/tipo-documento/tipo-documento.vue');
const TipoDocumentoUpdate = () => import('@/entities/tipo-documento/tipo-documento-update.vue');
const TipoDocumentoDetails = () => import('@/entities/tipo-documento/tipo-documento-details.vue');

// jhipster-needle-add-entity-to-router-import - JHipster will import entities to the router here

export default {
  path: '/',
  component: Entities,
  children: [
    {
      path: 'marca',
      name: 'Marca',
      component: Marca,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'marca/new',
      name: 'MarcaCreate',
      component: MarcaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'marca/:marcaId/edit',
      name: 'MarcaEdit',
      component: MarcaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'marca/:marcaId/view',
      name: 'MarcaView',
      component: MarcaDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'modelo',
      name: 'Modelo',
      component: Modelo,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'modelo/new',
      name: 'ModeloCreate',
      component: ModeloUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'modelo/:modeloId/edit',
      name: 'ModeloEdit',
      component: ModeloUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'modelo/:modeloId/view',
      name: 'ModeloView',
      component: ModeloDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'version',
      name: 'Version',
      component: Version,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'version/new',
      name: 'VersionCreate',
      component: VersionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'version/:versionId/edit',
      name: 'VersionEdit',
      component: VersionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'version/:versionId/view',
      name: 'VersionView',
      component: VersionDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'motor',
      name: 'Motor',
      component: Motor,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'motor/new',
      name: 'MotorCreate',
      component: MotorUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'motor/:motorId/edit',
      name: 'MotorEdit',
      component: MotorUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'motor/:motorId/view',
      name: 'MotorView',
      component: MotorDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'combustible',
      name: 'Combustible',
      component: Combustible,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'combustible/new',
      name: 'CombustibleCreate',
      component: CombustibleUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'combustible/:combustibleId/edit',
      name: 'CombustibleEdit',
      component: CombustibleUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'combustible/:combustibleId/view',
      name: 'CombustibleView',
      component: CombustibleDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'cliente',
      name: 'Cliente',
      component: Cliente,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'cliente/new',
      name: 'ClienteCreate',
      component: ClienteUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'cliente/:clienteId/edit',
      name: 'ClienteEdit',
      component: ClienteUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'cliente/:clienteId/view',
      name: 'ClienteView',
      component: ClienteDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'condicion-iva',
      name: 'CondicionIva',
      component: CondicionIva,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'condicion-iva/new',
      name: 'CondicionIvaCreate',
      component: CondicionIvaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'condicion-iva/:condicionIvaId/edit',
      name: 'CondicionIvaEdit',
      component: CondicionIvaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'condicion-iva/:condicionIvaId/view',
      name: 'CondicionIvaView',
      component: CondicionIvaDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'venta',
      name: 'Venta',
      component: Venta,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'venta/new',
      name: 'VentaCreate',
      component: VentaEditor,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'venta/:ventaId/edit',
      name: 'VentaEdit',
      component: VentaEditor,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'venta/:ventaId/view',
      name: 'VentaView',
      component: VentaDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'detalle-venta',
      name: 'DetalleVenta',
      component: DetalleVenta,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'detalle-venta/new',
      name: 'DetalleVentaCreate',
      component: DetalleVentaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'detalle-venta/:detalleVentaId/edit',
      name: 'DetalleVentaEdit',
      component: DetalleVentaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'detalle-venta/:detalleVentaId/view',
      name: 'DetalleVentaView',
      component: DetalleVentaDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'comprobante',
      name: 'Comprobante',
      component: Comprobante,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'comprobante/new',
      name: 'ComprobanteCreate',
      component: ComprobanteUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'comprobante/:comprobanteId/edit',
      name: 'ComprobanteEdit',
      component: ComprobanteUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'comprobante/:comprobanteId/view',
      name: 'ComprobanteView',
      component: ComprobanteDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-comprobante',
      name: 'TipoComprobante',
      component: TipoComprobante,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-comprobante/new',
      name: 'TipoComprobanteCreate',
      component: TipoComprobanteUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-comprobante/:tipoComprobanteId/edit',
      name: 'TipoComprobanteEdit',
      component: TipoComprobanteUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-comprobante/:tipoComprobanteId/view',
      name: 'TipoComprobanteView',
      component: TipoComprobanteDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'pago',
      name: 'Pago',
      component: Pago,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'pago/new',
      name: 'PagoCreate',
      component: PagoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'pago/:pagoId/edit',
      name: 'PagoEdit',
      component: PagoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'pago/:pagoId/view',
      name: 'PagoView',
      component: PagoDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'metodo-pago',
      name: 'MetodoPago',
      component: MetodoPago,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'metodo-pago/new',
      name: 'MetodoPagoCreate',
      component: MetodoPagoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'metodo-pago/:metodoPagoId/edit',
      name: 'MetodoPagoEdit',
      component: MetodoPagoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'metodo-pago/:metodoPagoId/view',
      name: 'MetodoPagoView',
      component: MetodoPagoDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'moneda',
      name: 'Moneda',
      component: Moneda,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'moneda/new',
      name: 'MonedaCreate',
      component: MonedaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'moneda/:monedaId/edit',
      name: 'MonedaEdit',
      component: MonedaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'moneda/:monedaId/view',
      name: 'MonedaView',
      component: MonedaDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'cotizacion',
      name: 'Cotizacion',
      component: Cotizacion,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'cotizacion/new',
      name: 'CotizacionCreate',
      component: CotizacionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'cotizacion/:cotizacionId/edit',
      name: 'CotizacionEdit',
      component: CotizacionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'cotizacion/:cotizacionId/view',
      name: 'CotizacionView',
      component: CotizacionDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'carroceria',
      name: 'Carroceria',
      component: Carroceria,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'carroceria/new',
      name: 'CarroceriaCreate',
      component: CarroceriaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'carroceria/:carroceriaId/edit',
      name: 'CarroceriaEdit',
      component: CarroceriaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'carroceria/:carroceriaId/view',
      name: 'CarroceriaView',
      component: CarroceriaDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-caja',
      name: 'TipoCaja',
      component: TipoCaja,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-caja/new',
      name: 'TipoCajaCreate',
      component: TipoCajaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-caja/:tipoCajaId/edit',
      name: 'TipoCajaEdit',
      component: TipoCajaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-caja/:tipoCajaId/view',
      name: 'TipoCajaView',
      component: TipoCajaDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'traccion',
      name: 'Traccion',
      component: Traccion,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'traccion/new',
      name: 'TraccionCreate',
      component: TraccionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'traccion/:traccionId/edit',
      name: 'TraccionEdit',
      component: TraccionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'traccion/:traccionId/view',
      name: 'TraccionView',
      component: TraccionDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-vehiculo',
      name: 'TipoVehiculo',
      component: TipoVehiculo,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-vehiculo/new',
      name: 'TipoVehiculoCreate',
      component: TipoVehiculoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-vehiculo/:tipoVehiculoId/edit',
      name: 'TipoVehiculoEdit',
      component: TipoVehiculoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-vehiculo/:tipoVehiculoId/view',
      name: 'TipoVehiculoView',
      component: TipoVehiculoDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'vehiculo',
      name: 'Vehiculo',
      component: Vehiculo,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'vehiculo/new',
      name: 'VehiculoCreate',
      component: VehiculoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'vehiculo/:vehiculoId/edit',
      name: 'VehiculoEdit',
      component: VehiculoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'vehiculo/:vehiculoId/view',
      name: 'VehiculoView',
      component: VehiculoDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventario',
      name: 'Inventario',
      component: Inventario,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventario/new',
      name: 'InventarioCreate',
      component: InventarioUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventario/:inventarioId/edit',
      name: 'InventarioEdit',
      component: InventarioUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'inventario/:inventarioId/view',
      name: 'InventarioView',
      component: InventarioDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-documento',
      name: 'TipoDocumento',
      component: TipoDocumento,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-documento/new',
      name: 'TipoDocumentoCreate',
      component: TipoDocumentoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-documento/:tipoDocumentoId/edit',
      name: 'TipoDocumentoEdit',
      component: TipoDocumentoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'tipo-documento/:tipoDocumentoId/view',
      name: 'TipoDocumentoView',
      component: TipoDocumentoDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: '/vehiculo/buscar',
      name: 'VehiculoSearch',
      component: () => import('@/entities/vehiculo/vehiculo-search.vue'),
      meta: { authorities: [Authority.USER] },
    },
    {
      path: '/ventas',
      name: 'VentaList',
      component: Venta,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: '/venta/nueva',
      name: 'VentaWizard',
      redirect: { name: 'VentaEditor' },
      meta: { authorities: [Authority.USER] },
    },
    {
      path: '/venta/editor',
      name: 'VentaEditor',
      component: VentaEditor,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: '/venta/editor/:ventaId',
      name: 'VentaEditorEdit',
      component: VentaEditor,
      meta: { authorities: [Authority.USER] },
    },
    // jhipster-needle-add-entity-to-router - JHipster will add entities to the router here
  ],
};
