package com.concesionaria.app.repository;

import com.concesionaria.app.domain.EntregaUnidad;
import com.concesionaria.app.domain.enumeration.EstadoEntregaUnidad;
import java.time.Instant;
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
public interface EntregaUnidadRepository extends JpaRepository<EntregaUnidad, Long> {
    @Query("select e from EntregaUnidad e left join fetch e.venta v left join fetch v.user where e.venta.id = :ventaId")
    Optional<EntregaUnidad> findByVentaIdWithEager(@Param("ventaId") Long ventaId);

    @Query(
        value = """
        select e from EntregaUnidad e
        left join fetch e.venta v
        left join fetch v.user vu
        left join fetch e.cliente c
        left join fetch e.vehiculo vh
        left join fetch e.inventario i
        """,
        countQuery = "select count(e) from EntregaUnidad e"
    )
    Page<EntregaUnidad> findAllWithEager(Pageable pageable);

    @Query(
        value = """
        select e from EntregaUnidad e
        left join fetch e.venta v
        left join fetch v.user vu
        left join fetch e.cliente c
        left join fetch e.vehiculo vh
        left join fetch e.inventario i
        where vu.login = :login
        """,
        countQuery = """
        select count(e) from EntregaUnidad e
        join e.venta v
        left join v.user vu
        where vu.login = :login
        """
    )
    Page<EntregaUnidad> findAllCurrentUserWithEager(@Param("login") String login, Pageable pageable);

    boolean existsByVentaIdAndEstadoIn(Long ventaId, java.util.Collection<EstadoEntregaUnidad> estados);

    @Query(
        """
        select e from EntregaUnidad e
        join e.venta v
        left join v.user u
        where e.id = :id and u.login = :login
        """
    )
    Optional<EntregaUnidad> findOneByIdAndUserLogin(@Param("id") Long id, @Param("login") String login);

    @Query(
        value = """
        select e from EntregaUnidad e
        join e.cliente c
        left join e.venta v
        where (:estado is null or e.estado = :estado)
          and (:fromDate is null or e.fechaProgramada >= :fromDate)
          and (:toDate is null or e.fechaProgramada <= :toDate)
          and (:cliente is null or lower(concat(coalesce(c.apellido,''),' ',coalesce(c.nombre,''))) like lower(concat('%', :cliente, '%')))
          and (:ventaId is null or v.id = :ventaId)
        """,
        countQuery = """
        select count(e) from EntregaUnidad e
        join e.cliente c
        left join e.venta v
        where (:estado is null or e.estado = :estado)
          and (:fromDate is null or e.fechaProgramada >= :fromDate)
          and (:toDate is null or e.fechaProgramada <= :toDate)
          and (:cliente is null or lower(concat(coalesce(c.apellido,''),' ',coalesce(c.nombre,''))) like lower(concat('%', :cliente, '%')))
          and (:ventaId is null or v.id = :ventaId)
        """
    )
    Page<EntregaUnidad> searchAdmin(
        @Param("estado") EstadoEntregaUnidad estado,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("cliente") String cliente,
        @Param("ventaId") Long ventaId,
        Pageable pageable
    );

    @Query(
        value = """
        select e from EntregaUnidad e
        join e.cliente c
        join e.venta v
        left join v.user u
        where u.login = :login
          and (:estado is null or e.estado = :estado)
          and (:fromDate is null or e.fechaProgramada >= :fromDate)
          and (:toDate is null or e.fechaProgramada <= :toDate)
          and (:cliente is null or lower(concat(coalesce(c.apellido,''),' ',coalesce(c.nombre,''))) like lower(concat('%', :cliente, '%')))
          and (:ventaId is null or v.id = :ventaId)
        """,
        countQuery = """
        select count(e) from EntregaUnidad e
        join e.cliente c
        join e.venta v
        left join v.user u
        where u.login = :login
          and (:estado is null or e.estado = :estado)
          and (:fromDate is null or e.fechaProgramada >= :fromDate)
          and (:toDate is null or e.fechaProgramada <= :toDate)
          and (:cliente is null or lower(concat(coalesce(c.apellido,''),' ',coalesce(c.nombre,''))) like lower(concat('%', :cliente, '%')))
          and (:ventaId is null or v.id = :ventaId)
        """
    )
    Page<EntregaUnidad> searchCurrentUser(
        @Param("login") String login,
        @Param("estado") EstadoEntregaUnidad estado,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("cliente") String cliente,
        @Param("ventaId") Long ventaId,
        Pageable pageable
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EntregaUnidad e where e.id = :id")
    Optional<EntregaUnidad> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select e from EntregaUnidad e
        join e.venta v
        left join v.user u
        where e.id = :id and u.login = :login
        """
    )
    Optional<EntregaUnidad> findOneByIdAndUserLoginForUpdate(@Param("id") Long id, @Param("login") String login);
}