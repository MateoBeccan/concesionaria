import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import CarroceriaService from '@/entities/carroceria/carroceria.service';
import MarcaService from '@/entities/marca/marca.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { type ICarroceria } from '@/shared/model/carroceria.model';
import { type IMarca } from '@/shared/model/marca.model';
import { type IModelo, Modelo } from '@/shared/model/modelo.model';

import ModeloService from './modelo.service';

export default defineComponent({
  name: 'ModeloUpdate',
  setup() {
    const modeloService = inject('modeloService', () => new ModeloService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const modelo: Ref<IModelo> = ref(new Modelo());

    const marcaService = inject('marcaService', () => new MarcaService());

    const marcas: Ref<IMarca[]> = ref([]);

    const carroceriaService = inject('carroceriaService', () => new CarroceriaService());

    const carrocerias: Ref<ICarroceria[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveModelo = async modeloId => {
      try {
        const res = await modeloService().find(modeloId);
        res.createdDate = new Date(res.createdDate);
        res.lastModifiedDate = new Date(res.lastModifiedDate);
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
      carroceriaService()
        .retrieve()
        .then(res => {
          carrocerias.value = res.data;
        });
    };

    initRelationships();

    const validations = useValidation();
    const validationRules = {
      nombre: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 2 caracteres.', 2),
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      anioLanzamiento: {
        required: validations.required('Este campo es obligatorio.'),
        integer: validations.integer('Este campo debe ser un número.'),
        min: validations.minValue('Este campo debe ser mayor que 1950.', 1950),
        max: validations.maxValue('Este campo no puede ser mayor que 2100.', 2100),
      },
      createdDate: {},
      lastModifiedDate: {},
      marca: {},
      carroceria: {},
    };
    const v$ = useVuelidate(validationRules, modelo as any);
    v$.value.$validate();

    return {
      modeloService,
      alertService,
      modelo,
      previousState,
      isSaving,
      currentLanguage,
      marcas,
      carrocerias,
      v$,
      ...useDateFormat({ entityRef: modelo }),
    };
  },
  created(): void {},
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
  },
});
