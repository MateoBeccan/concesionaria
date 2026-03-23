import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAlertService } from '@/shared/alert/alert.service';
import { type IDetalleVenta } from '@/shared/model/detalle-venta.model';

import DetalleVentaService from './detalle-venta.service';

export default defineComponent({
  name: 'DetalleVentaDetails',
  setup() {
    const detalleVentaService = inject('detalleVentaService', () => new DetalleVentaService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const detalleVenta: Ref<IDetalleVenta> = ref({});

    const retrieveDetalleVenta = async detalleVentaId => {
      try {
        const res = await detalleVentaService().find(detalleVentaId);
        detalleVenta.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.detalleVentaId) {
      retrieveDetalleVenta(route.params.detalleVentaId);
    }

    return {
      alertService,
      detalleVenta,

      previousState,
    };
  },
});
