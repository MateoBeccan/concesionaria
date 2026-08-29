package com.concesionaria.app.web.rest.vm;

import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ConfirmarVentaRequestVM {

    @Valid
    @NotNull
    private VentaDTO venta;

    @Valid
    private List<PagoDTO> pagos = new ArrayList<>();

    private Long tipoComprobanteId;

    public VentaDTO getVenta() {
        return venta;
    }

    public void setVenta(VentaDTO venta) {
        this.venta = venta;
    }

    public List<PagoDTO> getPagos() {
        return pagos;
    }

    public void setPagos(List<PagoDTO> pagos) {
        this.pagos = pagos;
    }

    public Long getTipoComprobanteId() {
        return tipoComprobanteId;
    }

    public void setTipoComprobanteId(Long tipoComprobanteId) {
        this.tipoComprobanteId = tipoComprobanteId;
    }
}
