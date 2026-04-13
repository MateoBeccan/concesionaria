<template>
  <div class="container py-4" style="max-width: 920px">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ inventario.id ? 'Editar inventario' : 'Nuevo inventario' }}</h1>
        <p class="page-subtitle">
          {{ inventario.id ? 'Ajustá la disponibilidad real de la unidad sin romper reservas ni ventas.' : 'Registrá una unidad en inventario con reglas de negocio consistentes.' }}
        </p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="previousState()">Volver</button>
    </div>

    <form @submit.prevent="save()" novalidate>
      <div class="card mb-3">
        <div class="card-header">1. Situación actual</div>
        <div class="card-body">
          <div class="row g-3 align-items-end">
            <div class="col-md-5">
              <label class="form-label">Estado de inventario <span class="text-danger">*</span></label>
              <select
                v-model="v$.estadoInventario.$model"
                class="form-select"
                :class="{ 'is-invalid': v$.estadoInventario.$dirty && v$.estadoInventario.$invalid }"
              >
                <option :value="null">Seleccionar</option>
                <option v-for="estado in estadoInventarioValues" :key="estado" :value="estado">{{ estado }}</option>
              </select>
              <div class="invalid-feedback">
                <span v-for="error of v$.estadoInventario.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-3">
              <label class="form-label">Disponibilidad operativa</label>
              <div class="form-control bg-light d-flex align-items-center justify-content-between">
                <span>{{ inventario.disponible ? 'Disponible' : 'No disponible' }}</span>
                <span class="badge" :class="inventario.disponible ? 'bg-success' : 'bg-secondary'">
                  {{ inventario.disponible ? 'SI' : 'NO' }}
                </span>
              </div>
              <small class="text-muted">Se calcula automáticamente según el estado elegido.</small>
            </div>

            <div class="col-md-4">
              <label class="form-label">Resumen</label>
              <div class="form-control bg-light d-flex align-items-center justify-content-between">
                <span>{{ stateSummaryLabel }}</span>
                <span class="badge" :class="stateSummaryClass">{{ inventario.estadoInventario ?? 'SIN_ESTADO' }}</span>
              </div>
            </div>

            <div class="col-md-4">
              <label class="form-label">Fecha de ingreso <span class="text-danger">*</span></label>
              <input
                id="inventario-fechaIngreso"
                type="datetime-local"
                class="form-control"
                :class="{ 'is-invalid': v$.fechaIngreso.$dirty && v$.fechaIngreso.$invalid }"
                :value="convertDateTimeFromServer(v$.fechaIngreso.$model)"
                @change="updateInstantField('fechaIngreso', $event)"
              />
              <div class="invalid-feedback">
                <span v-for="error of v$.fechaIngreso.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-8">
              <label class="form-label">Ubicación</label>
              <input
                v-model="v$.ubicacion.$model"
                type="text"
                class="form-control"
                :class="{ 'is-invalid': v$.ubicacion.$dirty && v$.ubicacion.$invalid }"
                placeholder="Salón principal, depósito, playa externa..."
              />
              <div class="invalid-feedback">
                <span v-for="error of v$.ubicacion.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">2. Unidad asociada</div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-7">
              <label class="form-label">Vehículo <span class="text-danger">*</span></label>
              <select v-model="v$.vehiculo.$model" class="form-select" :class="{ 'is-invalid': v$.vehiculo.$dirty && v$.vehiculo.$invalid }">
                <option :value="null">Seleccionar unidad</option>
                <option
                  v-for="vehiculoOption in vehiculos"
                  :key="vehiculoOption.id"
                  :value="inventario.vehiculo && vehiculoOption.id === inventario.vehiculo.id ? inventario.vehiculo : vehiculoOption"
                >
                  {{ vehiculoLabel(vehiculoOption) }}
                </option>
              </select>
              <div class="invalid-feedback">
                <span v-for="error of v$.vehiculo.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-5">
              <label class="form-label">Condición comercial esperada</label>
              <div class="form-control bg-light d-flex align-items-center justify-content-between">
                <span>{{ expectedCondicion ?? 'Sin definir' }}</span>
                <span class="badge bg-dark">{{ selectedVehiculo?.condicion ?? 'ACTUAL SIN DATO' }}</span>
              </div>
              <small class="text-muted">Al guardar, el sistema sincroniza la condición comercial del vehículo.</small>
            </div>

            <div class="col-md-12" v-if="selectedVehiculo">
              <div class="rounded border bg-light p-3">
                <div class="fw-semibold mb-1">Unidad seleccionada</div>
                <div>{{ selectedVehiculo.patente || 'Sin patente asignada' }}</div>
                <div class="text-muted small">ID {{ selectedVehiculo.id }} · Condición actual {{ selectedVehiculo.condicion ?? 'Sin condición' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">3. Reserva</div>
        <div class="card-body">
          <div v-if="isReservado" class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Cliente de la reserva <span class="text-danger">*</span></label>
              <select
                v-model="v$.clienteReserva.$model"
                class="form-select"
                :class="{ 'is-invalid': v$.clienteReserva.$dirty && v$.clienteReserva.$invalid }"
              >
                <option :value="null">Seleccionar cliente</option>
                <option
                  v-for="clienteOption in clientes"
                  :key="clienteOption.id"
                  :value="inventario.clienteReserva && clienteOption.id === inventario.clienteReserva.id ? inventario.clienteReserva : clienteOption"
                >
                  {{ clienteLabel(clienteOption) }}
                </option>
              </select>
              <div class="invalid-feedback">
                <span v-for="error of v$.clienteReserva.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-3">
              <label class="form-label">Fecha de reserva <span class="text-danger">*</span></label>
              <input
                type="datetime-local"
                class="form-control"
                :class="{ 'is-invalid': v$.fechaReserva.$dirty && v$.fechaReserva.$invalid }"
                :value="convertDateTimeFromServer(v$.fechaReserva.$model)"
                @change="updateInstantField('fechaReserva', $event)"
              />
              <div class="invalid-feedback">
                <span v-for="error of v$.fechaReserva.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-3">
              <label class="form-label">Vence el <span class="text-danger">*</span></label>
              <input
                type="datetime-local"
                class="form-control"
                :class="{ 'is-invalid': v$.fechaVencimientoReserva.$dirty && v$.fechaVencimientoReserva.$invalid }"
                :value="convertDateTimeFromServer(v$.fechaVencimientoReserva.$model)"
                @change="updateInstantField('fechaVencimientoReserva', $event)"
              />
              <div class="invalid-feedback">
                <span v-for="error of v$.fechaVencimientoReserva.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>

            <div class="col-md-12" v-if="selectedCliente">
              <div class="rounded border bg-light p-3">
                <div class="fw-semibold mb-1">Reserva asignada</div>
                <div>{{ clienteLabel(selectedCliente) }}</div>
                <div class="text-muted small">La unidad permanecerá fuera de venta hasta liberar o vencer la reserva.</div>
              </div>
            </div>
          </div>

          <div v-else class="text-muted">
            Este estado no requiere datos de reserva. Si había una reserva cargada, se limpiará automáticamente al guardar.
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">4. Observaciones</div>
        <div class="card-body">
          <textarea
            v-model="v$.observaciones.$model"
            class="form-control"
            rows="3"
            :class="{ 'is-invalid': v$.observaciones.$dirty && v$.observaciones.$invalid }"
            placeholder="Notas operativas, ubicación física o aclaraciones de la reserva..."
          />
          <div class="invalid-feedback">
            <span v-for="error of v$.observaciones.$errors" :key="error.$uid">{{ error.$message }}</span>
          </div>
        </div>
      </div>

      <div v-if="businessErrors.length" class="alert alert-danger">
        <div class="fw-semibold mb-1">No se puede guardar el inventario</div>
        <div v-for="error in businessErrors" :key="error">{{ error }}</div>
      </div>

      <div v-if="businessWarnings.length" class="alert alert-warning">
        <div class="fw-semibold mb-1">Revisá estas alertas</div>
        <div v-for="warning in businessWarnings" :key="warning">{{ warning }}</div>
      </div>

      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-outline-secondary" @click="previousState()">Cancelar</button>
        <button type="submit" class="btn btn-primary" :disabled="v$.$invalid || isSaving || !canSubmit || loadingRelations">
          <span v-if="isSaving" class="spinner-border spinner-border-sm me-1" />
          {{ inventario.id ? 'Guardar cambios' : 'Crear inventario' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script lang="ts" src="./inventario-update.component.ts"></script>
