<template>
  <div class="d-flex justify-content-center">
    <div class="col-10 col-lg-8">
      <form @submit.prevent="save()">
        <div class="card shadow-sm p-4">

          <!-- HEADER -->
          <h3 class="mb-4">
            {{ cliente.id ? 'Editar Cliente' : 'Nuevo Cliente' }}
          </h3>

          <!-- DATOS -->
          <h5 class="border-bottom pb-2 mb-3">Datos personales</h5>

          <div class="row mb-3">
            <div class="col-md-6">
              <label>Nombre *</label>
              <input class="form-control" v-model="v$.nombre.$model" />
            </div>

            <div class="col-md-6">
              <label>Apellido *</label>
              <input class="form-control" v-model="v$.apellido.$model" />
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label>Documento *</label>
              <input class="form-control" v-model="v$.nroDocumento.$model" />
            </div>

            <div class="col-md-6">
              <label>Condición IVA</label>
              <select class="form-control" v-model="v$.condicionIva.$model">
                <option :value="null">Seleccione</option>
                <option v-for="opt in condicionIvas" :key="opt.id" :value="opt">
                  {{ opt.descripcion }}
                </option>
              </select>
            </div>
          </div>

          <!-- CONTACTO -->
          <h5 class="border-bottom pb-2 mb-3">Contacto</h5>

          <div class="row mb-3">
            <div class="col-md-6">
              <label>Teléfono</label>
              <input class="form-control" v-model="v$.telefono.$model" />
            </div>

            <div class="col-md-6">
              <label>Email *</label>
              <input class="form-control" v-model="v$.email.$model" />
            </div>
          </div>

          <div class="mb-3">
            <label>Dirección</label>
            <input class="form-control" v-model="v$.direccion.$model" />
          </div>

          <div class="row mb-3">
            <div class="col-md-4">
              <input class="form-control" placeholder="Ciudad" v-model="v$.ciudad.$model" />
            </div>
            <div class="col-md-4">
              <input class="form-control" placeholder="Provincia" v-model="v$.provincia.$model" />
            </div>
            <div class="col-md-4">
              <input class="form-control" placeholder="País" v-model="v$.pais.$model" />
            </div>
          </div>

          <!-- ESTADO -->
          <div class="form-check form-switch mb-3">
            <input type="checkbox" v-model="v$.activo.$model" class="form-check-input" />
            <label>Activo</label>
          </div>

          <!-- FECHAS (FIX SIMPLE) -->
          <div class="row mb-3">
            <div class="col-md-4">
              <label>Fecha Alta</label>
              <input type="datetime-local" class="form-control" v-model="v$.fechaAlta.$model" />
            </div>

            <div class="col-md-4">
              <label>Creado</label>
              <input type="datetime-local" class="form-control" v-model="v$.createdDate.$model" />
            </div>

            <div class="col-md-4">
              <label>Modificado</label>
              <input type="datetime-local" class="form-control" v-model="v$.lastModifiedDate.$model" />
            </div>
          </div>

          <!-- TIPO DOCUMENTO -->
          <div class="mb-3">
            <label>Tipo Documento</label>
            <select class="form-control" v-model="v$.tipoDocumento.$model">
              <option :value="null">Seleccione</option>
              <option v-for="opt in tipoDocumentos" :key="opt.id" :value="opt">
                {{ opt.descripcion }}
              </option>
            </select>
          </div>

          <!-- BOTONES -->
          <div class="d-flex justify-content-end gap-2 mt-4">
            <button type="button" class="btn btn-outline-secondary" @click="previousState()">
              Cancelar
            </button>

            <button type="submit" class="btn btn-primary">
              Guardar
            </button>
          </div>

        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./cliente-update.component.ts"></script>
