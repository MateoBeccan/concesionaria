package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class VersionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Version getVersionSample1() {
        return new Version().id(1L).nombre("nombre1").descripcion("descripcion1").anioInicio(1).anioFin(1);
    }

    public static Version getVersionSample2() {
        return new Version().id(2L).nombre("nombre2").descripcion("descripcion2").anioInicio(2).anioFin(2);
    }

    public static Version getVersionRandomSampleGenerator() {
        return new Version()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString())
            .anioInicio(intCount.incrementAndGet())
            .anioFin(intCount.incrementAndGet());
    }
}
