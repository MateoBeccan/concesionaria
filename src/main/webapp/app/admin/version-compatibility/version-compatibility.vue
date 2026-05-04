<template>
  <div class="compatibility-admin container-fluid py-4">
    <div class="row g-4">
      <div class="col-12">
        <div class="page-header">
          <div>
            <p class="section-label mb-1">Administración</p>
            <h1 class="h3 mb-1">Compatibilidades versión - motor</h1>
            <p class="text-muted mb-0">Elegí una versión, revisá sus motores compatibles y administralos desde un flujo único.</p>
          </div>
          <button class="btn btn-outline-secondary" @click="reloadAll" :disabled="loadingVersions || loadingMotors || saving">
            <font-awesome-icon icon="sync" :spin="loadingVersions || loadingMotors || saving" />
            <span class="ms-2">Actualizar</span>
          </button>
        </div>
      </div>

      <div class="col-12 col-xl-4">
        <section class="admin-card h-100">
          <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
            <div>
              <h2 class="h5 mb-1">Versiones</h2>
              <p class="text-muted mb-0">Buscá una versión por marca, modelo o nombre comercial.</p>
            </div>
            <span class="badge text-bg-light">{{ filteredVersions.length }}</span>
          </div>

          <input v-model="versionSearch" type="search" class="form-control mb-3" placeholder="Buscar versión" />

          <div class="version-list" v-if="filteredVersions.length > 0">
            <button
              v-for="version in filteredVersions"
              :key="version.id"
              type="button"
              class="version-item"
              :class="{ active: selectedVersion?.id === version.id }"
              @click="selectVersion(version)"
            >
              <span class="version-title">{{ versionLabel(version) }}</span>
              <span class="version-meta">{{ versionYears(version) }}</span>
            </button>
          </div>

          <div v-else class="empty-state">
            <strong>No hay versiones para mostrar.</strong>
            <span>Probá con otro criterio de búsqueda.</span>
          </div>
        </section>
      </div>

      <div class="col-12 col-xl-8">
        <section class="admin-card h-100">
          <div class="d-flex justify-content-between align-items-start gap-3 mb-4">
            <div>
              <h2 class="h5 mb-1">Motores compatibles</h2>
              <p class="text-muted mb-0" v-if="selectedVersion">{{ versionLabel(selectedVersion) }}</p>
              <p class="text-muted mb-0" v-else>Seleccioná una versión para administrar compatibilidades.</p>
            </div>
            <span v-if="selectedVersion" class="badge text-bg-primary">{{ assignedMotors.length }} asignados</span>
          </div>

          <template v-if="selectedVersion">
            <div class="row g-3 align-items-end mb-4">
              <div class="col-12 col-lg-9">
                <label class="form-label">Agregar motor compatible</label>
                <select v-model="selectedMotorIdToAdd" class="form-select" :disabled="availableMotors.length === 0 || saving">
                  <option :value="null">Seleccionar motor</option>
                  <option v-for="motor in availableMotors" :key="motor.id" :value="motor.id">
                    {{ motorLabel(motor) }}
                  </option>
                </select>
              </div>
              <div class="col-12 col-lg-3">
                <button class="btn btn-primary w-100" @click="addCompatibility" :disabled="!selectedMotorIdToAdd || saving">
                  <font-awesome-icon icon="plus" />
                  <span class="ms-2">Agregar</span>
                </button>
              </div>
            </div>

            <div v-if="loadingMotors" class="empty-state">
              <strong>Cargando motores compatibles...</strong>
            </div>

            <div v-else-if="assignedMotors.length === 0" class="empty-state">
              <strong>Sin motores asignados.</strong>
              <span>Agregá compatibilidades para habilitar combinaciones válidas en vehículos.</span>
            </div>

            <div v-else class="motor-grid">
              <article v-for="motor in assignedMotors" :key="motor.id" class="motor-card">
                <div>
                  <h3 class="motor-name">{{ motor.nombre }}</h3>
                  <p class="motor-specs mb-1">{{ motorSummary(motor) }}</p>
                  <p class="motor-meta mb-0">{{ motorDetails(motor) }}</p>
                </div>
                <button class="btn btn-outline-danger btn-sm" @click="removeCompatibility(motor)" :disabled="saving">
                  Quitar
                </button>
              </article>
            </div>
          </template>

          <div v-else class="empty-state">
            <strong>Esperando selección de versión.</strong>
            <span>La gestión se ordena por versión para que el mantenimiento sea claro y rápido.</span>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, inject, onMounted, ref } from 'vue';

import type AlertService from '@/shared/alert/alert.service';
import { type IMotor } from '@/shared/model/motor.model';
import { type IVersion } from '@/shared/model/version.model';
import MotorService from '@/entities/motor/motor.service';
import VersionService from '@/entities/version/version.service';

const alertService = inject<AlertService>('alertService');
const versionService = new VersionService();
const motorService = new MotorService();

const versions = ref<IVersion[]>([]);
const allMotors = ref<IMotor[]>([]);
const assignedMotors = ref<IMotor[]>([]);
const selectedVersion = ref<IVersion | null>(null);
const selectedMotorIdToAdd = ref<number | null>(null);
const versionSearch = ref('');

const loadingVersions = ref(false);
const loadingMotors = ref(false);
const saving = ref(false);

const versionLabel = (version: IVersion) => [version.modelo?.marca?.nombre, version.modelo?.nombre, version.nombre].filter(Boolean).join(' / ');
const versionYears = (version: IVersion) => (version.anioFin ? `${version.anioInicio} - ${version.anioFin}` : `Desde ${version.anioInicio}`);
const motorSummary = (motor: IMotor) => [`${motor.cilindradaCc ?? '-'} cc`, `${motor.potenciaHp ?? '-'} HP`].join(' · ');
const motorDetails = (motor: IMotor) =>
  [motor.combustible?.nombre, motor.tipoCaja?.nombre, motor.traccion?.nombre, motor.turbo ? 'Turbo' : 'Aspirado'].filter(Boolean).join(' · ');
const motorLabel = (motor: IMotor) => [motor.nombre, motorSummary(motor)].filter(Boolean).join(' - ');

const filteredVersions = computed(() => {
  const term = versionSearch.value.trim().toLowerCase();
  if (!term) {
    return versions.value;
  }
  return versions.value.filter(version => versionLabel(version).toLowerCase().includes(term));
});

const assignedMotorIds = computed(() => new Set(assignedMotors.value.map(motor => motor.id).filter(Boolean)));
const availableMotors = computed(() =>
  allMotors.value.filter(motor => motor.id && !assignedMotorIds.value.has(motor.id)).sort((a, b) => motorLabel(a).localeCompare(motorLabel(b))),
);

const loadVersions = async () => {
  loadingVersions.value = true;
  try {
    const response = await versionService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] });
    versions.value = response.data ?? [];

    if (!selectedVersion.value && versions.value.length > 0) {
      selectedVersion.value = versions.value[0];
    } else if (selectedVersion.value) {
      selectedVersion.value = versions.value.find(version => version.id === selectedVersion.value?.id) ?? null;
    }
  } catch (error: any) {
    alertService?.showHttpError(error.response);
  } finally {
    loadingVersions.value = false;
  }
};

const loadMotors = async () => {
  try {
    const response = await motorService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] });
    allMotors.value = response.data ?? [];
  } catch (error: any) {
    alertService?.showHttpError(error.response);
  }
};

const loadAssignedMotors = async (versionId: number) => {
  loadingMotors.value = true;
  selectedMotorIdToAdd.value = null;
  try {
    assignedMotors.value = await versionService.retrieveMotors(versionId);
  } catch (error: any) {
    assignedMotors.value = [];
    alertService?.showHttpError(error.response);
  } finally {
    loadingMotors.value = false;
  }
};

const selectVersion = async (version: IVersion) => {
  selectedVersion.value = version;
  if (version.id) {
    await loadAssignedMotors(version.id);
  }
};

const addCompatibility = async () => {
  if (!selectedVersion.value?.id || !selectedMotorIdToAdd.value) {
    return;
  }

  saving.value = true;
  try {
    assignedMotors.value = await versionService.addMotorCompatibility(selectedVersion.value.id, selectedMotorIdToAdd.value);
    selectedMotorIdToAdd.value = null;
    alertService?.showSuccess('Compatibilidad agregada correctamente');
  } catch (error: any) {
    alertService?.showHttpError(error.response);
  } finally {
    saving.value = false;
  }
};

const removeCompatibility = async (motor: IMotor) => {
  if (!selectedVersion.value?.id || !motor.id) {
    return;
  }

  saving.value = true;
  try {
    assignedMotors.value = await versionService.removeMotorCompatibility(selectedVersion.value.id, motor.id);
    alertService?.showSuccess('Compatibilidad eliminada correctamente');
  } catch (error: any) {
    alertService?.showHttpError(error.response);
  } finally {
    saving.value = false;
  }
};

const reloadAll = async () => {
  await Promise.all([loadVersions(), loadMotors()]);
  if (selectedVersion.value?.id) {
    await loadAssignedMotors(selectedVersion.value.id);
  }
};

onMounted(async () => {
  await Promise.all([loadVersions(), loadMotors()]);
  if (selectedVersion.value?.id) {
    await loadAssignedMotors(selectedVersion.value.id);
  }
});
</script>

<style scoped>
.compatibility-admin {
  max-width: 1440px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.section-label {
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6c757d;
}

.admin-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 1rem;
  padding: 1.25rem;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05);
}

.version-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 560px;
  overflow-y: auto;
}

.version-item {
  width: 100%;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  border-radius: 0.85rem;
  padding: 0.9rem 1rem;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  transition: all 0.18s ease;
}

.version-item:hover,
.version-item.active {
  border-color: #0d6efd;
  background: #eef5ff;
}

.version-title {
  font-weight: 600;
  color: #111827;
}

.version-meta {
  font-size: 0.9rem;
  color: #6b7280;
}

.motor-grid {
  display: grid;
  gap: 0.9rem;
}

.motor-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.9rem;
  padding: 1rem;
  background: #fafafa;
}

.motor-name {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 0.35rem;
  color: #111827;
}

.motor-specs {
  color: #334155;
}

.motor-meta {
  font-size: 0.92rem;
  color: #6b7280;
}

.empty-state {
  min-height: 220px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 0.4rem;
  text-align: center;
  color: #6b7280;
  border: 1px dashed #d1d5db;
  border-radius: 0.9rem;
  padding: 1.5rem;
  background: #fcfcfd;
}

@media (max-width: 991px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .motor-card {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
