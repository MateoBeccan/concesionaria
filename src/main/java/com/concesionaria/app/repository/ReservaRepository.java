package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    Optional<Reserva> findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(Long inventarioId, EstadoReserva estado);

    List<Reserva> findAllByEstadoAndFechaVencimientoBefore(EstadoReserva estado, Instant fecha);
}
