import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';

import CondicionIvaService from '@/entities/condicion-iva/condicion-iva.service';
import TipoDocumentoService from '@/entities/tipo-documento/tipo-documento.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { Cliente, type ICliente } from '@/shared/model/cliente.model';
import { type ICondicionIva } from '@/shared/model/condicion-iva.model';
import { type ITipoDocumento } from '@/shared/model/tipo-documento.model';

import ClienteService from './cliente.service';

export default defineComponent({
  name: 'ClienteUpdate',
  setup() {
    const clienteService = inject('clienteService', () => new ClienteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const cliente: Ref<ICliente> = ref(new Cliente());

    const condicionIvaService = inject('condicionIvaService', () => new CondicionIvaService());

    const condicionIvas: Ref<ICondicionIva[]> = ref([]);

    const tipoDocumentoService = inject('tipoDocumentoService', () => new TipoDocumentoService());

    const tipoDocumentos: Ref<ITipoDocumento[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'es'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveCliente = async clienteId => {
      try {
        const res = await clienteService().find(clienteId);
        res.fechaAlta = new Date(res.fechaAlta);
        res.createdDate = new Date(res.createdDate);
        res.lastModifiedDate = new Date(res.lastModifiedDate);
        cliente.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.clienteId) {
      retrieveCliente(route.params.clienteId);
    }

    const initRelationships = () => {
      condicionIvaService()
        .retrieve()
        .then(res => {
          condicionIvas.value = res.data;
        });
      tipoDocumentoService()
        .retrieve()
        .then(res => {
          tipoDocumentos.value = res.data;
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
      apellido: {
        required: validations.required('Este campo es obligatorio.'),
        minLength: validations.minLength('Este campo requiere al menos 2 caracteres.', 2),
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      nroDocumento: {
        required: validations.required('Este campo es obligatorio.'),
      },
      telefono: {},
      email: {
        required: validations.required('Este campo es obligatorio.'),
      },
      direccion: {
        maxLength: validations.maxLength('Este campo no puede superar más de 255 caracteres.', 255),
      },
      ciudad: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      provincia: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      pais: {
        maxLength: validations.maxLength('Este campo no puede superar más de 100 caracteres.', 100),
      },
      activo: {
        required: validations.required('Este campo es obligatorio.'),
      },
      fechaAlta: {
        required: validations.required('Este campo es obligatorio.'),
      },
      createdDate: {},
      lastModifiedDate: {},
      condicionIva: {},
      tipoDocumento: {},
    };
    const v$ = useVuelidate(validationRules, cliente as any);
    v$.value.$validate();

    return {
      clienteService,
      alertService,
      cliente,
      previousState,
      isSaving,
      currentLanguage,
      condicionIvas,
      tipoDocumentos,
      v$,
      ...useDateFormat({ entityRef: cliente }),
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.cliente.id) {
        this.clienteService()
          .update(this.cliente)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`A Cliente is updated with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.clienteService()
          .create(this.cliente)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`A Cliente is created with identifier ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
