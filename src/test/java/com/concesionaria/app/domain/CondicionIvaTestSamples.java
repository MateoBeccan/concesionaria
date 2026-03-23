package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CondicionIvaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static CondicionIva getCondicionIvaSample1() {
        return new CondicionIva().id(1L).codigo("codigo1").descripcion("descripcion1");
    }

    public static CondicionIva getCondicionIvaSample2() {
        return new CondicionIva().id(2L).codigo("codigo2").descripcion("descripcion2");
    }

    public static CondicionIva getCondicionIvaRandomSampleGenerator() {
        return new CondicionIva()
            .id(longCount.incrementAndGet())
            .codigo(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
