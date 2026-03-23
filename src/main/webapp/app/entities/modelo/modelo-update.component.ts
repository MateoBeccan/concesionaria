import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import MarcaService from '@/entities/marca/marca.service';
import VersionService from '@/entities/version/version.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { Carroceria } from '@/shared/model/enumerations/carroceria.model';
import { type IMarca } from '@/shared/model/marca.model';
import { type IModelo, Modelo } from '@/shared/model/modelo.model';
import { type IVersion } from '@/shared/model/version.model';

import ModeloService from './modelo.service';

export default defineComponent({
  name: 'ModeloUpdate',
  setup() {
    const modeloService = inject('modeloService', () => new ModeloService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const modelo: Ref<IModelo> = ref(new Modelo());

    const marcaService = inject('marcaService', () => new MarcaService());

    const marcas: Ref<IMarca[]> = ref([]);

    const versionService = inject('versionService', () => new VersionService());

    const versions: Ref<IVersion[]> = ref([]);
    const carroceriaValues: Ref<string[]> = ref(Object.keys(Carroceria));
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveModelo = async modeloId => {
      try {
        const res = await modeloService().find(modeloId);
        modelo.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.modeloId) {
      retrieveModelo(route.params.modeloId);
    }

    const initRelationships = () => {
      marcaService()
        .retrieve()
        .then(res => {
          marcas.value = res.data;
        });
      versionService()
        .retrieve()
        .then(res => {
          versions.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
      },
      anioLanzamiento: {},
      carroceria: {},
      marca: {},
      versioneses: {},
    };
    const v$ = useVuelidate(validationRules, modelo as any);
    v$.value.$validate();

    return {
      modeloService,
      alertService,
      modelo,
      previousState,
      carroceriaValues,
      isSaving,
      currentLanguage,
      marcas,
      versions,
      v$,
    };
  },
  created(): void {
    this.modelo.versioneses = [];
  },
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.modelo.id) {
        this.modeloService()
          .update(this.modelo)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Modelo is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.modeloService()
          .create(this.modelo)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Modelo is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },

    getSelected(selectedVals, option, pkField = 'id'): any {
      if (selectedVals) {
        return selectedVals.find(value => option[pkField] === value[pkField]) ?? option;
      }
      return option;
    },
  },
});
