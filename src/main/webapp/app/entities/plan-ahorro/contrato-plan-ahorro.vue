<template>
  <div class="container-fluid px-0">
    <div class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
          <h2 class="h4 mb-1">Contratos de Plan</h2>
          <p class="text-muted mb-0">Alta y seguimiento de contratos activos de planes de ahorro.</p>
        </div>
        <button class="btn btn-primary" @click="openCreate">Nuevo contrato</button>
      </div>
    </div>

    <div class="table-responsive card">
      <table class="table mb-0">
        <thead>
          <tr>
            <th>Número</th>
            <th>Cliente</th>
            <th>Plan</th>
            <th>Estado</th>
            <th>Pagadas</th>
            <th>Saldo</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in contratos" :key="c.id">
            <td>{{ c.numeroContrato }}</td>
            <td>{{ c.cliente?.apellido }} {{ c.cliente?.nombre }}</td>
            <td>{{ c.plan?.nombre }}</td>
            <td><span class="badge bg-primary">{{ c.estado }}</span></td>
            <td>{{ c.cuotasPagadas }}/{{ c.cuotasTotales }}</td>
            <td>{{ c.saldoPendiente }}</td>
            <td class="text-end">
              <router-link :to="{ name: 'ContratoPlanAhorroView', params: { contratoId: c.id } }" class="btn btn-sm btn-outline-primary"
                >Ver</router-link
              >
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showCreate" class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Crear contrato</h5>
            <button type="button" class="btn-close" @click="showCreate = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-2">
              <label class="form-label">Cliente</label>
              <select v-model.number="draft.clienteId" class="form-select">
                <option :value="null">Seleccionar</option>
                <option v-for="cli in clientes" :key="cli.id" :value="cli.id">{{ cli.apellido }} {{ cli.nombre }}</option>
              </select>
            </div>
            <div class="mb-2">
              <label class="form-label">Plan</label>
              <select v-model.number="draft.planId" class="form-select">
                <option :value="null">Seleccionar</option>
                <option v-for="plan in planes" :key="plan.id" :value="plan.id">{{ plan.nombre }}</option>
              </select>
            </div>
            <div class="mb-2">
              <label class="form-label">Observaciones</label>
              <textarea v-model="draft.observaciones" class="form-control" rows="2"></textarea>
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

<script lang="ts" src="./contrato-plan-ahorro.component.ts"></script>

