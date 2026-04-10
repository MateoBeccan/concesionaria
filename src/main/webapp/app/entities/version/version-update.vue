<template>
  <div class="d-flex justify-content-center">
    <div class="col-8">

      <form @submit.prevent="save()">

        <h2 class="mb-4 fw-bold">
          {{ version.id ? 'Editar Versión' : 'Nueva Versión' }}
        </h2>

        <!-- ID -->
        <div class="mb-3" v-if="version.id">
          <label>ID</label>
          <input type="text" class="form-control" v-model="version.id" readonly />
        </div>

        <!-- NOMBRE -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Nombre</label>
          <input
            type="text"
            class="form-control"
            v-model="v$.nombre.$model"
            :class="{ valid: !v$.nombre.$invalid, invalid: v$.nombre.$invalid }"
            required
          />
        </div>

        <!-- DESCRIPCIÓN -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Descripción</label>
          <input
            type="text"
            class="form-control"
            v-model="v$.descripcion.$model"
          />
        </div>

        <!-- AÑO INICIO -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Año Inicio</label>
          <input
            type="number"
            class="form-control"
            v-model.number="v$.anioInicio.$model"
            required
          />
        </div>

        <!-- AÑO FIN -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Año Fin</label>
          <input
            type="number"
            class="form-control"
            v-model.number="v$.anioFin.$model"
          />
        </div>

        <!-- MODELO (🔥 FIX IMPORTANTE) -->
        <div class="mb-3">
          <label class="form-label fw-semibold">Modelo</label>

          <select class="form-control" v-model="version.modelo">

            <option :value="null" disabled>
              Seleccione un modelo
            </option>

            <option
              v-for="modeloOption in modelos"
              :key="modeloOption.id"
              :value="modeloOption"
            >
              {{ modeloOption.nombre }} ({{ modeloOption.marca?.nombre }})
            </option>

          </select>
        </div>

        <!-- BOTONES -->
        <div class="d-flex gap-2 mt-4">

          <button type="button" class="btn btn-secondary" @click="previousState()">
            Cancelar
          </button>

          <button
            type="submit"
            class="btn btn-primary"
            :disabled="v$.$invalid || isSaving"
          >
            Guardar
          </button>

        </div>

      </form>

    </div>
  </div>
</template>
<script lang="ts" src="./version-update.component.ts"></script>
