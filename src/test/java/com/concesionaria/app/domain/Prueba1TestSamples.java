package com.concesionaria.app.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Prueba1TestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Prueba1 getPrueba1Sample1() {
        return new Prueba1().id(1L);
    }

    public static Prueba1 getPrueba1Sample2() {
        return new Prueba1().id(2L);
    }

    public static Prueba1 getPrueba1RandomSampleGenerator() {
        return new Prueba1().id(longCount.incrementAndGet());
    }
}
