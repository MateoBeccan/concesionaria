package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TipoVehiculoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static TipoVehiculo getTipoVehiculoSample1() {
        return new TipoVehiculo().id(1L).nombre("nombre1").descripcion("descripcion1");
    }

    public static TipoVehiculo getTipoVehiculoSample2() {
        return new TipoVehiculo().id(2L).nombre("nombre2").descripcion("descripcion2");
    }

    public static TipoVehiculo getTipoVehiculoRandomSampleGenerator() {
        return new TipoVehiculo()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString());
    }
}
