package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ComprobanteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Comprobante getComprobanteSample1() {
        return new Comprobante().id(1L).numeroComprobante("numeroComprobante1");
    }

    public static Comprobante getComprobanteSample2() {
        return new Comprobante().id(2L).numeroComprobante("numeroComprobante2");
    }

    public static Comprobante getComprobanteRandomSampleGenerator() {
        return new Comprobante().id(longCount.incrementAndGet()).numeroComprobante(UUID.randomUUID().toString());
    }
}
