package com.concesionaria.app.service;

public interface InventarioVentaSyncService {
    void sincronizarInventarioConVenta(Long ventaId, String usuarioLogin);
}

