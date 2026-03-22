package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MotorTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Motor getMotorSample1() {
        return new Motor().id(1L).nombre("nombre1").cilindradaCc(1).cilindroCant(1).potenciaHp(1);
    }

    public static Motor getMotorSample2() {
        return new Motor().id(2L).nombre("nombre2").cilindradaCc(2).cilindroCant(2).potenciaHp(2);
    }

    public static Motor getMotorRandomSampleGenerator() {
        return new Motor()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .cilindradaCc(intCount.incrementAndGet())
            .cilindroCant(intCount.incrementAndGet())
            .potenciaHp(intCount.incrementAndGet());
    }
}
