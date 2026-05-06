package com.concesionaria.app.repository;

import com.concesionaria.app.domain.TasacionUsado;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TasacionUsadoRepository extends JpaRepository<TasacionUsado, Long> {
    @Query(
        value = """
        select t
        from TasacionUsado t
        left join t.tasadorUser tu
        where tu.login = ?#{authentication.name}
           or t.usuarioTasador = ?#{authentication.name}
        """,
        countQuery = """
        select count(t)
        from TasacionUsado t
        left join t.tasadorUser tu
        where tu.login = ?#{authentication.name}
           or t.usuarioTasador = ?#{authentication.name}
        """
    )
    Page<TasacionUsado> findAllCurrentUser(Pageable pageable);

    List<TasacionUsado> findAllByClienteIdOrderByFechaTasacionDesc(Long clienteId);

    List<TasacionUsado> findAllByClienteIdAndEstadoOrderByFechaTasacionDesc(Long clienteId, com.concesionaria.app.domain.enumeration.EstadoTasacionUsado estado);

    @Query(
        """
        select t
        from TasacionUsado t
        where t.cliente.id = :clienteId
          and t.estado = com.concesionaria.app.domain.enumeration.EstadoTasacionUsado.ACEPTADA
          and t.inventarioGenerado is null
          and not exists (
            select 1 from Venta v
            where v.tasacionUsado.id = t.id
          )
        order by t.fechaTasacion desc
        """
    )
    List<TasacionUsado> findAceptadasDisponiblesByClienteId(@Param("clienteId") Long clienteId);

    @Query(
        """
        select (count(t) > 0)
        from TasacionUsado t
        where t.id = :tasacionId
          and t.cliente.id = :clienteId
          and t.estado = com.concesionaria.app.domain.enumeration.EstadoTasacionUsado.ACEPTADA
          and t.inventarioGenerado is null
          and not exists (
            select 1 from Venta v
            where v.tasacionUsado.id = t.id
          )
        """
    )
    boolean existsAceptadaDisponibleByIdAndClienteId(@Param("tasacionId") Long tasacionId, @Param("clienteId") Long clienteId);
}
