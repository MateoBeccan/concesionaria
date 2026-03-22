package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CombustibleTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Combustible getCombustibleSample1() {
        return new Combustible().id(1L).nombre("nombre1").descripcion("descripcion1");
    }

    public static Combustible getCombustibleSample2() {
        return new Combustible().id(2L).nombre("nombre2").descripcion("descripcion2");
    }

    public static Combustible getCombustibleRandomSampleGenerator() {
        return new Combustible()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
