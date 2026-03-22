package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Version;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface VersionRepositoryWithBagRelationships {
    Optional<Version> fetchBagRelationships(Optional<Version> version);

    List<Version> fetchBagRelationships(List<Version> versions);

    Page<Version> fetchBagRelationships(Page<Version> versions);
}
