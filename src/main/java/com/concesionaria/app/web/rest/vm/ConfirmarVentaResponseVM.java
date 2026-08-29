package com.concesionaria.app.web.rest.vm;

import com.concesionaria.app.service.dto.ComprobanteDTO;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import java.util.ArrayList;
import java.util.List;

public class ConfirmarVentaResponseVM {

    private VentaDTO venta;

    private List<PagoDTO> pagos = new ArrayList<>();

    private ComprobanteDTO comprobante;

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

    public ComprobanteDTO getComprobante() {
        return comprobante;
    }

    public void setComprobante(ComprobanteDTO comprobante) {
        this.comprobante = comprobante;
    }
}
