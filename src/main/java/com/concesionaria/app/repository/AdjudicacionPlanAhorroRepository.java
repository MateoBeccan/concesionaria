package com.concesionaria.app.repository;

import com.concesionaria.app.domain.AdjudicacionPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoAdjudicacionPlanAhorro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjudicacionPlanAhorroRepository extends JpaRepository<AdjudicacionPlanAhorro, Long> {
    @Query(
        value = """
        select a
        from AdjudicacionPlanAhorro a
        join fetch a.contratoPlanAhorro c
        left join fetch c.cliente cli
        left join fetch c.plan p
        left join fetch a.inventario i
        left join fetch a.vehiculo v
        left join fetch a.venta ve
        left join fetch ve.user vu
        """,
        countQuery = """
        select count(a)
        from AdjudicacionPlanAhorro a
        """
    )
    Page<AdjudicacionPlanAhorro> findAllWithEagerRelationships(Pageable pageable);

    @Query(
        value = """
        select a
        from AdjudicacionPlanAhorro a
        join fetch a.contratoPlanAhorro c
        left join fetch c.user cu
        left join fetch c.cliente cli
        left join fetch c.plan p
        left join fetch a.inventario i
        left join fetch a.vehiculo v
        left join fetch a.venta ve
        left join fetch ve.user vu
        where cu.login = :login or vu.login = :login
        """,
        countQuery = """
        select count(a)
        from AdjudicacionPlanAhorro a
        join a.contratoPlanAhorro c
        left join c.user cu
        left join a.venta ve
        left join ve.user vu
        where cu.login = :login or vu.login = :login
        """
    )
    Page<AdjudicacionPlanAhorro> findAllByUserLoginWithEagerRelationships(@Param("login") String login, Pageable pageable);

    @Query(
        """
        select a
        from AdjudicacionPlanAhorro a
        join a.contratoPlanAhorro c
        where c.id = :contratoId
        order by a.id desc
        """
    )
    List<AdjudicacionPlanAhorro> findAllByContratoIdOrderByIdDesc(@Param("contratoId") Long contratoId);

    @Query(
        """
        select a
        from AdjudicacionPlanAhorro a
        join a.contratoPlanAhorro c
        left join c.user u
        where a.id = :id
          and u.login = :login
        """
    )
    Optional<AdjudicacionPlanAhorro> findOneByIdAndUserLogin(@Param("id") Long id, @Param("login") String login);

    @Query(
        """
        select a
        from AdjudicacionPlanAhorro a
        join a.contratoPlanAhorro c
        left join c.user u
        where c.id = :contratoId
          and u.login = :login
        order by a.id desc
        """
    )
    List<AdjudicacionPlanAhorro> findAllByContratoIdAndUserLogin(@Param("contratoId") Long contratoId, @Param("login") String login);

    @Query(
        """
        select count(a) > 0
        from AdjudicacionPlanAhorro a
        join a.contratoPlanAhorro c
        where c.id = :contratoId
          and a.estado in :estados
        """
    )
    boolean existsByContratoIdAndEstadoIn(@Param("contratoId") Long contratoId, @Param("estados") List<EstadoAdjudicacionPlanAhorro> estados);
}
