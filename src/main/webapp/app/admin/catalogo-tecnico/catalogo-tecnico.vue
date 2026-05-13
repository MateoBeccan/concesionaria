<template>
  <div class="catalog-page">
    <section class="card border-0 shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <p class="text-uppercase small text-muted fw-semibold mb-1">Catalogo tecnico</p>
          <h1 class="h4 mb-1">Estructura guiada</h1>
          <p class="text-muted mb-0">Navega relaciones y carga datos en secuencia: Marca -> Modelo -> Version.</p>
        </div>
        <button class="btn btn-outline-primary" @click="loadData" :disabled="isLoading">
          <font-awesome-icon icon="sync" :spin="isLoading" class="me-2" />
          Actualizar
        </button>
      </div>
    </section>

    <section class="row g-3">
      <div class="col-12 col-xl-5">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body p-0">
            <div class="hierarchy-header">
              <div>
                <h2 class="h6 text-uppercase text-muted mb-1">Jerarquia comercial</h2>
                <p class="small text-muted mb-0">Selecciona marca, luego modelo y version</p>
              </div>
              <input v-model.trim="searchTerm" class="form-control form-control-sm search-input" placeholder="Buscar..." type="text" />
            </div>

            <div class="hierarchy-scroll">
              <section v-for="marca in filteredMarcas" :key="marca.id" class="brand-card" :class="{ active: marca.id === selectedMarcaId }">
                <div class="brand-row">
                  <button class="entity-btn brand-btn" @click="selectMarca(marca.id)">
                    <span class="entity-left">
                      <span class="entity-dot"></span>
                      <span class="entity-name">{{ marca.nombre }}</span>
                    </span>
                    <span class="entity-badge">{{ getModelosByMarca(marca.id).length }}</span>
                  </button>
                  <button class="info-btn" @click="openHierarchyQuickInfo('marca', marca)" title="Vista rapida">
                    <svg class="info-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                      <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="1.8" />
                      <circle cx="12" cy="8.2" r="1.2" fill="currentColor" />
                      <path d="M12 11v5.2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                    </svg>
                  </button>
                </div>

                <div v-if="marca.id === selectedMarcaId" class="model-list">
                  <div v-for="modelo in getModelosByMarca(marca.id)" :key="modelo.id" class="model-card" :class="{ active: modelo.id === selectedModeloId }">
                    <div class="model-row">
                      <button class="entity-btn model-btn" @click="selectModelo(modelo.id)">
                        <span class="entity-left">
                          <span class="entity-dot"></span>
                          <span class="entity-name">{{ modelo.nombre }}</span>
                        </span>
                        <span class="entity-badge">{{ getVersionsByModelo(modelo.id).length }}</span>
                      </button>
                      <button class="info-btn" @click="openHierarchyQuickInfo('modelo', modelo)" title="Vista rapida">
                        <svg class="info-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                          <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="1.8" />
                          <circle cx="12" cy="8.2" r="1.2" fill="currentColor" />
                          <path d="M12 11v5.2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                        </svg>
                      </button>
                    </div>

                    <div v-if="selectedModeloId === modelo.id && getVersionsByModelo(modelo.id).length" class="version-list">
                      <div v-for="version in getVersionsByModelo(modelo.id)" :key="version.id" class="version-row" :class="{ active: version.id === selectedVersionId }">
                        <button class="entity-btn version-btn" @click="selectVersion(version.id)">
                          <span class="entity-left">
                            <span class="entity-dot"></span>
                            <span class="entity-name">{{ version.nombre }}</span>
                          </span>
                          <span class="entity-meta">{{ version.anioInicio }}{{ version.anioFin ? ` - ${version.anioFin}` : '' }}</span>
                        </button>
                        <button class="info-btn" @click="openHierarchyQuickInfo('version', version)" title="Vista rapida">
                          <svg class="info-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                            <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="1.8" />
                            <circle cx="12" cy="8.2" r="1.2" fill="currentColor" />
                            <path d="M12 11v5.2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                          </svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </section>
            </div>

            <div v-if="!isLoading && !filteredMarcas.length" class="alert alert-light border m-3 mb-0">No hay marcas para mostrar.</div>
          </div>
        </div>
      </div>

      <div class="col-12 col-xl-7">
        <div class="card border-0 shadow-sm mb-3">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Alta contextual</h2>
            <div class="d-flex flex-wrap gap-2">
              <button class="btn btn-primary" @click="openMarcaModal">Nueva marca</button>
              <button class="btn btn-outline-primary" :disabled="!selectedMarcaId" @click="openModeloModal">Nuevo modelo para marca</button>
              <button class="btn btn-outline-primary" :disabled="!selectedModeloId" @click="openVersionModal">Nueva version para modelo</button>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-3">
              <router-link :to="{ name: 'MarcaCreate' }" class="btn btn-sm btn-light border">Ir al formulario avanzado de marca</router-link>
              <router-link :to="{ name: 'ModeloCreate', query: selectedMarcaId ? { marcaId: selectedMarcaId } : {} }" class="btn btn-sm btn-light border">
                Ir al formulario avanzado de modelo
              </router-link>
              <router-link :to="{ name: 'VersionCreate', query: selectedModeloId ? { modeloId: selectedModeloId } : {} }" class="btn btn-sm btn-light border">
                Ir al formulario avanzado de version
              </router-link>
            </div>
          </div>
        </div>

        <div class="card border-0 shadow-sm mb-3">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Contexto seleccionado</h2>
            <dl class="row mb-0">
              <dt class="col-sm-3">Marca</dt>
              <dd class="col-sm-9">
                <span v-if="selectedMarca">{{ selectedMarca.nombre }}</span>
                <span v-else class="text-muted">Sin seleccion</span>
              </dd>
              <dt class="col-sm-3">Modelo</dt>
              <dd class="col-sm-9">
                <span v-if="selectedModelo">{{ selectedModelo.nombre }}</span>
                <span v-else class="text-muted">Selecciona una marca para continuar</span>
              </dd>
              <dt class="col-sm-3">Version</dt>
              <dd class="col-sm-9">
                <span v-if="selectedVersion">{{ selectedVersion.nombre }}</span>
                <span v-else class="text-muted">Selecciona un modelo para ver versiones</span>
              </dd>
            </dl>
          </div>
        </div>

        <div class="card border-0 shadow-sm">
          <div class="card-body">
            <h2 class="h6 text-uppercase text-muted mb-3">Acciones rapidas</h2>
            <div class="d-flex flex-wrap gap-2">
              <router-link v-if="selectedMarca?.id" :to="{ name: 'MarcaView', params: { marcaId: selectedMarca.id } }" class="btn btn-light border">
                Ver marca
              </router-link>
              <router-link v-if="selectedModelo?.id" :to="{ name: 'ModeloView', params: { modeloId: selectedModelo.id } }" class="btn btn-light border">
                Ver modelo
              </router-link>
              <router-link v-if="selectedVersion?.id" :to="{ name: 'VersionView', params: { versionId: selectedVersion.id } }" class="btn btn-light border">
                Ver version
              </router-link>
              <router-link :to="{ name: 'VersionCompatibilityAdmin' }" class="btn btn-outline-secondary">Compatibilidades</router-link>
            </div>
          </div>
        </div>

        <div class="card border-0 shadow-sm mt-3" v-if="selectedVersion?.id">
          <div class="card-body">
            <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
              <h2 class="h6 text-uppercase text-muted mb-0">Motores compatibles</h2>
              <button class="btn btn-sm btn-primary" @click="openAssociateMotorModal">Asociar motor</button>
            </div>

            <div v-if="isLoadingVersionMotors" class="text-muted small">Cargando motores...</div>

            <div v-else-if="!versionMotors.length" class="alert alert-warning mb-0">Esta version no tiene motores asociados.</div>

            <ul v-else class="list-group list-group-flush">
              <li
                v-for="motor in versionMotors"
                :key="motor.id"
                class="list-group-item px-0 d-flex justify-content-between align-items-start gap-2"
              >
                <div>
                  <div class="fw-semibold">{{ motor.nombre }}</div>
                  <div class="small text-muted">
                    {{ motor.cilindradaCc || '-' }} cc | {{ motor.potenciaHp || '-' }} HP | {{ motor.combustible?.nombre || '-' }}
                  </div>
                </div>
                <button class="btn btn-sm btn-outline-danger" :disabled="savingInline" @click="removeMotorAssociation(motor.id)">Quitar</button>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <b-modal id="modal-inline-marca" ref="marcaModalRef" title="Nueva marca" centered>
      <div class="mb-2">
        <label class="form-label">Nombre *</label>
        <input v-model.trim="inlineMarca.nombre" type="text" class="form-control" maxlength="100" />
      </div>
      <div>
        <label class="form-label">Pais de origen</label>
        <input v-model.trim="inlineMarca.paisOrigen" type="text" class="form-control" maxlength="100" />
      </div>
      <template #footer>
        <button class="btn btn-light" @click="closeMarcaModal">Cancelar</button>
        <button class="btn btn-primary" :disabled="savingInline" @click="saveInlineMarca">Guardar marca</button>
      </template>
    </b-modal>

    <b-modal id="modal-inline-modelo" ref="modeloModalRef" title="Nuevo modelo" centered>
      <div class="alert alert-light border mb-3">Marca seleccionada: <strong>{{ selectedMarca?.nombre || '-' }}</strong></div>
      <div class="mb-2">
        <label class="form-label">Nombre *</label>
        <input v-model.trim="inlineModelo.nombre" type="text" class="form-control" maxlength="100" />
      </div>
      <div>
        <label class="form-label">Anio lanzamiento *</label>
        <input v-model.number="inlineModelo.anioLanzamiento" type="number" class="form-control" min="1950" max="2100" />
      </div>
      <template #footer>
        <button class="btn btn-light" @click="closeModeloModal">Cancelar</button>
        <button class="btn btn-primary" :disabled="savingInline || !selectedMarcaId" @click="saveInlineModelo">Guardar modelo</button>
      </template>
    </b-modal>

    <b-modal id="modal-inline-version" ref="versionModalRef" title="Nueva version" centered>
      <div class="alert alert-light border mb-3">Modelo seleccionado: <strong>{{ selectedModelo?.nombre || '-' }}</strong></div>
      <div class="mb-2">
        <label class="form-label">Nombre *</label>
        <input v-model.trim="inlineVersion.nombre" type="text" class="form-control" maxlength="50" />
      </div>
      <div class="row g-2">
        <div class="col-6">
          <label class="form-label">Anio inicio *</label>
          <input v-model.number="inlineVersion.anioInicio" type="number" class="form-control" min="1950" max="2100" />
        </div>
        <div class="col-6">
          <label class="form-label">Anio fin</label>
          <input v-model.number="inlineVersion.anioFin" type="number" class="form-control" min="1950" max="2100" />
        </div>
      </div>
      <div class="mt-2">
        <label class="form-label">Descripcion</label>
        <input v-model.trim="inlineVersion.descripcion" type="text" class="form-control" maxlength="150" />
      </div>
      <template #footer>
        <button class="btn btn-light" @click="closeVersionModal">Cancelar</button>
        <button class="btn btn-primary" :disabled="savingInline || !selectedModeloId" @click="saveInlineVersion">Guardar version</button>
      </template>
    </b-modal>

    <b-modal id="modal-associate-motor" ref="associateMotorModalRef" title="Asociar motor a version" centered>
      <div class="alert alert-light border mb-3">Version seleccionada: <strong>{{ selectedVersion?.nombre || '-' }}</strong></div>
      <div class="mb-3">
        <label class="form-label">Motor existente *</label>
        <select v-model.number="selectedMotorIdToAssociate" class="form-select">
          <option :value="null">Seleccionar motor</option>
          <option v-for="motor in availableMotorsToAssociate" :key="motor.id" :value="motor.id">
            {{ motor.nombre }} - {{ motor.cilindradaCc || '-' }} cc / {{ motor.potenciaHp || '-' }} HP
          </option>
        </select>
      </div>

      <div class="border rounded p-3 bg-light-subtle">
        <h3 class="h6 mb-2">Crear motor rapido (opcional)</h3>
        <div class="row g-2">
          <div class="col-12">
            <label class="form-label">Nombre *</label>
            <input v-model.trim="inlineMotor.nombre" type="text" class="form-control" maxlength="100" />
          </div>
          <div class="col-4">
            <label class="form-label">Cilindrada *</label>
            <input v-model.number="inlineMotor.cilindradaCc" type="number" class="form-control" min="50" max="10000" />
          </div>
          <div class="col-4">
            <label class="form-label">Cilindros *</label>
            <input v-model.number="inlineMotor.cilindroCant" type="number" class="form-control" min="1" max="16" />
          </div>
          <div class="col-4">
            <label class="form-label">Potencia HP *</label>
            <input v-model.number="inlineMotor.potenciaHp" type="number" class="form-control" min="1" max="2000" />
          </div>
          <div class="col-4">
            <label class="form-label">Combustible *</label>
            <select v-model.number="inlineMotor.combustibleId" class="form-select">
              <option :value="null">Seleccionar</option>
              <option v-for="combustible in combustibles" :key="combustible.id" :value="combustible.id">{{ combustible.nombre }}</option>
            </select>
          </div>
          <div class="col-4">
            <label class="form-label">Tipo caja *</label>
            <select v-model.number="inlineMotor.tipoCajaId" class="form-select">
              <option :value="null">Seleccionar</option>
              <option v-for="tipoCaja in tipoCajas" :key="tipoCaja.id" :value="tipoCaja.id">{{ tipoCaja.nombre }}</option>
            </select>
          </div>
          <div class="col-4">
            <label class="form-label">Traccion *</label>
            <select v-model.number="inlineMotor.traccionId" class="form-select">
              <option :value="null">Seleccionar</option>
              <option v-for="traccion in tracciones" :key="traccion.id" :value="traccion.id">{{ traccion.nombre }}</option>
            </select>
          </div>
          <div class="col-12 d-flex align-items-center gap-2">
            <input id="inline-motor-turbo" v-model="inlineMotor.turbo" class="form-check-input" type="checkbox" />
            <label class="form-check-label" for="inline-motor-turbo">Turbo</label>
          </div>
        </div>
        <div class="mt-3">
          <button class="btn btn-sm btn-outline-secondary" :disabled="savingInline" @click="saveQuickMotor">Crear motor rapido</button>
        </div>
      </div>

      <template #footer>
        <button class="btn btn-light" @click="closeAssociateMotorModal">Cancelar</button>
        <button class="btn btn-primary" :disabled="savingInline || !selectedMotorIdToAssociate" @click="associateSelectedMotor">Asociar motor</button>
      </template>
    </b-modal>

    <b-modal id="modal-hierarchy-quick-info" ref="hierarchyQuickInfoModalRef" title="Vista rapida" centered>
      <div v-if="hierarchyQuickInfo.type === 'marca' && hierarchyQuickInfo.marca">
        <h3 class="h6 mb-2">{{ hierarchyQuickInfo.marca.nombre }}</h3>
        <p class="mb-1 text-muted">Pais origen: {{ hierarchyQuickInfo.marca.paisOrigen || '-' }}</p>
        <p class="mb-0 text-muted">Modelos asociados: {{ getModelosByMarca(hierarchyQuickInfo.marca.id).length }}</p>
      </div>

      <div v-else-if="hierarchyQuickInfo.type === 'modelo' && hierarchyQuickInfo.modelo">
        <h3 class="h6 mb-2">{{ hierarchyQuickInfo.modelo.nombre }}</h3>
        <p class="mb-1 text-muted">Marca: {{ hierarchyQuickInfo.modelo.marca?.nombre || '-' }}</p>
        <p class="mb-1 text-muted">Anio lanzamiento: {{ hierarchyQuickInfo.modelo.anioLanzamiento || '-' }}</p>
        <p class="mb-0 text-muted">Versiones asociadas: {{ getVersionsByModelo(hierarchyQuickInfo.modelo.id).length }}</p>
      </div>

      <div v-else-if="hierarchyQuickInfo.type === 'version' && hierarchyQuickInfo.version">
        <h3 class="h6 mb-2">{{ hierarchyQuickInfo.version.nombre }}</h3>
        <p class="mb-1 text-muted">Modelo: {{ hierarchyQuickInfo.version.modelo?.nombre || '-' }}</p>
        <p class="mb-1 text-muted">
          Vigencia: {{ hierarchyQuickInfo.version.anioInicio || '-' }}{{ hierarchyQuickInfo.version.anioFin ? ` - ${hierarchyQuickInfo.version.anioFin}` : '' }}
        </p>
        <p class="mb-0 text-muted">Motores asociados: {{ versionMotors.length }}</p>
      </div>

      <template #footer>
        <button class="btn btn-light" @click="closeHierarchyQuickInfoModal">Cerrar</button>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, inject, onMounted, ref, watch } from 'vue';
import MarcaService from '@/entities/marca/marca.service';
import ModeloService from '@/entities/modelo/modelo.service';
import MotorService from '@/entities/motor/motor.service';
import VersionService from '@/entities/version/version.service';
import CombustibleService from '@/entities/combustible/combustible.service';
import TipoCajaService from '@/entities/tipo-caja/tipo-caja.service';
import TraccionService from '@/entities/traccion/traccion.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { type ICombustible } from '@/shared/model/combustible.model';
import { type IMarca } from '@/shared/model/marca.model';
import { type IModelo } from '@/shared/model/modelo.model';
import { type IMotor } from '@/shared/model/motor.model';
import { type ITipoCaja } from '@/shared/model/tipo-caja.model';
import { type ITraccion } from '@/shared/model/traccion.model';
import { type IVersion } from '@/shared/model/version.model';

const marcaService = new MarcaService();
const modeloService = new ModeloService();
const motorService = new MotorService();
const versionService = new VersionService();
const combustibleService = new CombustibleService();
const tipoCajaService = new TipoCajaService();
const traccionService = new TraccionService();
const alertService = inject('alertService', () => useAlertService(), true);

const isLoading = ref(false);
const isLoadingVersionMotors = ref(false);
const savingInline = ref(false);
const searchTerm = ref('');

const marcas = ref<IMarca[]>([]);
const modelos = ref<IModelo[]>([]);
const versions = ref<IVersion[]>([]);
const motors = ref<IMotor[]>([]);
const versionMotors = ref<IMotor[]>([]);
const combustibles = ref<ICombustible[]>([]);
const tipoCajas = ref<ITipoCaja[]>([]);
const tracciones = ref<ITraccion[]>([]);

const selectedMarcaId = ref<number | null>(null);
const selectedModeloId = ref<number | null>(null);
const selectedVersionId = ref<number | null>(null);
const selectedMotorIdToAssociate = ref<number | null>(null);

const marcaModalRef = ref();
const modeloModalRef = ref();
const versionModalRef = ref();
const associateMotorModalRef = ref();
const hierarchyQuickInfoModalRef = ref();

const hierarchyQuickInfo = ref<{
  type: 'marca' | 'modelo' | 'version' | null;
  marca: IMarca | null;
  modelo: IModelo | null;
  version: IVersion | null;
}>({
  type: null,
  marca: null,
  modelo: null,
  version: null,
});

const inlineMarca = ref({
  nombre: '',
  paisOrigen: '',
});

const inlineModelo = ref({
  nombre: '',
  anioLanzamiento: new Date().getFullYear(),
});

const inlineVersion = ref({
  nombre: '',
  descripcion: '',
  anioInicio: new Date().getFullYear(),
  anioFin: null as number | null,
});

const inlineMotor = ref({
  nombre: '',
  cilindradaCc: null as number | null,
  cilindroCant: null as number | null,
  potenciaHp: null as number | null,
  turbo: false,
  combustibleId: null as number | null,
  tipoCajaId: null as number | null,
  traccionId: null as number | null,
});

const normalize = (value?: string | null) => (value ?? '').toLowerCase().trim();

const filteredMarcas = computed(() => {
  const term = normalize(searchTerm.value);
  if (!term) return marcas.value;
  return marcas.value.filter(m => normalize(m.nombre).includes(term));
});

const selectedMarca = computed(() => marcas.value.find(m => m.id === selectedMarcaId.value) ?? null);
const selectedModelo = computed(() => modelos.value.find(m => m.id === selectedModeloId.value) ?? null);
const selectedVersion = computed(() => versions.value.find(v => v.id === selectedVersionId.value) ?? null);
const availableMotorsToAssociate = computed(() => {
  const assignedIds = new Set(versionMotors.value.map(m => m.id).filter(Boolean));
  return motors.value.filter(motor => motor.id && !assignedIds.has(motor.id)).sort((a, b) => (a.nombre ?? '').localeCompare(b.nombre ?? ''));
});

const getModelosByMarca = (marcaId?: number | null) => modelos.value.filter(m => m.marca?.id === marcaId);
const getVersionsByModelo = (modeloId?: number | null) => versions.value.filter(v => v.modelo?.id === modeloId);

const selectMarca = (marcaId?: number) => {
  selectedMarcaId.value = marcaId ?? null;
  const firstModelo = getModelosByMarca(marcaId ?? null)[0];
  selectedModeloId.value = firstModelo?.id ?? null;
  const firstVersion = getVersionsByModelo(firstModelo?.id ?? null)[0];
  selectedVersionId.value = firstVersion?.id ?? null;
};

const selectModelo = (modeloId?: number) => {
  selectedModeloId.value = modeloId ?? null;
  const firstVersion = getVersionsByModelo(modeloId ?? null)[0];
  selectedVersionId.value = firstVersion?.id ?? null;
};

const selectVersion = (versionId?: number) => {
  selectedVersionId.value = versionId ?? null;
};

const loadVersionMotors = async (versionId: number) => {
  isLoadingVersionMotors.value = true;
  try {
    versionMotors.value = await versionService.retrieveMotors(versionId);
  } catch (error: any) {
    versionMotors.value = [];
    alertService.showHttpError(error.response);
  } finally {
    isLoadingVersionMotors.value = false;
  }
};

const loadData = async () => {
  isLoading.value = true;
  try {
    const [marcasRes, modelosRes, versionsRes, motorsRes, combustiblesRes, tipoCajasRes, traccionesRes] = await Promise.all([
      marcaService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] }),
      modeloService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] }),
      versionService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] }),
      motorService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] }),
      combustibleService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] }),
      tipoCajaService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] }),
      traccionService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] }),
    ]);
    marcas.value = marcasRes.data ?? [];
    modelos.value = modelosRes.data ?? [];
    versions.value = versionsRes.data ?? [];
    motors.value = motorsRes.data ?? [];
    combustibles.value = combustiblesRes.data ?? [];
    tipoCajas.value = tipoCajasRes.data ?? [];
    tracciones.value = traccionesRes.data ?? [];

    if (!selectedMarcaId.value && marcas.value.length > 0) {
      selectMarca(marcas.value[0].id);
    } else if (selectedMarcaId.value && !marcas.value.some(m => m.id === selectedMarcaId.value)) {
      selectMarca(marcas.value[0]?.id);
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(loadData);

watch(
  () => selectedVersionId.value,
  async versionId => {
    if (!versionId) {
      versionMotors.value = [];
      return;
    }
    await loadVersionMotors(versionId);
  },
  { immediate: true },
);

const openMarcaModal = () => {
  inlineMarca.value = { nombre: '', paisOrigen: '' };
  marcaModalRef.value?.show();
};

const closeMarcaModal = () => marcaModalRef.value?.hide();

const openModeloModal = () => {
  if (!selectedMarcaId.value) {
    alertService.showError('Selecciona una marca antes de crear un modelo.');
    return;
  }
  inlineModelo.value = { nombre: '', anioLanzamiento: new Date().getFullYear() };
  modeloModalRef.value?.show();
};

const closeModeloModal = () => modeloModalRef.value?.hide();

const openVersionModal = () => {
  if (!selectedModeloId.value) {
    alertService.showError('Selecciona un modelo antes de crear una version.');
    return;
  }
  inlineVersion.value = { nombre: '', descripcion: '', anioInicio: new Date().getFullYear(), anioFin: null };
  versionModalRef.value?.show();
};

const closeVersionModal = () => versionModalRef.value?.hide();

const openAssociateMotorModal = () => {
  if (!selectedVersionId.value) {
    alertService.showError('Selecciona una version antes de asociar un motor.');
    return;
  }
  selectedMotorIdToAssociate.value = null;
  inlineMotor.value = {
    nombre: '',
    cilindradaCc: null,
    cilindroCant: null,
    potenciaHp: null,
    turbo: false,
    combustibleId: null,
    tipoCajaId: null,
    traccionId: null,
  };
  associateMotorModalRef.value?.show();
};

const closeAssociateMotorModal = () => associateMotorModalRef.value?.hide();

const openHierarchyQuickInfo = async (type: 'marca' | 'modelo' | 'version', entity: IMarca | IModelo | IVersion) => {
  if (type === 'marca') {
    hierarchyQuickInfo.value = { type, marca: entity as IMarca, modelo: null, version: null };
  } else if (type === 'modelo') {
    hierarchyQuickInfo.value = { type, marca: null, modelo: entity as IModelo, version: null };
  } else {
    const version = entity as IVersion;
    hierarchyQuickInfo.value = { type, marca: null, modelo: null, version };
    if (version.id) {
      await loadVersionMotors(version.id);
    }
  }
  hierarchyQuickInfoModalRef.value?.show();
};

const closeHierarchyQuickInfoModal = () => hierarchyQuickInfoModalRef.value?.hide();

const saveInlineMarca = async () => {
  const nombre = inlineMarca.value.nombre.trim();
  if (!nombre) {
    alertService.showError('El nombre de la marca es obligatorio.');
    return;
  }
  savingInline.value = true;
  try {
    const created = await marcaService.create({ nombre, paisOrigen: inlineMarca.value.paisOrigen?.trim() || null });
    await loadData();
    selectMarca(created.id);
    closeMarcaModal();
    alertService.showSuccess('Marca creada correctamente.');
  } catch (error: any) {
    alertService.showHttpError(error.response);
  } finally {
    savingInline.value = false;
  }
};

const saveInlineModelo = async () => {
  if (!selectedMarcaId.value) {
    alertService.showError('No puedes crear un modelo sin marca seleccionada.');
    return;
  }
  const nombre = inlineModelo.value.nombre.trim();
  if (!nombre) {
    alertService.showError('El nombre del modelo es obligatorio.');
    return;
  }
  if (!inlineModelo.value.anioLanzamiento || inlineModelo.value.anioLanzamiento < 1950 || inlineModelo.value.anioLanzamiento > 2100) {
    alertService.showError('El anio de lanzamiento debe estar entre 1950 y 2100.');
    return;
  }
  savingInline.value = true;
  try {
    const created = await modeloService.create({
      nombre,
      anioLanzamiento: inlineModelo.value.anioLanzamiento,
      marca: { id: selectedMarcaId.value },
    });
    await loadData();
    selectMarca(selectedMarcaId.value);
    selectModelo(created.id);
    closeModeloModal();
    alertService.showSuccess('Modelo creado correctamente.');
  } catch (error: any) {
    alertService.showHttpError(error.response);
  } finally {
    savingInline.value = false;
  }
};

const saveInlineVersion = async () => {
  if (!selectedModeloId.value) {
    alertService.showError('No puedes crear una version sin modelo seleccionado.');
    return;
  }
  const nombre = inlineVersion.value.nombre.trim();
  if (!nombre) {
    alertService.showError('El nombre de la version es obligatorio.');
    return;
  }
  if (!inlineVersion.value.anioInicio || inlineVersion.value.anioInicio < 1950 || inlineVersion.value.anioInicio > 2100) {
    alertService.showError('El anio de inicio debe estar entre 1950 y 2100.');
    return;
  }
  if (inlineVersion.value.anioFin && inlineVersion.value.anioFin < inlineVersion.value.anioInicio) {
    alertService.showError('El anio de fin no puede ser menor al anio de inicio.');
    return;
  }
  savingInline.value = true;
  try {
    const created = await versionService.create({
      nombre,
      descripcion: inlineVersion.value.descripcion?.trim() || null,
      anioInicio: inlineVersion.value.anioInicio,
      anioFin: inlineVersion.value.anioFin,
      modelo: { id: selectedModeloId.value },
    });
    await loadData();
    selectModelo(selectedModeloId.value);
    selectVersion(created.id);
    closeVersionModal();
    alertService.showSuccess('Version creada correctamente.');
  } catch (error: any) {
    alertService.showHttpError(error.response);
  } finally {
    savingInline.value = false;
  }
};

const saveQuickMotor = async () => {
  const data = inlineMotor.value;
  if (!data.nombre.trim()) {
    alertService.showError('El nombre del motor es obligatorio.');
    return;
  }
  if (!data.cilindradaCc || data.cilindradaCc < 50 || data.cilindradaCc > 10000) {
    alertService.showError('La cilindrada debe estar entre 50 y 10000.');
    return;
  }
  if (!data.cilindroCant || data.cilindroCant < 1 || data.cilindroCant > 16) {
    alertService.showError('La cantidad de cilindros debe estar entre 1 y 16.');
    return;
  }
  if (!data.potenciaHp || data.potenciaHp < 1 || data.potenciaHp > 2000) {
    alertService.showError('La potencia debe estar entre 1 y 2000 HP.');
    return;
  }
  if (!data.combustibleId || !data.tipoCajaId || !data.traccionId) {
    alertService.showError('Completa combustible, caja y traccion.');
    return;
  }

  savingInline.value = true;
  try {
    const created = await motorService.create({
      nombre: data.nombre.trim(),
      cilindradaCc: data.cilindradaCc,
      cilindroCant: data.cilindroCant,
      potenciaHp: data.potenciaHp,
      turbo: data.turbo,
      combustible: { id: data.combustibleId },
      tipoCaja: { id: data.tipoCajaId },
      traccion: { id: data.traccionId },
    });
    const motorsRes = await motorService.retrieve({ page: 0, size: 1000, sort: ['nombre,asc'] });
    motors.value = motorsRes.data ?? [];
    selectedMotorIdToAssociate.value = created.id ?? null;
    alertService.showSuccess('Motor creado. Ahora puedes asociarlo.');
  } catch (error: any) {
    alertService.showHttpError(error.response);
  } finally {
    savingInline.value = false;
  }
};

const associateSelectedMotor = async () => {
  if (!selectedVersionId.value || !selectedMotorIdToAssociate.value) {
    alertService.showError('Selecciona un motor para asociar.');
    return;
  }
  savingInline.value = true;
  try {
    versionMotors.value = await versionService.addMotorCompatibility(selectedVersionId.value, selectedMotorIdToAssociate.value);
    closeAssociateMotorModal();
    alertService.showSuccess('Motor asociado correctamente.');
  } catch (error: any) {
    alertService.showHttpError(error.response);
  } finally {
    savingInline.value = false;
  }
};

const removeMotorAssociation = async (motorId?: number) => {
  if (!selectedVersionId.value || !motorId) {
    return;
  }
  savingInline.value = true;
  try {
    versionMotors.value = await versionService.removeMotorCompatibility(selectedVersionId.value, motorId);
    alertService.showSuccess('Asociacion de motor eliminada.');
  } catch (error: any) {
    alertService.showHttpError(error.response);
  } finally {
    savingInline.value = false;
  }
};
</script>

<style scoped>
.catalog-page {
  max-width: 1320px;
  margin: 0 auto;
}

.hierarchy-header {
  padding: 0.9rem 1rem 0.7rem;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 0.75rem;
}

.search-input {
  max-width: 220px;
}

.hierarchy-scroll {
  max-height: 68vh;
  overflow: auto;
  display: grid;
  gap: 0.5rem;
  padding: 0.75rem;
}

.brand-card {
  border: 1px solid #edf2f7;
  border-radius: 10px;
  background: #fff;
  padding: 0.4rem;
}

.brand-card.active {
  border-color: #bfdbfe;
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.18);
}

.brand-row,
.model-row,
.version-row {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 0.45rem;
}

.model-list {
  margin-top: 0.35rem;
  display: grid;
  gap: 0.35rem;
  padding-left: 0.6rem;
  border-left: 1px solid #e6edf7;
}

.model-card {
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  background: #fff;
  padding: 0.25rem;
}

.model-card.active {
  border-color: #dbeafe;
  background: #f9fbff;
}

.version-list {
  margin-top: 0.3rem;
  display: grid;
  gap: 0.25rem;
  padding-left: 0.55rem;
  border-left: 1px dashed #dbeafe;
}

.version-row {
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  padding: 0.15rem;
  background: #fff;
}

.version-row.active {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.entity-btn {
  width: 100%;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  padding: 0.38rem 0.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-align: left;
}

.entity-btn:hover {
  background: #f8fbff;
}

.brand-btn {
  font-weight: 600;
}

.model-btn {
  font-weight: 500;
}

.version-btn {
  font-size: 0.92rem;
}

.entity-left {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  min-width: 0;
}

.entity-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.15;
}

.entity-dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: #3b82f6;
  flex: 0 0 auto;
}

.entity-badge {
  min-width: 1.5rem;
  text-align: center;
  padding: 0.02rem 0.3rem;
  border-radius: 999px;
  background: #f1f5f9;
  color: #475569;
  font-size: 0.72rem;
  font-weight: 600;
}

.entity-meta {
  color: #64748b;
  font-size: 0.72rem;
  white-space: nowrap;
}

.info-btn {
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  min-width: 1.8rem;
  min-height: 1.8rem;
  border-radius: 6px;
}

.info-btn:hover {
  border-color: #93c5fd;
  color: #1d4ed8;
  background: #eff6ff;
}

.info-icon {
  width: 14px;
  height: 14px;
  display: block;
}
</style>
