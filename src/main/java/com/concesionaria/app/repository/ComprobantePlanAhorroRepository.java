package com.concesionaria.app.repository;

import com.concesionaria.app.domain.ComprobantePlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComprobantePlanAhorroRepository extends JpaRepository<ComprobantePlanAhorro, Long> {
    boolean existsByCuotaPlanAhorroIdAndEstado(Long cuotaId, EstadoComprobante estado);

    boolean existsByPagoIdAndEstado(Long pagoId, EstadoComprobante estado);

    @Query("select coalesce(max(c.id), 0) from ComprobantePlanAhorro c")
    Long nextCorrelativoBase();

    Optional<ComprobantePlanAhorro> findFirstByPagoIdAndEstadoOrderByIdDesc(Long pagoId, EstadoComprobante estado);

    List<ComprobantePlanAhorro> findAllByCuotaPlanAhorroIdOrderByIdDesc(Long cuotaId);

    @Query(
        """
        select c
        from ComprobantePlanAhorro c
        join c.contratoPlanAhorro cp
        left join cp.user u
        where c.id = :id and u.login = :login
        """
    )
    Optional<ComprobantePlanAhorro> findForUser(@Param("id") Long id, @Param("login") String login);
}

