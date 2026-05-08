package com.concesionaria.app.service;

import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.service.dto.MovimientoCajaDTO;
import com.concesionaria.app.service.dto.ResumenDiarioCajaDTO;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovimientoCajaService {
    void registrarDesdePago(Pago pago, TipoMovimientoCaja tipoMovimiento, EstadoPago estadoMovimiento, boolean monetario);

    Page<MovimientoCajaDTO> findAll(
        Instant fechaDesde,
        Instant fechaHasta,
        String usuario,
        Long metodoPagoId,
        Long entidadFinancieraId,
        TipoMovimientoCaja tipo,
        EstadoPago estado,
        Pageable pageable
    );

    ResumenDiarioCajaDTO obtenerResumenDiario(LocalDate fecha);

    void delete(Long id);
}
