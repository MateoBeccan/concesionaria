package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@IntegrationTest
class ComprobanteIntegrityIT {

    private static final BigDecimal ONE = BigDecimal.ONE.setScale(2);
    private static final AtomicInteger SEQUENCE = new AtomicInteger(10_000);

    @Autowired
    private EntityManager em;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ComprobanteNumeradorService comprobanteNumeradorService;

    private TransactionTemplate tx;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(transactionManager);
        cleanupItData();
    }

    @AfterEach
    void tearDown() {
        cleanupItData();
    }

    @Test
    void liquibaseAplicaObjetosDeIntegridadDeComprobantes() {
        assertThat(tableExists("comprobante_correlativo")).isTrue();
        assertThat(columnExists("comprobante", "venta_emitida_id")).isTrue();
        assertThat(columnExists("comprobante", "pago_emitido_id")).isTrue();
        assertThat(indexExists("comprobante", "ux_comprobante_numero")).isTrue();
        assertThat(indexExists("comprobante", "ux_comprobante_venta_emitida_tipo")).isTrue();
        assertThat(indexExists("comprobante", "ux_comprobante_pago_emitido_tipo")).isTrue();
    }

    @Test
    void generatedColumnsVentaReflejanSoloComprobantesEmitidosSinPago() {
        Fixture fixture = createFixture(true);
        Long comprobanteId = createComprobante(fixture.ventaId(), null, fixture.tipoId(), fixture.monedaId(), EstadoComprobante.EMITIDO, numero());

        Map<String, Object> activeColumns = activeColumns(comprobanteId);
        assertThat(asLong(activeColumns.get("venta_emitida_id"))).isEqualTo(fixture.ventaId());
        assertThat(activeColumns.get("pago_emitido_id")).isNull();

        anularComprobante(comprobanteId);

        Map<String, Object> annulledColumns = activeColumns(comprobanteId);
        assertThat(annulledColumns.get("venta_emitida_id")).isNull();
        assertThat(annulledColumns.get("pago_emitido_id")).isNull();
    }

    @Test
    void generatedColumnsPagoReflejanSoloComprobantesEmitidosConPago() {
        Fixture fixture = createFixture(true);
        Long comprobanteId = createComprobante(
            fixture.ventaId(),
            fixture.pago1Id(),
            fixture.tipoId(),
            fixture.monedaId(),
            EstadoComprobante.EMITIDO,
            numero()
        );

        Map<String, Object> activeColumns = activeColumns(comprobanteId);
        assertThat(activeColumns.get("venta_emitida_id")).isNull();
        assertThat(asLong(activeColumns.get("pago_emitido_id"))).isEqualTo(fixture.pago1Id());

        anularComprobante(comprobanteId);

        Map<String, Object> annulledColumns = activeColumns(comprobanteId);
        assertThat(annulledColumns.get("venta_emitida_id")).isNull();
        assertThat(annulledColumns.get("pago_emitido_id")).isNull();
    }

    @Test
    void uniqueActivoVentaRechazaSegundoEmitidoYExponeConstraintEsperado() {
        Fixture fixture = createFixture(false);
        createComprobante(fixture.ventaId(), null, fixture.tipoId(), fixture.monedaId(), EstadoComprobante.EMITIDO, numero());

        Throwable thrown = catchThrowable(() ->
            createComprobante(fixture.ventaId(), null, fixture.tipoId(), fixture.monedaId(), EstadoComprobante.EMITIDO, numero())
        );

        assertConstraintViolation(thrown, "ux_comprobante_venta_emitida_tipo");
    }

    @Test
    void anuladosVentaHistoricosEstanPermitidos() {
        Fixture fixture = createFixture(false);

        Long first = createComprobante(fixture.ventaId(), null, fixture.tipoId(), fixture.monedaId(), EstadoComprobante.ANULADO, numero());
        Long second = createComprobante(fixture.ventaId(), null, fixture.tipoId(), fixture.monedaId(), EstadoComprobante.ANULADO, numero());

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
    }

    @Test
    void reemisionVentaPermiteNuevoEmitidoTrasAnulacion() {
        Fixture fixture = createFixture(false);
        Long first = createComprobante(fixture.ventaId(), null, fixture.tipoId(), fixture.monedaId(), EstadoComprobante.EMITIDO, numero());

        anularComprobante(first);
        Long second = createComprobante(fixture.ventaId(), null, fixture.tipoId(), fixture.monedaId(), EstadoComprobante.EMITIDO, numero());

        assertThat(second).isNotNull();
        assertThat(countComprobantes(fixture.ventaId(), null, fixture.tipoId(), EstadoComprobante.EMITIDO)).isEqualTo(1);
        assertThat(countComprobantes(fixture.ventaId(), null, fixture.tipoId(), EstadoComprobante.ANULADO)).isEqualTo(1);
    }

    @Test
    void uniqueActivoPagoRechazaSegundoEmitidoYExponeConstraintEsperado() {
        Fixture fixture = createFixture(true);
        createComprobante(fixture.ventaId(), fixture.pago1Id(), fixture.tipoId(), fixture.monedaId(), EstadoComprobante.EMITIDO, numero());

        Throwable thrown = catchThrowable(() ->
            createComprobante(fixture.ventaId(), fixture.pago1Id(), fixture.tipoId(), fixture.monedaId(), EstadoComprobante.EMITIDO, numero())
        );

        assertConstraintViolation(thrown, "ux_comprobante_pago_emitido_tipo");
    }

    @Test
    void anuladosPagoHistoricosEstanPermitidos() {
        Fixture fixture = createFixture(true);

        Long first = createComprobante(
            fixture.ventaId(),
            fixture.pago1Id(),
            fixture.tipoId(),
            fixture.monedaId(),
            EstadoComprobante.ANULADO,
            numero()
        );
        Long second = createComprobante(
            fixture.ventaId(),
            fixture.pago1Id(),
            fixture.tipoId(),
            fixture.monedaId(),
            EstadoComprobante.ANULADO,
            numero()
        );

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
    }

    @Test
    void reemisionPagoPermiteNuevoEmitidoTrasAnulacion() {
        Fixture fixture = createFixture(true);
        Long first = createComprobante(
            fixture.ventaId(),
            fixture.pago1Id(),
            fixture.tipoId(),
            fixture.monedaId(),
            EstadoComprobante.EMITIDO,
            numero()
        );

        anularComprobante(first);
        Long second = createComprobante(
            fixture.ventaId(),
            fixture.pago1Id(),
            fixture.tipoId(),
            fixture.monedaId(),
            EstadoComprobante.EMITIDO,
            numero()
        );

        assertThat(second).isNotNull();
        assertThat(countComprobantes(fixture.ventaId(), fixture.pago1Id(), fixture.tipoId(), EstadoComprobante.EMITIDO)).isEqualTo(1);
        assertThat(countComprobantes(fixture.ventaId(), fixture.pago1Id(), fixture.tipoId(), EstadoComprobante.ANULADO)).isEqualTo(1);
    }

    @Test
    void variosPagosDeLaMismaVentaPermitenComprobantesEmitidosDelMismoTipo() {
        Fixture fixture = createFixture(true);

        Long first = createComprobante(
            fixture.ventaId(),
            fixture.pago1Id(),
            fixture.tipoId(),
            fixture.monedaId(),
            EstadoComprobante.EMITIDO,
            numero()
        );
        Long second = createComprobante(
            fixture.ventaId(),
            fixture.pago2Id(),
            fixture.tipoId(),
            fixture.monedaId(),
            EstadoComprobante.EMITIDO,
            numero()
        );

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(countComprobantes(fixture.ventaId(), null, fixture.tipoId(), EstadoComprobante.EMITIDO)).isZero();
    }

    @Test
    void uniqueNumeroComprobanteRechazaDuplicadoYExponeConstraintEsperado() {
        Fixture firstFixture = createFixture(false);
        Fixture secondFixture = createFixture(false);
        String duplicatedNumber = numero();
        createComprobante(
            firstFixture.ventaId(),
            null,
            firstFixture.tipoId(),
            firstFixture.monedaId(),
            EstadoComprobante.EMITIDO,
            duplicatedNumber
        );

        Throwable thrown = catchThrowable(() ->
            createComprobante(
                secondFixture.ventaId(),
                null,
                secondFixture.tipoId(),
                secondFixture.monedaId(),
                EstadoComprobante.EMITIDO,
                duplicatedNumber
            )
        );

        assertConstraintViolation(thrown, "ux_comprobante_numero");
    }

    @Test
    void numeradorConcurrenteNoDuplicaNumeros() throws Exception {
        Long tipoId = createTipoComprobante("IT-CONC-" + next());

        List<Long> numbers = runConcurrentNumerador(tipoId, 4);

        assertThat(numbers).containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
        assertThat(ultimoNumero(tipoId)).isEqualTo(4L);
    }

    @Test
    void rollbackNoConsumeCorrelativo() {
        Long tipoId = createTipoComprobante("IT-RB-" + next());

        assertThrows(IllegalStateException.class, () ->
            tx.executeWithoutResult(status -> {
                assertThat(comprobanteNumeradorService.siguienteNumero(tipoId)).isEqualTo(1L);
                throw new IllegalStateException("rollback deliberado");
            })
        );

        Long numberAfterRollback = tx.execute(status -> comprobanteNumeradorService.siguienteNumero(tipoId));

        assertThat(numberAfterRollback).isEqualTo(1L);
        assertThat(ultimoNumero(tipoId)).isEqualTo(1L);
    }

    private Fixture createFixture(boolean withPayments) {
        return tx.execute(status -> {
            int suffix = next();
            Moneda moneda = new Moneda().codigo("ITM" + suffix).descripcion("IT moneda " + suffix).simbolo("$").activo(true);
            em.persist(moneda);

            TipoComprobante tipo = new TipoComprobante().codigo("IT-TC-" + suffix).descripcion("IT tipo " + suffix);
            em.persist(tipo);

            User user = new User();
            user.setLogin("itfinance" + suffix);
            user.setPassword("x".repeat(60));
            user.setEmail("it-finance-" + suffix + "@example.test");
            user.setActivated(true);
            user.setLangKey("es");
            em.persist(user);

            Cliente cliente = new Cliente()
                .nombre("IT")
                .apellido("Cliente")
                .nroDocumento(String.valueOf(20_000_000 + suffix))
                .email("it-cliente-" + suffix + "@example.test")
                .activo(true)
                .fechaAlta(Instant.now());
            em.persist(cliente);

            Vehiculo vehiculo = new Vehiculo()
                .estado(EstadoVehiculo.NUEVO)
                .fechaFabricacion(LocalDate.of(2024, 1, 1))
                .km(0)
                .vinChasis("ITVIN" + suffix)
                .precio(new BigDecimal("1000.00"))
                .moneda(moneda);
            em.persist(vehiculo);

            Venta venta = new Venta()
                .fecha(Instant.now())
                .cotizacion(BigDecimal.ONE)
                .importeNeto(new BigDecimal("1000.00"))
                .impuesto(new BigDecimal("210.00"))
                .total(new BigDecimal("1210.00"))
                .porcentajeImpuesto(new BigDecimal("21.00"))
                .totalPagado(new BigDecimal("1210.00"))
                .saldo(BigDecimal.ZERO.setScale(2))
                .observaciones("IT-VENTA-" + suffix)
                .cliente(cliente)
                .estado(EstadoVenta.PAGADA)
                .moneda(moneda)
                .user(user);
            venta.setFechaCotizacionUsada(Instant.now());
            venta.setVehiculo(vehiculo);
            em.persist(venta);

            Long pago1Id = null;
            Long pago2Id = null;
            if (withPayments) {
                Pago pago1 = pago(venta, moneda, "IT-PAGO-" + suffix + "-1");
                em.persist(pago1);
                Pago pago2 = pago(venta, moneda, "IT-PAGO-" + suffix + "-2");
                em.persist(pago2);
                em.flush();
                pago1Id = pago1.getId();
                pago2Id = pago2.getId();
            } else {
                em.flush();
            }

            return new Fixture(moneda.getId(), tipo.getId(), venta.getId(), pago1Id, pago2Id);
        });
    }

    private Pago pago(Venta venta, Moneda moneda, String referencia) {
        Pago pago = new Pago()
            .monto(new BigDecimal("100.00"))
            .fecha(Instant.now())
            .referencia(referencia)
            .createdDate(Instant.now())
            .venta(venta)
            .moneda(moneda)
            .estado(EstadoPago.REGISTRADO);
        pago.setTipoMovimiento(TipoMovimientoPago.PAGO_RECIBIDO);
        pago.setCotizacionUsada(BigDecimal.ONE);
        pago.setMontoAplicadoVenta(new BigDecimal("100.00"));
        pago.setFechaCotizacionUsada(Instant.now());
        pago.setUsuarioRegistro("it");
        return pago;
    }

    private Long createTipoComprobante(String codigo) {
        return tx.execute(status -> {
            TipoComprobante tipo = new TipoComprobante().codigo(codigo).descripcion("IT numerador");
            em.persist(tipo);
            em.flush();
            return tipo.getId();
        });
    }

    private Long createComprobante(Long ventaId, Long pagoId, Long tipoId, Long monedaId, EstadoComprobante estado, String numero) {
        return tx.execute(status -> {
            Comprobante comprobante = new Comprobante()
                .numeroComprobante(numero)
                .fechaEmision(Instant.now())
                .importeNeto(ONE)
                .impuesto(BigDecimal.ZERO.setScale(2))
                .total(ONE)
                .createdDate(Instant.now())
                .estado(estado)
                .venta(em.getReference(Venta.class, ventaId))
                .tipoComprobante(em.getReference(TipoComprobante.class, tipoId))
                .moneda(em.getReference(Moneda.class, monedaId));
            if (pagoId != null) {
                comprobante.setPago(em.getReference(Pago.class, pagoId));
            }
            em.persist(comprobante);
            em.flush();
            return comprobante.getId();
        });
    }

    private void anularComprobante(Long comprobanteId) {
        tx.executeWithoutResult(status -> {
            Comprobante comprobante = em.find(Comprobante.class, comprobanteId);
            comprobante.setEstado(EstadoComprobante.ANULADO);
            comprobante.setFechaAnulacion(Instant.now());
            comprobante.setMotivoAnulacion("IT anulacion");
            em.flush();
        });
    }

    private List<Long> runConcurrentNumerador(Long tipoId, int threadCount) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                tasks.add(() -> {
                    ready.countDown();
                    assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                    return tx.execute(status -> comprobanteNumeradorService.siguienteNumero(tipoId));
                });
            }
            List<Future<Long>> futures = tasks.stream().map(executor::submit).toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<Long> numbers = new ArrayList<>();
            for (Future<Long> future : futures) {
                numbers.add(future.get(30, TimeUnit.SECONDS));
            }
            return numbers;
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    private void assertConstraintViolation(Throwable thrown, String constraintName) {
        assertThat(thrown).isNotNull();

        List<Throwable> causes = causeChain(thrown);
        System.out.println("Observed " + constraintName + ": " + describeThrowable(thrown));

        assertThat(causes)
            .anyMatch(cause ->
                cause instanceof DataIntegrityViolationException ||
                cause instanceof ConstraintViolationException ||
                cause instanceof SQLIntegrityConstraintViolationException
            );
        assertThat(causes).anyMatch(cause -> constraintName.equals(hibernateConstraintName(cause)) || messageContains(cause, constraintName));
    }

    private List<Throwable> causeChain(Throwable throwable) {
        List<Throwable> causes = new ArrayList<>();
        Throwable current = throwable;
        while (current != null) {
            causes.add(current);
            current = current.getCause();
        }
        return causes;
    }

    private String describeThrowable(Throwable throwable) {
        StringBuilder description = new StringBuilder();
        for (Throwable cause : causeChain(throwable)) {
            if (!description.isEmpty()) {
                description.append(" -> ");
            }
            description.append(cause.getClass().getSimpleName()).append(": ").append(cause.getMessage());
        }
        return description.toString();
    }

    private String hibernateConstraintName(Throwable throwable) {
        if (throwable instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintName();
        }
        return null;
    }

    private boolean messageContains(Throwable throwable, String expected) {
        String message = throwable.getMessage();
        return message != null && message.contains(expected);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.tables where table_schema = database() and table_name = ?",
            Integer.class,
            tableName
        );
        return count != null && count == 1;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.columns where table_schema = database() and table_name = ? and column_name = ?",
            Integer.class,
            tableName,
            columnName
        );
        return count != null && count == 1;
    }

    private boolean indexExists(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.statistics where table_schema = database() and table_name = ? and index_name = ?",
            Integer.class,
            tableName,
            indexName
        );
        return count != null && count > 0;
    }

    private Map<String, Object> activeColumns(Long comprobanteId) {
        return jdbcTemplate.queryForMap(
            "select venta_emitida_id, pago_emitido_id from comprobante where id = ?",
            comprobanteId
        );
    }

    private long countComprobantes(Long ventaId, Long pagoId, Long tipoId, EstadoComprobante estado) {
        String pagoPredicate = pagoId == null ? "pago_id is null" : "pago_id = ?";
        List<Object> args = new ArrayList<>();
        args.add(ventaId);
        if (pagoId != null) {
            args.add(pagoId);
        }
        args.add(tipoId);
        args.add(estado.name());
        Long count = jdbcTemplate.queryForObject(
            "select count(*) from comprobante where venta_id = ? and " + pagoPredicate + " and tipo_comprobante_id = ? and estado = ?",
            Long.class,
            args.toArray()
        );
        return count == null ? 0 : count;
    }

    private Long ultimoNumero(Long tipoId) {
        return jdbcTemplate.queryForObject(
            "select ultimo_numero from comprobante_correlativo where tipo_comprobante_id = ?",
            Long.class,
            tipoId
        );
    }

    private Long asLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private String numero() {
        return "IT-" + next();
    }

    private int next() {
        return SEQUENCE.incrementAndGet();
    }

    private void cleanupItData() {
        em.clear();
        TransactionTemplate cleanupTx = tx != null ? tx : new TransactionTemplate(transactionManager);
        cleanupTx.executeWithoutResult(status -> {
            List<Long> comprobanteIds = itComprobanteIds();
            jdbcTemplate.update("update cuota_plan_ahorro set comprobante_id = null where comprobante_id in (" + placeholders(comprobanteIds) + ")", comprobanteIds.toArray());
            deleteByIds("delete from comprobante where id = ?", comprobanteIds);

            List<Long> pagoIds = itPagoIds();
            List<Long> remainingComprobanteIds = queryIds(
                "select id from comprobante where pago_id in (" + placeholders(pagoIds) + ")",
                pagoIds.toArray()
            );
            jdbcTemplate.update(
                "update cuota_plan_ahorro set comprobante_id = null where comprobante_id in (" + placeholders(remainingComprobanteIds) + ")",
                remainingComprobanteIds.toArray()
            );
            deleteByIds("delete from comprobante where id = ?", remainingComprobanteIds);

            deleteByIds("delete from movimiento_caja where pago_id = ?", pagoIds);
            deleteByIds("delete from comprobante_plan_ahorro where pago_id = ?", pagoIds);
            jdbcTemplate.update("update cuota_plan_ahorro set pago_id = null where pago_id in (" + placeholders(pagoIds) + ")", pagoIds.toArray());
            jdbcTemplate.update("update comprobante set pago_id = null where pago_id in (" + placeholders(pagoIds) + ")", pagoIds.toArray());
            deleteByIds("delete from pago where id = ?", pagoIds);

            List<Long> ventaIds = itVentaIds();
            deleteByIds("delete from movimiento_caja where venta_id = ?", ventaIds);
            deleteByIds("delete from detalle_venta where venta_id = ?", ventaIds);
            deleteByIds("delete from venta_historial where venta_id = ?", ventaIds);
            deleteByIds("delete from inventario_historial where venta_id = ?", ventaIds);
            deleteByIds("delete from operacion_idempotente where venta_id = ?", ventaIds);
            deleteByIds("delete from entrega_unidad where venta_id = ?", ventaIds);
            deleteByIds("delete from reserva where venta_id = ?", ventaIds);
            deleteByIds("delete from tasacion_usado where venta_id = ?", ventaIds);
            deleteByIds("delete from adjudicacion_plan_ahorro where venta_id = ?", ventaIds);
            deleteByIds("delete from venta where id = ?", ventaIds);

            jdbcTemplate.update("delete from comprobante_correlativo where tipo_comprobante_id in (select id from tipo_comprobante where codigo like 'IT-%')");
            jdbcTemplate.update("delete from vehiculo where vin_chasis like 'ITVIN%'");
            jdbcTemplate.update("delete from cliente where email like 'it-cliente-%@example.test'");
            jdbcTemplate.update("delete from tipo_comprobante where codigo like 'IT-%'");
            jdbcTemplate.update("delete from moneda where codigo like 'ITM%'");
            jdbcTemplate.update("delete from jhi_user where login like 'itfinance%'");
        });
        em.clear();
    }

    private List<Long> itComprobanteIds() {
        return queryIds(
            """
            select distinct c.id
            from comprobante c
            left join pago p on p.id = c.pago_id
            left join venta cv on cv.id = c.venta_id
            left join venta pv on pv.id = p.venta_id
            left join tipo_comprobante tc on tc.id = c.tipo_comprobante_id
            where c.numero_comprobante like 'IT-%'
               or p.referencia like 'IT-PAGO-%'
               or cv.observaciones like 'IT-VENTA-%'
               or pv.observaciones like 'IT-VENTA-%'
               or tc.codigo like 'IT-%'
            """
        );
    }

    private List<Long> itPagoIds() {
        return queryIds(
            """
            select distinct p.id
            from pago p
            left join venta v on v.id = p.venta_id
            where p.referencia like 'IT-PAGO-%'
               or v.observaciones like 'IT-VENTA-%'
            """
        );
    }

    private List<Long> itVentaIds() {
        return queryIds("select id from venta where observaciones like 'IT-VENTA-%'");
    }

    private List<Long> queryIds(String sql, Object... args) {
        if (sql.contains("()")) {
            return List.of();
        }
        return jdbcTemplate.queryForList(sql, Long.class, args);
    }

    private void deleteByIds(String sql, List<Long> ids) {
        for (Long id : ids) {
            jdbcTemplate.update(sql, id);
        }
    }

    private String placeholders(List<Long> ids) {
        if (ids.isEmpty()) {
            return "null";
        }
        return "?,".repeat(ids.size() - 1) + "?";
    }

    private record Fixture(Long monedaId, Long tipoId, Long ventaId, Long pago1Id, Long pago2Id) {}
}
