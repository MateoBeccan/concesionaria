import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { useDateFormat } from '@/shared/composables';
import { type ICliente } from '@/shared/model/cliente.model';

import ClienteService from './cliente.service';

export default defineComponent({
  name: 'ClienteDetails',
  setup() {
    const dateFormat = useDateFormat();
    const clienteService = inject('clienteService', () => new ClienteService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const cliente: Ref<ICliente> = ref({});

    const retrieveCliente = async clienteId => {
      try {
        const res = await clienteService().find(clienteId);
        cliente.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.clienteId) {
      retrieveCliente(route.params.clienteId);
    }

    return {
      ...dateFormat,
      alertService,
      cliente,

      previousState,
    };
  },
});
