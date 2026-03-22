import { defineComponent, provide } from 'vue';

import UserService from '@/entities/user/user.service';

import AutoService from './auto/auto.service';
import CombustibleService from './combustible/combustible.service';
import MarcaService from './marca/marca.service';
import ModeloService from './modelo/modelo.service';
import MotorService from './motor/motor.service';
import VersionService from './version/version.service';
// jhipster-needle-add-entity-service-to-entities-component-import - JHipster will import entities services here

export default defineComponent({
  name: 'Entities',
  setup() {
    provide('userService', () => new UserService());
    provide('marcaService', () => new MarcaService());
    provide('modeloService', () => new ModeloService());
    provide('versionService', () => new VersionService());
    provide('motorService', () => new MotorService());
    provide('combustibleService', () => new CombustibleService());
    provide('autoService', () => new AutoService());
    // jhipster-needle-add-entity-service-to-entities-component - JHipster will import entities services here
  },
});
