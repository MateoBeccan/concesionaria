package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ModeloTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Modelo getModeloSample1() {
        return new Modelo().id(1L).nombre("nombre1").anioLanzamiento(1);
    }

    public static Modelo getModeloSample2() {
        return new Modelo().id(2L).nombre("nombre2").anioLanzamiento(2);
    }

    public static Modelo getModeloRandomSampleGenerator() {
        return new Modelo()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .anioLanzamiento(intCount.incrementAndGet());
    }
}
