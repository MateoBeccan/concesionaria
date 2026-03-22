package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AutoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Auto getAutoSample1() {
        return new Auto().id(1L).km(1).patente("patente1");
    }

    public static Auto getAutoSample2() {
        return new Auto().id(2L).km(2).patente("patente2");
    }

    public static Auto getAutoRandomSampleGenerator() {
        return new Auto().id(longCount.incrementAndGet()).km(intCount.incrementAndGet()).patente(UUID.randomUUID().toString());
    }
}
