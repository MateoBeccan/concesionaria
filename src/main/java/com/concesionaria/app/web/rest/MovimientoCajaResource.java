package com.concesionaria.app.web.rest;

import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.service.MovimientoCajaService;
import com.concesionaria.app.service.dto.MovimientoCajaDTO;
import com.concesionaria.app.service.dto.ResumenDiarioCajaDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/movimientos-caja")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class MovimientoCajaResource {

    private static final Logger LOG = LoggerFactory.getLogger(MovimientoCajaResource.class);

    private final MovimientoCajaService movimientoCajaService;

    public MovimientoCajaResource(MovimientoCajaService movimientoCajaService) {
        this.movimientoCajaService = movimientoCajaService;
    }

    @GetMapping("")
    public ResponseEntity<List<MovimientoCajaDTO>> getAllMovimientos(
        @RequestParam(required = false) Instant fechaDesde,
        @RequestParam(required = false) Instant fechaHasta,
        @RequestParam(required = false) String usuario,
        @RequestParam(required = false) Long metodoPagoId,
        @RequestParam(required = false) Long entidadFinancieraId,
        @RequestParam(required = false) TipoMovimientoCaja tipo,
        @RequestParam(required = false) EstadoPago estado,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get movimientos de caja");
        Page<MovimientoCajaDTO> page = movimientoCajaService.findAll(
            fechaDesde,
            fechaHasta,
            usuario,
            metodoPagoId,
            entidadFinancieraId,
            tipo,
            estado,
            pageable
        );
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/resumen-diario")
    public ResponseEntity<ResumenDiarioCajaDTO> getResumenDiario(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return ResponseEntity.ok(movimientoCajaService.obtenerResumenDiario(fecha));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovimiento(@PathVariable("id") Long id) {
        movimientoCajaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
