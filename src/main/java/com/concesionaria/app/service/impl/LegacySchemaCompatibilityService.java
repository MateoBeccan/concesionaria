package com.concesionaria.app.service.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LegacySchemaCompatibilityService {

    private final JdbcTemplate jdbcTemplate;
    private final AtomicBoolean inventarioDisponibleChecked = new AtomicBoolean(false);

    public LegacySchemaCompatibilityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void ensureInventarioDisponibleDefault() {
        if (inventarioDisponibleChecked.get()) {
            return;
        }

        Integer columnCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'inventario'
              AND column_name = 'disponible'
            """,
            Integer.class
        );

        if (columnCount == null || columnCount == 0) {
            inventarioDisponibleChecked.set(true);
            return;
        }

        String defaultValue = jdbcTemplate.queryForObject(
            """
            SELECT column_default
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'inventario'
              AND column_name = 'disponible'
            """,
            String.class
        );

        if (defaultValue == null) {
            jdbcTemplate.execute("ALTER TABLE inventario MODIFY COLUMN disponible BOOLEAN NOT NULL DEFAULT TRUE");
        }
        jdbcTemplate.update("UPDATE inventario SET disponible = TRUE WHERE disponible IS NULL");
        inventarioDisponibleChecked.set(true);
    }
}
