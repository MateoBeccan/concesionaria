package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TipoComprobanteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static TipoComprobante getTipoComprobanteSample1() {
        return new TipoComprobante().id(1L).codigo("codigo1").descripcion("descripcion1");
    }

    public static TipoComprobante getTipoComprobanteSample2() {
        return new TipoComprobante().id(2L).codigo("codigo2").descripcion("descripcion2");
    }

    public static TipoComprobante getTipoComprobanteRandomSampleGenerator() {
        return new TipoComprobante()
            .id(longCount.incrementAndGet())
            .codigo(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
