package com.concesionaria.app.repository;

import com.concesionaria.app.domain.CuotaPlanAhorro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CuotaPlanAhorroRepository extends JpaRepository<CuotaPlanAhorro, Long> {
    List<CuotaPlanAhorro> findAllByContratoIdOrderByNumeroCuotaAsc(Long contratoId);

    @Query(
        """
        select q
        from CuotaPlanAhorro q
        join q.contrato c
        left join c.user u
        where q.id = :cuotaId
          and u.login = :login
        """
    )
    Optional<CuotaPlanAhorro> findOneByIdForUser(@Param("cuotaId") Long cuotaId, @Param("login") String login);

    Optional<CuotaPlanAhorro> findFirstByPagoId(Long pagoId);
}

