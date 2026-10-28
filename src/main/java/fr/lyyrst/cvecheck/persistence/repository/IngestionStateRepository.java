package fr.lyyrst.cvecheck.persistence.repository;

import fr.lyyrst.cvecheck.persistence.entities.IngestionStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngestionStateRepository extends JpaRepository<IngestionStateEntity, String> {
}