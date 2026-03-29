package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TipoCajaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static TipoCaja getTipoCajaSample1() {
        return new TipoCaja().id(1L).nombre("nombre1").descripcion("descripcion1");
    }

    public static TipoCaja getTipoCajaSample2() {
        return new TipoCaja().id(2L).nombre("nombre2").descripcion("descripcion2");
    }

    public static TipoCaja getTipoCajaRandomSampleGenerator() {
        return new TipoCaja()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
