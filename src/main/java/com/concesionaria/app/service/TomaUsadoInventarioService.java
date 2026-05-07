package com.concesionaria.app.service;

import com.concesionaria.app.domain.Venta;

public interface TomaUsadoInventarioService {
    void generarSiCorresponde(Venta venta, String usuarioLogin);
}

