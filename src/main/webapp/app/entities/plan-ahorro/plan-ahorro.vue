<template>
  <div class="container-fluid px-0">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
          <h2 class="h4 mb-1">Planes de Ahorro</h2>
          <p class="text-muted mb-0">Gestión comercial de planes vigentes y configuración base.</p>
        </div>
        <button class="btn btn-primary" @click="openCreate">Nuevo plan</button>
      </div>
    </div>

    <div class="table-responsive card">
      <table class="table mb-0">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Versión objetivo</th>
            <th>Cuotas</th>
            <th>Valor móvil</th>
            <th>Moneda</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="plan in planes" :key="plan.id">
            <td>{{ plan.nombre }}</td>
            <td>{{ plan.versionObjetivo?.nombre ?? '-' }}</td>
            <td>{{ plan.cantidadCuotas }}</td>
            <td>{{ plan.valorMovil }}</td>
            <td>{{ plan.moneda?.codigo }}</td>
            <td>
              <span class="badge" :class="plan.estado === 'ACTIVO' ? 'bg-success' : 'bg-secondary'">{{ plan.estado }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showCreate" class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Crear plan</h5>
            <button type="button" class="btn-close" @click="showCreate = false"></button>
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
            <div class="row g-2">
              <div class="col-6">
                <label class="form-label">Cuotas</label>
                <input v-model.number="draft.cantidadCuotas" type="number" min="1" class="form-control" />
              </div>
              <div class="col-6">
                <label class="form-label">Valor móvil</label>
                <input v-model.number="draft.valorMovil" type="number" min="0.01" step="0.01" class="form-control" />
              </div>
            </div>
            <div class="mt-2">
              <label class="form-label">Moneda</label>
              <select v-model.number="draft.monedaId" class="form-select">
                <option :value="null">Seleccionar</option>
                <option v-for="mon in monedas" :key="mon.id" :value="mon.id">{{ mon.codigo }} - {{ mon.descripcion }}</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-outline-secondary" @click="showCreate = false">Cancelar</button>
            <button class="btn btn-primary" @click="save">Guardar</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showCreate" class="modal-backdrop fade show"></div>
  </div>
</template>

<script lang="ts" src="./plan-ahorro.component.ts"></script>

