package com.concesionaria.app.web.rest;

import com.concesionaria.app.service.EntidadFinancieraService;
import com.concesionaria.app.service.dto.EntidadFinancieraDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entidades-financieras")
public class EntidadFinancieraResource {

    private final EntidadFinancieraService entidadFinancieraService;

    public EntidadFinancieraResource(EntidadFinancieraService entidadFinancieraService) {
        this.entidadFinancieraService = entidadFinancieraService;
    }

    @GetMapping
    public ResponseEntity<List<EntidadFinancieraDTO>> getAllActivas() {
        return ResponseEntity.ok(entidadFinancieraService.findAllActivas());
    }
}
