package com.concesionaria.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ClienteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Cliente getClienteSample1() {
        return new Cliente()
            .id(1L)
            .nombre("nombre1")
            .apellido("apellido1")
            .nroDocumento("nroDocumento1")
            .telefono("telefono1")
            .email("email1")
            .direccion("direccion1")
            .ciudad("ciudad1")
            .provincia("provincia1")
            .pais("pais1");
    }

    public static Cliente getClienteSample2() {
        return new Cliente()
            .id(2L)
            .nombre("nombre2")
            .apellido("apellido2")
            .nroDocumento("nroDocumento2")
            .telefono("telefono2")
            .email("email2")
            .direccion("direccion2")
            .ciudad("ciudad2")
            .provincia("provincia2")
            .pais("pais2");
    }

    public static Cliente getClienteRandomSampleGenerator() {
        return new Cliente()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .apellido(UUID.randomUUID().toString())
            .nroDocumento(UUID.randomUUID().toString())
            .telefono(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .direccion(UUID.randomUUID().toString())
            .ciudad(UUID.randomUUID().toString())
            .provincia(UUID.randomUUID().toString())
            .pais(UUID.randomUUID().toString());
    }
}
