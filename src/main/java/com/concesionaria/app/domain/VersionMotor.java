package com.concesionaria.app.domain;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(
    name = "version_motor",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_version_motor_version_motor", columnNames = { "version_id", "motor_id" }),
    }
)
public class VersionMotor implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private Version version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "motor_id", nullable = false)
    private Motor motor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Motor getMotor() {
        return motor;
    }

    public void setMotor(Motor motor) {
        this.motor = motor;
    }
}
