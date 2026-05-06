package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    Optional<Reserva> findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(Long inventarioId, EstadoReserva estado);

    List<Reserva> findAllByEstadoAndFechaVencimientoBefore(EstadoReserva estado, Instant fecha);

    @Query(
        value = "select r from Reserva r where r.usuarioCreacion = ?#{authentication.name}",
        countQuery = "select count(r) from Reserva r where r.usuarioCreacion = ?#{authentication.name}"
    )
    org.springframework.data.domain.Page<Reserva> findAllCurrentUser(org.springframework.data.domain.Pageable pageable);
}
