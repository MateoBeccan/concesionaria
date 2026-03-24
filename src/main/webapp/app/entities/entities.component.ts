import { defineComponent, provide } from 'vue';

import UserService from '@/entities/user/user.service';

import AutoService from './auto/auto.service';
import ClienteService from './cliente/cliente.service';
import CombustibleService from './combustible/combustible.service';
import ComprobanteService from './comprobante/comprobante.service';
import CondicionIvaService from './condicion-iva/condicion-iva.service';
import MetodoPagoService from './metodo-pago/metodo-pago.service';
import MonedaService from './moneda/moneda.service';
import CotizacionService from './cotizacion/cotizacion.service';
import ConfiguracionAutoService from './configuracion-auto/configuracion-auto.service';
import DetalleVentaService from './detalle-venta/detalle-venta.service';
import EstadoVentaService from './estado-venta/estado-venta.service';
import MarcaService from './marca/marca.service';
import ModeloService from './modelo/modelo.service';
import MotorService from './motor/motor.service';
import PagoService from './pago/pago.service';
import Prueba1Service from './prueba-1/prueba-1.service';
import TipoComprobanteService from './tipo-comprobante/tipo-comprobante.service';
import VentaService from './venta/venta.service';
import VersionService from './version/version.service';
// jhipster-needle-add-entity-service-to-entities-component-import - JHipster will import entities services here

export default defineComponent({
  name: 'Entities',
  setup() {
    provide('userService', () => new UserService());
    provide('prueba1Service', () => new Prueba1Service());
    provide('marcaService', () => new MarcaService());
    provide('modeloService', () => new ModeloService());
    provide('versionService', () => new VersionService());
    provide('motorService', () => new MotorService());
    provide('combustibleService', () => new CombustibleService());
    provide('autoService', () => new AutoService());
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
    provide('configuracionAutoService', () => new ConfiguracionAutoService());
    // jhipster-needle-add-entity-service-to-entities-component - JHipster will import entities services here
  },
});
