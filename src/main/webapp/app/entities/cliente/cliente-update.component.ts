import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useVuelidate } from '@vuelidate/core';
import { helpers } from '@vuelidate/validators';

import CondicionIvaService from '@/entities/condicion-iva/condicion-iva.service';
import TipoDocumentoService from '@/entities/tipo-documento/tipo-documento.service';
import { useAlertService } from '@/shared/alert/alert.service';
import { useValidation } from '@/shared/composables';
import { Cliente, type ICliente } from '@/shared/model/cliente.model';
import { type ICondicionIva } from '@/shared/model/condicion-iva.model';
import { type ITipoDocumento } from '@/shared/model/tipo-documento.model';

import ClienteService from './cliente.service';

const DOCUMENTO_REGEX = /^\d{7,11}$/;
const TELEFONO_REGEX = /^[0-9+\-\s]{6,20}$/;
const EMAIL_REGEX = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

const toDateOrNull = value => (value ? new Date(value) : null);
const toIsoOrNull = value => {
  if (!value) return null;
  const parsed = new Date(value as any);
  return Number.isNaN(parsed.getTime()) ? null : parsed.toISOString();
};

const normalizeString = (value?: string | null) => value?.trim() ?? '';

export default defineComponent({
  name: 'ClienteUpdate',
  setup() {
    const clienteService = inject('clienteService', () => new ClienteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const cliente: Ref<ICliente> = ref(
      new Cliente(undefined, undefined, undefined, undefined, null, undefined, null, null, null, null, true, new Date(), null, null, null, null),
    );

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
        res.fechaAlta = toDateOrNull(res.fechaAlta);
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
        validFormat: helpers.withMessage(
          'El documento debe tener entre 7 y 11 dígitos.',
          value => !value || DOCUMENTO_REGEX.test(String(value).trim()),
        ),
        validByTipo: helpers.withMessage('El documento no coincide con el tipo seleccionado.', value => {
          const documento = String(value ?? '').trim();
          if (!documento) return true;
          const codigo = cliente.value.tipoDocumento?.codigo?.toUpperCase() ?? '';
          if (codigo.includes('CUIT') || codigo.includes('CUIL')) return documento.length === 11;
          if (codigo.includes('DNI')) return documento.length >= 7 && documento.length <= 8;
          return documento.length >= 7 && documento.length <= 11;
        }),
      },
      telefono: {
        validFormat: helpers.withMessage('Ingresá un teléfono válido.', value => !value || TELEFONO_REGEX.test(String(value).trim())),
      },
      email: {
        required: validations.required('Este campo es obligatorio.'),
        validFormat: helpers.withMessage('Ingresá un email válido.', value => !value || EMAIL_REGEX.test(String(value).trim())),
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
      condicionIva: {},
      tipoDocumento: {
        required: validations.required('Seleccioná un tipo de documento.'),
      },
    };
    const v$ = useVuelidate(validationRules, cliente as any);

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
    };
  },
  methods: {
    save(): void {
      this.v$.$touch();
      if (this.v$.$invalid) {
        return;
      }

      const entity = {
        ...this.cliente,
        nombre: normalizeString(this.cliente.nombre),
        apellido: normalizeString(this.cliente.apellido),
        nroDocumento: normalizeString(this.cliente.nroDocumento),
        telefono: normalizeString(this.cliente.telefono) || null,
        email: normalizeString(this.cliente.email).toLowerCase(),
        direccion: normalizeString(this.cliente.direccion) || null,
        ciudad: normalizeString(this.cliente.ciudad) || null,
        provincia: normalizeString(this.cliente.provincia) || null,
        pais: normalizeString(this.cliente.pais) || null,
        fechaAlta: toIsoOrNull(this.cliente.fechaAlta),
      };

      this.isSaving = true;
      if (entity.id) {
        this.clienteService()
          .update(entity)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(`Cliente actualizado con ID ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.clienteService()
          .create(entity)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(`Cliente creado con ID ${param.id}`);
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
