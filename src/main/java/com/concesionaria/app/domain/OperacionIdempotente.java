package com.concesionaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(
    name = "operacion_idempotente",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "ux_operacion_idempotente_usuario_tipo_key",
            columnNames = { "usuario", "tipo_operacion", "idempotency_key" }
        ),
    }
)
public class OperacionIdempotente implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "usuario", length = 100, nullable = false)
    private String usuario;

    @NotNull
    @Size(max = 50)
    @Column(name = "tipo_operacion", length = 50, nullable = false)
    private String tipoOperacion;

    @NotNull
    @Size(max = 100)
    @Column(name = "idempotency_key", length = 100, nullable = false)
    private String idempotencyKey;

    @NotNull
    @Size(max = 64)
    @Column(name = "request_hash", length = 64, nullable = false)
    private String requestHash;

    @Lob
    @Column(name = "response_data", columnDefinition = "longtext")
    private String responseData;

    @Column(name = "venta_id")
    private Long ventaId;

    @NotNull
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate = Instant.now();

    @PrePersist
    void prePersist() {
        if (createdDate == null) {
            createdDate = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}
