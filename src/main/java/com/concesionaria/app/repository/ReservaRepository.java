package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    Optional<Reserva> findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(Long inventarioId, EstadoReserva estado);

    List<Reserva> findAllByEstadoAndFechaVencimientoBefore(EstadoReserva estado, Instant fecha);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select distinct r
        from Reserva r
        left join fetch r.inventario i
        left join fetch i.vehiculo
        left join fetch r.cliente
        where r.estado = :estado
          and r.fechaVencimiento < :now
        order by r.fechaVencimiento asc, r.id asc
        """
    )
    List<Reserva> findAllActivasVencidasForUpdate(@Param("estado") EstadoReserva estado, @Param("now") Instant now);

    @Query(value = "select r from Reserva r where r.usuarioCreacion = :login", countQuery = "select count(r) from Reserva r where r.usuarioCreacion = :login")
    Page<Reserva> findAllCurrentUser(@Param("login") String login, Pageable pageable);

    @Query("select (count(r) > 0) from Reserva r where r.id = :reservaId and r.usuarioCreacion = :login")
    boolean existsAccessibleByIdForUser(@Param("reservaId") Long reservaId, @Param("login") String login);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select r
        from Reserva r
        where r.inventario.id = :inventarioId
          and r.estado = :estado
        order by r.fechaReserva desc
        """
    )
    List<Reserva> findAllByInventarioIdAndEstadoForUpdateOrderByFechaReservaDesc(
        @Param("inventarioId") Long inventarioId,
        @Param("estado") EstadoReserva estado
    );
}
