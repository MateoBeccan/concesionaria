package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CarroceriaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Carroceria getCarroceriaSample1() {
        return new Carroceria().id(1L).nombre("nombre1").descripcion("descripcion1");
    }

    public static Carroceria getCarroceriaSample2() {
        return new Carroceria().id(2L).nombre("nombre2").descripcion("descripcion2");
    }

    public static Carroceria getCarroceriaRandomSampleGenerator() {
        return new Carroceria()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
