import { Authority } from '@/shared/jhipster/constants';
const Entities = () => import('@/entities/entities.vue');

const Marca = () => import('@/entities/marca/marca.vue');
const MarcaUpdate = () => import('@/entities/marca/marca-update.vue');
const MarcaDetails = () => import('@/entities/marca/marca-details.vue');

const Modelo = () => import('@/entities/modelo/modelo.vue');
const ModeloUpdate = () => import('@/entities/modelo/modelo-update.vue');
const ModeloDetails = () => import('@/entities/modelo/modelo-details.vue');

const Version = () => import('@/entities/version/version.vue');
const VersionUpdate = () => import('@/entities/version/version-update.vue');
const VersionDetails = () => import('@/entities/version/version-details.vue');

const Motor = () => import('@/entities/motor/motor.vue');
const MotorUpdate = () => import('@/entities/motor/motor-update.vue');
const MotorDetails = () => import('@/entities/motor/motor-details.vue');

const Combustible = () => import('@/entities/combustible/combustible.vue');
const CombustibleUpdate = () => import('@/entities/combustible/combustible-update.vue');
const CombustibleDetails = () => import('@/entities/combustible/combustible-details.vue');

const Auto = () => import('@/entities/auto/auto.vue');
const AutoUpdate = () => import('@/entities/auto/auto-update.vue');
const AutoDetails = () => import('@/entities/auto/auto-details.vue');

// jhipster-needle-add-entity-to-router-import - JHipster will import entities to the router here

export default {
  path: '/',
  component: Entities,
  children: [
    {
      path: 'marca',
      name: 'Marca',
      component: Marca,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'marca/new',
      name: 'MarcaCreate',
      component: MarcaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'marca/:marcaId/edit',
      name: 'MarcaEdit',
      component: MarcaUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'marca/:marcaId/view',
      name: 'MarcaView',
      component: MarcaDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'modelo',
      name: 'Modelo',
      component: Modelo,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'modelo/new',
      name: 'ModeloCreate',
      component: ModeloUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'modelo/:modeloId/edit',
      name: 'ModeloEdit',
      component: ModeloUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'modelo/:modeloId/view',
      name: 'ModeloView',
      component: ModeloDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'version',
      name: 'Version',
      component: Version,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'version/new',
      name: 'VersionCreate',
      component: VersionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'version/:versionId/edit',
      name: 'VersionEdit',
      component: VersionUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'version/:versionId/view',
      name: 'VersionView',
      component: VersionDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'motor',
      name: 'Motor',
      component: Motor,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'motor/new',
      name: 'MotorCreate',
      component: MotorUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'motor/:motorId/edit',
      name: 'MotorEdit',
      component: MotorUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'motor/:motorId/view',
      name: 'MotorView',
      component: MotorDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'combustible',
      name: 'Combustible',
      component: Combustible,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'combustible/new',
      name: 'CombustibleCreate',
      component: CombustibleUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'combustible/:combustibleId/edit',
      name: 'CombustibleEdit',
      component: CombustibleUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'combustible/:combustibleId/view',
      name: 'CombustibleView',
      component: CombustibleDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'auto',
      name: 'Auto',
      component: Auto,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'auto/new',
      name: 'AutoCreate',
      component: AutoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'auto/:autoId/edit',
      name: 'AutoEdit',
      component: AutoUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'auto/:autoId/view',
      name: 'AutoView',
      component: AutoDetails,
      meta: { authorities: [Authority.USER] },
    },
    // jhipster-needle-add-entity-to-router - JHipster will add entities to the router here
  ],
};
