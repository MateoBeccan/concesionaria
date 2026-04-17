<template>
  <div class="container py-4" style="max-width: 920px">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ inventario.id ? 'Editar inventario' : 'Nuevo inventario' }}</h1>
        <p class="page-subtitle">
          {{
            inventario.id
              ? 'Ajusta la disponibilidad real de la unidad sin romper reservas ni ventas.'
              : 'Registra una unidad en inventario con reglas de negocio consistentes.'
          }}
        </p>
      </div>
      <button class="btn btn-sm btn-outline-secondary" @click="previousState()">Volver</button>
    </div>

    <section class="process-strip mb-4">
      <article v-for="step in procesoInventario" :key="step.number" class="process-step" :class="{ done: step.done, current: step.current }">
        <div class="process-number">{{ step.number }}</div>
        <div>
          <div class="process-title">{{ step.title }}</div>
          <div class="process-copy">{{ step.copy }}</div>
        </div>
      </article>
    </section>

    <form @submit.prevent="save()" novalidate>
      <div class="card mb-3">
        <div class="card-header">
          <div>
            <div class="section-title">1. Estado operativo</div>
            <div class="section-copy">Defini si la unidad esta disponible, reservada o vendida. La disponibilidad se deriva automaticamente del estado.</div>
          </div>
        </div>
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
                <span>{{ inventario.estadoInventario === 'DISPONIBLE' ? 'Disponible' : 'No disponible' }}</span>
                <span class="badge" :class="inventario.estadoInventario === 'DISPONIBLE' ? 'bg-success' : 'bg-secondary'">
                  {{ inventario.estadoInventario === 'DISPONIBLE' ? 'SI' : 'NO' }}
                </span>
              </div>
              <small class="text-muted">Se calcula automaticamente segun el estado elegido.</small>
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
              <label class="form-label">Ubicacion</label>
              <input
                v-model="v$.ubicacion.$model"
                type="text"
                class="form-control"
                :class="{ 'is-invalid': v$.ubicacion.$dirty && v$.ubicacion.$invalid }"
                placeholder="Salon principal, deposito, playa externa..."
              />
              <div class="invalid-feedback">
                <span v-for="error of v$.ubicacion.$errors" :key="error.$uid">{{ error.$message }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">
          <div>
            <div class="section-title">2. Unidad asociada</div>
            <div class="section-copy">Elegi la unidad correcta y verifica que la condicion comercial quede alineada con el inventario.</div>
          </div>
        </div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-7">
              <label class="form-label">Vehiculo <span class="text-danger">*</span></label>
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
              <label class="form-label">Condicion comercial esperada</label>
              <div class="form-control bg-light d-flex align-items-center justify-content-between">
                <span>{{ expectedCondicion ?? 'Sin definir' }}</span>
                <span class="badge bg-dark">{{ selectedVehiculo?.condicion ?? 'ACTUAL SIN DATO' }}</span>
              </div>
              <small class="text-muted">Al guardar, el sistema sincroniza la condicion comercial del vehiculo.</small>
            </div>

            <div class="col-md-12" v-if="selectedVehiculo">
              <div class="rounded border bg-light p-3">
                <div class="fw-semibold mb-1">Unidad seleccionada</div>
                <div>{{ selectedVehiculo.patente || 'Sin patente asignada' }}</div>
                <div class="text-muted small">ID {{ selectedVehiculo.id }} · Condicion actual {{ selectedVehiculo.condicion ?? 'Sin condicion' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">
          <div>
            <div class="section-title">3. Reserva</div>
            <div class="section-copy">Solo cuando el estado sea reservado se solicitan cliente, fecha de reserva y vencimiento.</div>
          </div>
        </div>
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
                <div class="text-muted small">La unidad permanecera fuera de venta hasta liberar o vencer la reserva.</div>
              </div>
            </div>
          </div>

          <div v-else class="text-muted">
            Este estado no requiere datos de reserva. Si habia una reserva cargada, se limpiara automaticamente al guardar.
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <div class="card-header">
          <div>
            <div class="section-title">4. Validacion y observaciones</div>
            <div class="section-copy">Agrega notas operativas y revisa alertas antes de guardar el inventario.</div>
          </div>
        </div>
        <div class="card-body">
          <textarea
            v-model="v$.observaciones.$model"
            class="form-control"
            rows="3"
            :class="{ 'is-invalid': v$.observaciones.$dirty && v$.observaciones.$invalid }"
            placeholder="Notas operativas, ubicacion fisica o aclaraciones de la reserva..."
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
        <div class="fw-semibold mb-1">Revisa estas alertas</div>
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

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
}

.page-title {
  margin: 0;
  font-size: 1.45rem;
  font-weight: 700;
  color: #0f172a;
}

.page-subtitle {
  margin: 0.35rem 0 0;
  color: #64748b;
}

.card {
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.card-header {
  padding: 1rem 1.15rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.card-body {
  padding: 1.15rem;
}

.process-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
}

.process-step {
  display: flex;
  gap: 0.8rem;
  padding: 0.9rem 1rem;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: #fff;
}

.process-step.current {
  border-color: #93c5fd;
  background: #f8fbff;
}

.process-step.done {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.process-number {
  width: 2rem;
  height: 2rem;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: #e2e8f0;
  color: #334155;
  font-size: 0.78rem;
  font-weight: 700;
  flex-shrink: 0;
}

.process-step.current .process-number {
  background: #2563eb;
  color: #fff;
}

.process-step.done .process-number {
  background: #16a34a;
  color: #fff;
}

.process-title,
.section-title {
  font-weight: 700;
  color: #0f172a;
}

.process-copy,
.section-copy {
  font-size: 0.84rem;
  color: #64748b;
}

.section-copy {
  margin-top: 0.2rem;
}

.form-control,
.form-select {
  min-height: 42px;
}

textarea.form-control {
  min-height: 96px;
}

.rounded.border.bg-light.p-3 {
  border-color: #e2e8f0 !important;
  background: #f8fafc !important;
}

@media (max-width: 991px) {
  .process-strip {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 575px) {
  .page-header {
    flex-direction: column;
  }

  .process-strip {
    grid-template-columns: 1fr;
  }
}
</style>
