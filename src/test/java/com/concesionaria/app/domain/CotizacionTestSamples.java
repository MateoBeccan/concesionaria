package com.concesionaria.app.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class CotizacionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Cotizacion getCotizacionSample1() {
        return new Cotizacion().id(1L);
    }

    public static Cotizacion getCotizacionSample2() {
        return new Cotizacion().id(2L);
    }

    public static Cotizacion getCotizacionRandomSampleGenerator() {
        return new Cotizacion().id(longCount.incrementAndGet());
    }
}
