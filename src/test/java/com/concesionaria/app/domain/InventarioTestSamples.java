package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class InventarioTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Inventario getInventarioSample1() {
        return new Inventario().id(1L).ubicacion("ubicacion1").observaciones("observaciones1");
    }

    public static Inventario getInventarioSample2() {
        return new Inventario().id(2L).ubicacion("ubicacion2").observaciones("observaciones2");
    }

    public static Inventario getInventarioRandomSampleGenerator() {
        return new Inventario()
            .id(longCount.incrementAndGet())
            .ubicacion(UUID.randomUUID().toString())
            .observaciones(UUID.randomUUID().toString());
    }
}
