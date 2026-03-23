package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MetodoPagoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static MetodoPago getMetodoPagoSample1() {
        return new MetodoPago().id(1L).codigo("codigo1").descripcion("descripcion1");
    }

    public static MetodoPago getMetodoPagoSample2() {
        return new MetodoPago().id(2L).codigo("codigo2").descripcion("descripcion2");
    }

    public static MetodoPago getMetodoPagoRandomSampleGenerator() {
        return new MetodoPago()
            .id(longCount.incrementAndGet())
            .codigo(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
