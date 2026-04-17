<template>
  <div>
    <b-dropdown-item to="/vehiculo" active-class="active">
      <font-awesome-icon icon="asterisk" />
      <span>Vehiculos</span>
    </b-dropdown-item>
    <b-dropdown-item to="/inventario" active-class="active">
      <font-awesome-icon icon="asterisk" />
      <span>Inventario</span>
    </b-dropdown-item>
    <b-dropdown-item to="/cotizacion" active-class="active">
      <font-awesome-icon icon="asterisk" />
      <span>Cotizaciones</span>
    </b-dropdown-item>
    <b-dropdown-item to="/metodo-pago" active-class="active">
      <font-awesome-icon icon="asterisk" />
      <span>Metodos de pago</span>
    </b-dropdown-item>
    <b-dropdown-item to="/moneda" active-class="active">
      <font-awesome-icon icon="asterisk" />
      <span>Monedas</span>
    </b-dropdown-item>
    <b-dropdown-item to="/admin/catalogo-tecnico" active-class="active">
      <font-awesome-icon icon="cogs" />
      <span>Centro de catalogo tecnico</span>
    </b-dropdown-item>
    <b-dropdown-item to="/admin/version-compatibilities" active-class="active">
      <font-awesome-icon icon="cogs" />
      <span>Compatibilidades</span>
    </b-dropdown-item>
    <b-dropdown-item to="/tipo-documento" active-class="active">
      <font-awesome-icon icon="asterisk" />
      <span>Tipos de documento</span>
    </b-dropdown-item>
    <b-dropdown-item to="/condicion-iva" active-class="active">
      <font-awesome-icon icon="asterisk" />
      <span>Condiciones IVA</span>
    </b-dropdown-item>
  </div>
</template>

<script lang="ts" src="./entities-menu.component.ts"></script>
