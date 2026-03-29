package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TraccionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Traccion getTraccionSample1() {
        return new Traccion().id(1L).nombre("nombre1").descripcion("descripcion1");
    }

    public static Traccion getTraccionSample2() {
        return new Traccion().id(2L).nombre("nombre2").descripcion("descripcion2");
    }

    public static Traccion getTraccionRandomSampleGenerator() {
        return new Traccion()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
