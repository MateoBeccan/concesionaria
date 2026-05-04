package com.concesionaria.app.repository;

import com.concesionaria.app.domain.VersionMotor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionMotorRepository extends JpaRepository<VersionMotor, Long> {
    boolean existsByVersionIdAndMotorId(Long versionId, Long motorId);

    Optional<VersionMotor> findByVersionIdAndMotorId(Long versionId, Long motorId);

    @Query("select vm from VersionMotor vm join fetch vm.motor m where vm.version.id = :versionId order by m.nombre asc")
    List<VersionMotor> findAllByVersionIdWithMotor(@Param("versionId") Long versionId);
}
