<template>
  <div>
    <b-dropdown-item to="/cliente" active-class="active">Clientes</b-dropdown-item>
    <b-dropdown-item to="/venta" active-class="active">Ventas</b-dropdown-item>
    <b-dropdown-item to="/reserva" active-class="active">Reservas</b-dropdown-item>
    <b-dropdown-item to="/tasacion-usados" active-class="active">Tasaciones</b-dropdown-item>
    <b-dropdown-item to="/inventario" active-class="active">Inventario</b-dropdown-item>
    <b-dropdown-item to="/vehiculo/buscar" active-class="active">Buscar vehiculo</b-dropdown-item>
    <b-dropdown-item to="/pago" active-class="active">Pagos</b-dropdown-item>
    <b-dropdown-item to="/comprobante" active-class="active">Comprobantes</b-dropdown-item>

    <template v-if="isAdmin">
      <b-dropdown-divider />
      <b-dropdown-item to="/movimiento-caja" active-class="active">Caja</b-dropdown-item>
      <b-dropdown-item to="/metodo-pago" active-class="active">Metodos de pago</b-dropdown-item>
      <b-dropdown-item to="/moneda" active-class="active">Monedas</b-dropdown-item>
      <b-dropdown-item to="/cotizacion" active-class="active">Cotizaciones</b-dropdown-item>
      <b-dropdown-item to="/admin/catalogo-tecnico" active-class="active">Catalogo tecnico</b-dropdown-item>
      <b-dropdown-item to="/admin/version-compatibilities" active-class="active">Compatibilidades</b-dropdown-item>
      <b-dropdown-item to="/tipo-documento" active-class="active">Tipos de documento</b-dropdown-item>
      <b-dropdown-item to="/condicion-iva" active-class="active">Condiciones IVA</b-dropdown-item>
    </template>
  </div>
</template>

<script lang="ts" src="./entities-menu.component.ts"></script>
