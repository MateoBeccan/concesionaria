package com.concesionaria.app.repository;

import com.concesionaria.app.domain.ContratoPlanAhorro;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratoPlanAhorroRepository extends JpaRepository<ContratoPlanAhorro, Long> {
    boolean existsByNumeroContrato(String numeroContrato);

    @Query(
        value = """
        select c
        from ContratoPlanAhorro c
        left join c.user u
        where u.login = :login
        """,
        countQuery = """
        select count(c)
        from ContratoPlanAhorro c
        left join c.user u
        where u.login = :login
        """
    )
    Page<ContratoPlanAhorro> findAllByUserLogin(@Param("login") String login, Pageable pageable);

    @Query(
        """
        select c
        from ContratoPlanAhorro c
        left join c.user u
        where c.id = :id
          and u.login = :login
        """
    )
    Optional<ContratoPlanAhorro> findOneByIdAndUserLogin(@Param("id") Long id, @Param("login") String login);
}

