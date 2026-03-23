package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EstadoVentaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static EstadoVenta getEstadoVentaSample1() {
        return new EstadoVenta().id(1L).codigo("codigo1").descripcion("descripcion1");
    }

    public static EstadoVenta getEstadoVentaSample2() {
        return new EstadoVenta().id(2L).codigo("codigo2").descripcion("descripcion2");
    }

    public static EstadoVenta getEstadoVentaRandomSampleGenerator() {
        return new EstadoVenta()
            .id(longCount.incrementAndGet())
            .codigo(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
