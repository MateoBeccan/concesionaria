package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PagoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Pago getPagoSample1() {
        return new Pago().id(1L).referencia("referencia1");
    }

    public static Pago getPagoSample2() {
        return new Pago().id(2L).referencia("referencia2");
    }

    public static Pago getPagoRandomSampleGenerator() {
        return new Pago().id(longCount.incrementAndGet()).referencia(UUID.randomUUID().toString());
    }
}
