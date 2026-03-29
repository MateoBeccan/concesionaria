import { defineComponent, provide } from 'vue';

import UserService from '@/entities/user/user.service';

import CarroceriaService from './carroceria/carroceria.service';
import ClienteService from './cliente/cliente.service';
import CombustibleService from './combustible/combustible.service';
import DetalleVentaService from './detalle-venta/detalle-venta.service';
import ComprobanteService from './comprobante/comprobante.service';
import CondicionIvaService from './condicion-iva/condicion-iva.service';
import TipoComprobanteService from './tipo-comprobante/tipo-comprobante.service';
import MetodoPagoService from './metodo-pago/metodo-pago.service';
import MonedaService from './moneda/moneda.service';
import CotizacionService from './cotizacion/cotizacion.service';
import EstadoVentaService from './estado-venta/estado-venta.service';
import TipoCajaService from './tipo-caja/tipo-caja.service';
import TipoVehiculoService from './tipo-vehiculo/tipo-vehiculo.service';
import VehiculoService from './vehiculo/vehiculo.service';
import InventarioService from './inventario/inventario.service';
import MarcaService from './marca/marca.service';
import ModeloService from './modelo/modelo.service';
import MotorService from './motor/motor.service';
import PagoService from './pago/pago.service';
import TipoDocumentoService from './tipo-documento/tipo-documento.service';
import TraccionService from './traccion/traccion.service';
import VentaService from './venta/venta.service';
import VersionService from './version/version.service';
// jhipster-needle-add-entity-service-to-entities-component-import - JHipster will import entities services here

export default defineComponent({
  name: 'Entities',
  setup() {
    provide('userService', () => new UserService());
    provide('marcaService', () => new MarcaService());
    provide('modeloService', () => new ModeloService());
    provide('versionService', () => new VersionService());
    provide('motorService', () => new MotorService());
    provide('combustibleService', () => new CombustibleService());
    provide('clienteService', () => new ClienteService());
    provide('condicionIvaService', () => new CondicionIvaService());
    provide('estadoVentaService', () => new EstadoVentaService());
    provide('ventaService', () => new VentaService());
    provide('detalleVentaService', () => new DetalleVentaService());
    provide('comprobanteService', () => new ComprobanteService());
    provide('tipoComprobanteService', () => new TipoComprobanteService());
    provide('pagoService', () => new PagoService());
    provide('metodoPagoService', () => new MetodoPagoService());
    provide('monedaService', () => new MonedaService());
    provide('cotizacionService', () => new CotizacionService());
    provide('carroceriaService', () => new CarroceriaService());
    provide('tipoCajaService', () => new TipoCajaService());
    provide('traccionService', () => new TraccionService());
    provide('tipoVehiculoService', () => new TipoVehiculoService());
    provide('vehiculoService', () => new VehiculoService());
    provide('inventarioService', () => new InventarioService());
    provide('tipoDocumentoService', () => new TipoDocumentoService());
    // jhipster-needle-add-entity-service-to-entities-component - JHipster will import entities services here
  },
});
