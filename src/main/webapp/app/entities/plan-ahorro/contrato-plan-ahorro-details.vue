<template>
  <div class="container-fluid px-0">
    <div class="card border-0 shadow-sm mb-3" v-if="contrato">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
          <h2 class="h4 mb-1">Contrato {{ contrato.numeroContrato }}</h2>
          <p class="text-muted mb-0">
            {{ contrato.cliente?.apellido }} {{ contrato.cliente?.nombre }} · {{ contrato.plan?.nombre }} · {{ contrato.estado }}
          </p>
        </div>
        <router-link :to="{ name: 'ContratoPlanAhorro' }" class="btn btn-outline-secondary">Volver</router-link>
      </div>
    </div>

    <div class="card">
      <div class="card-header"><strong>Cuotas</strong></div>
      <div class="table-responsive">
        <table class="table mb-0">
          <thead>
            <tr>
              <th>#</th>
              <th>Vencimiento</th>
              <th>Importe</th>
              <th>Estado</th>
              <th>Pago</th>
              <th>Comprobante</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="cuota in cuotas" :key="cuota.id">
              <td>{{ cuota.numeroCuota }}</td>
              <td>{{ formatDateShort(cuota.fechaVencimiento) }}</td>
              <td>{{ cuota.importe }}</td>
              <td><span class="badge" :class="cuota.estado === 'PAGADA' ? 'bg-success' : 'bg-secondary'">{{ cuota.estado }}</span></td>
              <td>{{ cuota.fechaPago ? formatDateShort(cuota.fechaPago) : '-' }}</td>
              <td>
                <div v-if="cuota.comprobantePlanAhorroId">
                  <span
                    class="badge me-2"
                    :class="cuota.estadoComprobantePlanAhorro === 'EMITIDO' ? 'bg-success' : 'bg-warning text-dark'"
                    >{{ cuota.estadoComprobantePlanAhorro }}</span
                  >
                  <small class="text-muted">{{ cuota.numeroComprobantePlanAhorro }}</small>
                </div>
                <span v-else class="text-muted">-</span>
              </td>
              <td class="text-end">
                <button v-if="cuota.estado === 'PENDIENTE' || cuota.estado === 'VENCIDA'" class="btn btn-sm btn-primary" @click="abrirPago(cuota)">
                  Pagar cuota
                </button>
                <button
                  v-if="cuota.comprobantePlanAhorroId"
                  class="btn btn-sm btn-outline-primary ms-2"
                  @click="descargarComprobante(cuota)"
                >
                  Descargar PDF
                </button>
                <button
                  v-if="cuota.comprobantePlanAhorroId && cuota.estadoComprobantePlanAhorro === 'EMITIDO'"
                  class="btn btn-sm btn-outline-danger ms-2"
                  @click="anularComprobante(cuota)"
                >
                  Anular comprobante
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showPay" class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Pagar cuota #{{ cuotaActiva?.numeroCuota }}</h5>
            <button type="button" class="btn-close" @click="showPay = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-2">
              <label class="form-label">Monto</label>
              <input v-model.number="pagoMonto" type="number" class="form-control" min="0.01" step="0.01" />
            </div>
            <div class="mb-2">
              <label class="form-label">Observaciones</label>
              <textarea v-model="pagoObservaciones" class="form-control" rows="2"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-outline-secondary" @click="showPay = false">Cancelar</button>
            <button class="btn btn-primary" @click="confirmarPago">Confirmar pago</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showPay" class="modal-backdrop fade show"></div>
  </div>
</template>

<script lang="ts" src="./contrato-plan-ahorro-details.component.ts"></script>
