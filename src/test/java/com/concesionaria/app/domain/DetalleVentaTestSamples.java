package com.concesionaria.app.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DetalleVentaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static DetalleVenta getDetalleVentaSample1() {
        return new DetalleVenta().id(1L).cantidad(1);
    }

    public static DetalleVenta getDetalleVentaSample2() {
        return new DetalleVenta().id(2L).cantidad(2);
    }

    public static DetalleVenta getDetalleVentaRandomSampleGenerator() {
        return new DetalleVenta().id(longCount.incrementAndGet()).cantidad(intCount.incrementAndGet());
    }
}
