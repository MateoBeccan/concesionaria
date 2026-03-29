package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class VehiculoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Vehiculo getVehiculoSample1() {
        return new Vehiculo().id(1L).km(1).patente("patente1");
    }

    public static Vehiculo getVehiculoSample2() {
        return new Vehiculo().id(2L).km(2).patente("patente2");
    }

    public static Vehiculo getVehiculoRandomSampleGenerator() {
        return new Vehiculo().id(longCount.incrementAndGet()).km(intCount.incrementAndGet()).patente(UUID.randomUUID().toString());
    }
}
