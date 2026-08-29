package com.concesionaria.app.service;

import com.concesionaria.app.web.rest.vm.ConfirmarVentaRequestVM;
import com.concesionaria.app.web.rest.vm.ConfirmarVentaResponseVM;

public interface VentaConfirmacionService {
    ConfirmarVentaResponseVM confirmarVenta(ConfirmarVentaRequestVM request, String idempotencyKey);
}
