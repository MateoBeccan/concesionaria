<template>
  <div class="catalog-page">
    <section class="hero-card">
      <div>
        <p class="hero-eyebrow">Configuracion tecnica</p>
        <h1 class="hero-title">Centro de catalogo tecnico</h1>
        <p class="hero-copy">Gestiona la estructura tecnica completa con un flujo ordenado y controles de consistencia.</p>
      </div>
      <div class="hero-flow">
        <span>Paso 1: Base comercial</span>
        <span>Paso 2: Especificaciones</span>
        <span>Paso 3: Compatibilidades</span>
      </div>
    </section>

    <section class="kpi-grid">
      <article class="kpi-card">
        <span class="kpi-label">Items base</span>
        <strong class="kpi-value">{{ baseCount }}</strong>
      </article>
      <article class="kpi-card">
        <span class="kpi-label">Items tecnicos</span>
        <strong class="kpi-value">{{ technicalCount }}</strong>
      </article>
      <article class="kpi-card">
        <span class="kpi-label">Modulos activos</span>
        <strong class="kpi-value">{{ allModules.length }}</strong>
      </article>
      <article class="kpi-card">
        <span class="kpi-label">Compatibilidades</span>
        <strong class="kpi-value">Gestion centralizada</strong>
      </article>
    </section>

    <section class="surface-card">
      <div class="surface-head">
        <div>
          <p class="surface-eyebrow">Navegacion guiada</p>
          <h2 class="surface-title">Carga de catalogo por etapas</h2>
        </div>
        <input v-model.trim="searchTerm" type="text" class="form-control filter-input" placeholder="Buscar modulo..." />
      </div>

      <div class="group-layout">
        <article class="group-card">
          <p class="group-label">Base comercial</p>
          <div class="card-grid">
            <router-link v-for="item in filteredBusiness" :key="item.name" :to="{ name: item.name }" class="module-card">
              <span class="module-code">{{ item.code }}</span>
              <div class="module-main">
                <strong>{{ item.title }}</strong>
                <small>{{ item.copy }}</small>
              </div>
              <div class="module-meta">
                <span class="count-pill">{{ countByName[item.name] ?? '-' }}</span>
                <span class="open-pill">Abrir</span>
              </div>
            </router-link>
          </div>
        </article>

        <article class="group-card">
          <p class="group-label">Especificaciones tecnicas</p>
          <div class="card-grid">
            <router-link v-for="item in filteredTechnical" :key="item.name" :to="{ name: item.name }" class="module-card">
              <span class="module-code">{{ item.code }}</span>
              <div class="module-main">
                <strong>{{ item.title }}</strong>
                <small>{{ item.copy }}</small>
              </div>
              <div class="module-meta">
                <span class="count-pill">{{ countByName[item.name] ?? '-' }}</span>
                <span class="open-pill">Abrir</span>
              </div>
            </router-link>
          </div>
        </article>
      </div>
    </section>

    <section class="surface-card">
      <div class="compat-head">
        <div>
          <p class="surface-eyebrow">Control de consistencia</p>
          <h2 class="surface-title">Compatibilidades version-motor</h2>
          <p class="surface-copy">Define combinaciones validas para prevenir errores al cargar o editar vehiculos.</p>
        </div>
        <router-link :to="{ name: 'VersionCompatibilityAdmin' }" class="btn btn-primary">Gestionar compatibilidades</router-link>
      </div>
    </section>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import CombustibleService from '@/entities/combustible/combustible.service';
import MarcaService from '@/entities/marca/marca.service';
import ModeloService from '@/entities/modelo/modelo.service';
import MotorService from '@/entities/motor/motor.service';
import TipoCajaService from '@/entities/tipo-caja/tipo-caja.service';
import TipoVehiculoService from '@/entities/tipo-vehiculo/tipo-vehiculo.service';
import TraccionService from '@/entities/traccion/traccion.service';
import VersionService from '@/entities/version/version.service';

type ModuleEntry = {
  name: string;
  code: string;
  title: string;
  copy: string;
};

const searchTerm = ref('');
const countByName = ref<Record<string, string>>({});

const businessStructure: ModuleEntry[] = [
  { name: 'Marca', code: 'MA', title: 'Marcas', copy: 'Define fabricantes y lineas principales.' },
  { name: 'Modelo', code: 'MO', title: 'Modelos', copy: 'Agrupa modelos por marca para catalogo comercial.' },
  { name: 'Version', code: 'VE', title: 'Versiones', copy: 'Versiones por modelo para oferta y venta.' },
];

const technicalSpecs: ModuleEntry[] = [
  { name: 'Motor', code: 'MT', title: 'Motores', copy: 'Motorizaciones, potencia y caracteristicas.' },
  { name: 'TipoVehiculo', code: 'TV', title: 'Tipos de vehiculo', copy: 'Segmentacion por categoria de unidad.' },
  { name: 'Combustible', code: 'CB', title: 'Combustibles', copy: 'Tipos de energia para motores.' },
  { name: 'Traccion', code: 'TR', title: 'Tracciones', copy: 'Esquemas de traccion disponibles.' },
  { name: 'TipoCaja', code: 'TC', title: 'Tipos de caja', copy: 'Transmisiones manuales y automaticas.' },
];

const allModules = computed(() => [...businessStructure, ...technicalSpecs]);

const normalize = (value: string) => value.toLowerCase().trim();

const matches = (entry: ModuleEntry, term: string) => {
  if (!term) return true;
  const t = normalize(term);
  return [entry.title, entry.copy, entry.code].some(field => normalize(field).includes(t));
};

const filteredBusiness = computed(() => businessStructure.filter(entry => matches(entry, searchTerm.value)));
const filteredTechnical = computed(() => technicalSpecs.filter(entry => matches(entry, searchTerm.value)));

const parseTotal = (headers: any): string => {
  const total = headers?.['x-total-count'] ?? headers?.['X-Total-Count'];
  if (total === undefined || total === null) return '-';
  const asNumber = Number(total);
  return Number.isFinite(asNumber) ? asNumber.toLocaleString('es-AR') : String(total);
};

async function loadCounts() {
  const marcaService = new MarcaService();
  const modeloService = new ModeloService();
  const versionService = new VersionService();
  const motorService = new MotorService();
  const tipoVehiculoService = new TipoVehiculoService();
  const combustibleService = new CombustibleService();
  const traccionService = new TraccionService();
  const tipoCajaService = new TipoCajaService();

  const queries = [
    { key: 'Marca', request: marcaService.retrieve({ page: 0, size: 1 }) },
    { key: 'Modelo', request: modeloService.retrieve({ page: 0, size: 1 }) },
    { key: 'Version', request: versionService.retrieve({ page: 0, size: 1 }) },
    { key: 'Motor', request: motorService.retrieve({ page: 0, size: 1 }) },
    { key: 'TipoVehiculo', request: tipoVehiculoService.retrieve({ page: 0, size: 1 }) },
    { key: 'Combustible', request: combustibleService.retrieve({ page: 0, size: 1 }) },
    { key: 'Traccion', request: traccionService.retrieve({ page: 0, size: 1 }) },
    { key: 'TipoCaja', request: tipoCajaService.retrieve({ page: 0, size: 1 }) },
  ];

  const results = await Promise.allSettled(queries.map(item => item.request));
  const nextCounts: Record<string, string> = {};
  results.forEach((result, index) => {
    const key = queries[index].key;
    if (result.status === 'fulfilled') {
      nextCounts[key] = parseTotal(result.value.headers);
    } else {
      nextCounts[key] = '-';
    }
  });
  countByName.value = nextCounts;
}

const baseCount = computed(() =>
  businessStructure.reduce((acc, item) => acc + Number(String(countByName.value[item.name] ?? '').replace(/\./g, '') || 0), 0).toLocaleString('es-AR'),
);
const technicalCount = computed(() =>
  technicalSpecs.reduce((acc, item) => acc + Number(String(countByName.value[item.name] ?? '').replace(/\./g, '') || 0), 0).toLocaleString('es-AR'),
);

onMounted(() => {
  loadCounts();
});
</script>

<style scoped>
.catalog-page {
  max-width: 1140px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.hero-card {
  padding: 1.25rem 1.3rem;
  border-radius: 18px;
  color: #f8fafc;
  border: 1px solid #1d4ed8;
  background: linear-gradient(140deg, #0f172a 0%, #1e40af 100%);
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.hero-eyebrow,
.surface-eyebrow,
.group-label {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 700;
}

.hero-title,
.surface-title {
  margin: 0.35rem 0 0;
}

.hero-copy,
.surface-copy {
  margin: 0.45rem 0 0;
  color: #dbeafe;
}

.hero-flow {
  display: grid;
  gap: 0.45rem;
  align-content: center;
}

.hero-flow span {
  padding: 0.45rem 0.65rem;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 0.8rem;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 0.75rem;
}

.kpi-card,
.surface-card,
.group-card,
.module-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
}

.kpi-card {
  padding: 0.85rem 1rem;
  display: grid;
  gap: 0.2rem;
}

.kpi-label {
  font-size: 0.78rem;
  color: #64748b;
}

.kpi-value {
  color: #0f172a;
  font-size: 1.25rem;
}

.surface-card {
  padding: 1rem;
}

.surface-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 1rem;
  margin-bottom: 0.8rem;
}

.filter-input {
  max-width: 320px;
}

.group-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.85rem;
}

.group-card {
  padding: 0.85rem;
}

.group-label {
  color: #1d4ed8;
  margin-bottom: 0.65rem;
}

.card-grid {
  display: grid;
  gap: 0.6rem;
}

.module-card {
  padding: 0.7rem 0.75rem;
  text-decoration: none;
  color: inherit;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 0.6rem;
  align-items: center;
}

.module-code {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 0.74rem;
  font-weight: 700;
}

.module-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.module-main strong {
  color: #0f172a;
}

.module-main small {
  color: #64748b;
  margin-top: 0.12rem;
}

.module-meta {
  display: grid;
  gap: 0.3rem;
  justify-items: end;
}

.count-pill,
.open-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  min-width: 52px;
  padding: 0 0.45rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
}

.count-pill {
  background: #f1f5f9;
  color: #334155;
}

.open-pill {
  background: #eff6ff;
  color: #2563eb;
}

.compat-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

@media (max-width: 992px) {
  .hero-card,
  .surface-head,
  .compat-head {
    flex-direction: column;
    align-items: stretch;
  }

  .group-layout {
    grid-template-columns: 1fr;
  }

  .filter-input {
    max-width: none;
  }
}
</style>
