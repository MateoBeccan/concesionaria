<template>
  <div class="container-fluid px-0">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex justify-content-between align-items-center">
        <div>
          <p class="section-kicker mb-1">Planes de ahorro</p>
          <h1 class="h4 mb-1">Reglas de adjudicación</h1>
          <p class="text-muted mb-0">Definí condiciones comerciales reutilizables por plan.</p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="badge bg-success">Activas {{ activeCount }}</span>
          <button class="btn btn-primary" @click="newRegla">Nueva regla</button>
        </div>
      </div>
    </div>

    <div class="table-responsive card border-0 shadow-sm">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Nombre</th>
            <th>Tipo</th>
            <th>Mín. cuotas</th>
            <th>Mín. %</th>
            <th>Mora</th>
            <th>Estado</th>
            <th class="text-end">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="regla in reglas" :key="regla.id">
            <td>{{ regla.nombre }}</td>
            <td><span class="badge bg-primary-subtle text-primary-emphasis">{{ regla.tipoRegla }}</span></td>
            <td>{{ regla.minimoCuotas ?? '-' }}</td>
            <td>{{ regla.minimoPorcentaje ?? '-' }}</td>
            <td>{{ regla.permiteMora ? 'Sí' : 'No' }}</td>
            <td><span class="badge" :class="regla.activo ? 'bg-success' : 'bg-secondary'">{{ regla.activo ? 'ACTIVA' : 'INACTIVA' }}</span></td>
            <td class="text-end">
              <button class="btn btn-sm btn-outline-primary me-2" @click="editRegla(regla)">Editar</button>
              <button class="btn btn-sm btn-outline-danger" :disabled="!regla.activo" @click="deactivate(regla)">Desactivar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editing ? 'Editar regla' : 'Nueva regla' }}</h5>
            <button type="button" class="btn-close" @click="showForm = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-2">
              <label class="form-label">Nombre</label>
              <input v-model="draft.nombre" class="form-control" />
            </div>
            <div class="mb-2">
              <label class="form-label">Descripción</label>
              <textarea v-model="draft.descripcion" class="form-control" rows="2"></textarea>
            </div>
            <div class="mb-2">
              <label class="form-label">Tipo</label>
              <select v-model="draft.tipoRegla" class="form-select">
                <option v-for="t in TIPOS" :key="t" :value="t">{{ t }}</option>
              </select>
            </div>
            <div class="row g-2">
              <div class="col">
                <label class="form-label">Mínimo cuotas</label>
                <input v-model.number="draft.minimoCuotas" type="number" min="1" class="form-control" />
              </div>
              <div class="col">
                <label class="form-label">Mínimo porcentaje</label>
                <input v-model.number="draft.minimoPorcentaje" type="number" min="0.01" max="100" step="0.01" class="form-control" />
              </div>
            </div>
            <div class="form-check mt-2">
              <input id="permiteMora" v-model="draft.permiteMora" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="permiteMora">Permite adjudicación en mora</label>
            </div>
            <div class="form-check mt-1">
              <input id="requiereActivo" v-model="draft.requiereContratoActivo" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="requiereActivo">Requiere contrato en estado habilitado</label>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-outline-secondary" @click="showForm = false">Cancelar</button>
            <button class="btn btn-primary" @click="save">Guardar</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showForm" class="modal-backdrop fade show"></div>
  </div>
</template>

<script lang="ts" src="./regla-adjudicacion-plan.component.ts"></script>

<style scoped>
.section-kicker {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #64748b;
  font-weight: 700;
}
</style>
