<template>
  <div>
    <h2 id="page-heading">
      <span>Caja</span>
      <div class="d-flex justify-content-end gap-2">
        <button class="btn btn-info" @click="retrieveMovimientos" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching" />
          <span>Actualizar</span>
        </button>
      </div>
    </h2>

    <div class="card mb-3">
      <div class="card-body">
        <div class="row g-2 align-items-end">
          <div class="col-12 col-md-2">
            <label class="form-label">Desde</label>
            <input v-model="filtros.fechaDesde" type="date" class="form-control" />
          </div>
          <div class="col-12 col-md-2">
            <label class="form-label">Hasta</label>
            <input v-model="filtros.fechaHasta" type="date" class="form-control" />
          </div>
          <div class="col-12 col-md-2">
            <label class="form-label">Usuario</label>
            <input v-model="filtros.usuario" type="text" class="form-control" />
          </div>
          <div class="col-12 col-md-2">
            <label class="form-label">Método</label>
            <select v-model.number="filtros.metodoPagoId" class="form-select">
              <option :value="null">Todos</option>
              <option v-for="m in metodosPago" :key="m.id" :value="m.id">{{ m.descripcion ?? m.codigo }}</option>
            </select>
          </div>
          <div class="col-12 col-md-2">
            <label class="form-label">Entidad</label>
            <select v-model.number="filtros.entidadFinancieraId" class="form-select">
              <option :value="null">Todas</option>
              <option v-for="e in entidadesFinancieras" :key="e.id" :value="e.id">{{ e.nombre }}</option>
            </select>
          </div>
          <div class="col-12 col-md-1">
            <label class="form-label">Tipo</label>
            <select v-model="filtros.tipo" class="form-select">
              <option value="">Todos</option>
              <option value="INGRESO">Ingreso</option>
              <option value="REVERSO">Reverso</option>
              <option value="INFORMATIVO">Informativo</option>
            </select>
          </div>
          <div class="col-12 col-md-1">
            <label class="form-label">Estado</label>
            <select v-model="filtros.estado" class="form-select">
              <option value="">Todos</option>
              <option value="REGISTRADO">Registrado</option>
              <option value="ANULADO">Anulado</option>
            </select>
          </div>
          <div class="col-12 d-flex gap-2 mt-2">
            <button class="btn btn-primary" @click="aplicarFiltros">Aplicar</button>
            <button class="btn btn-outline-secondary" @click="limpiarFiltros">Limpiar</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card mb-3">
      <div class="card-body">
        <div class="d-flex flex-wrap gap-3 align-items-end">
          <div>
            <label class="form-label">Resumen fecha</label>
            <input v-model="filtros.fechaResumen" type="date" class="form-control" />
          </div>
          <button class="btn btn-outline-primary" @click="retrieveResumen">Actualizar resumen</button>
        </div>
        <div class="row mt-3" v-if="resumen">
          <div class="col-12 col-md-4"><strong>Ingresos:</strong> {{ resumen.totalIngresos ?? 0 }}</div>
          <div class="col-12 col-md-4"><strong>Reversos:</strong> {{ resumen.totalReversos ?? 0 }}</div>
          <div class="col-12 col-md-4"><strong>Neto:</strong> {{ resumen.neto ?? 0 }}</div>
        </div>
      </div>
    </div>

    <div class="alert alert-warning" v-if="!isFetching && movimientos.length === 0">No se encontraron movimientos de caja</div>

    <div class="table-responsive" v-if="movimientos.length > 0">
      <table class="table table-striped">
        <thead>
          <tr>
            <th @click="changeOrder('id')">ID</th>
            <th @click="changeOrder('fecha')">Fecha</th>
            <th @click="changeOrder('usuario')">Usuario</th>
            <th @click="changeOrder('tipoMovimiento')">Tipo</th>
            <th>Método</th>
            <th>Entidad</th>
            <th>Monto original</th>
            <th>Moneda</th>
            <th>Cotización</th>
            <th>Aplicado ARS</th>
            <th>Estado</th>
            <th>Referencia</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="mov in movimientos" :key="mov.id">
            <td>{{ mov.id }}</td>
            <td>{{ formatDateShort(mov.fecha) }}</td>
            <td>{{ mov.usuario }}</td>
            <td>
              <span
                class="badge"
                :class="mov.tipoMovimiento === 'INGRESO' ? 'bg-success' : mov.tipoMovimiento === 'REVERSO' ? 'bg-danger' : 'bg-secondary'"
              >
                {{ mov.tipoMovimiento }}
              </span>
            </td>
            <td>{{ mov.metodoPago?.descripcion ?? mov.metodoPago?.codigo ?? '-' }}</td>
            <td>{{ mov.entidadFinanciera?.nombre ?? '-' }}</td>
            <td class="text-end">{{ mov.montoOriginal ?? 0 }}</td>
            <td>{{ mov.moneda?.codigo ?? '-' }}</td>
            <td class="text-end">{{ mov.cotizacionUsada ?? '-' }}</td>
            <td class="text-end">{{ mov.montoAplicadoArs ?? 0 }}</td>
            <td>
              <span class="badge" :class="mov.estado === 'ANULADO' ? 'bg-warning text-dark' : 'bg-primary'">{{ mov.estado }}</span>
            </td>
            <td>{{ mov.referencia ?? '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" src="./movimiento-caja.component.ts"></script>
