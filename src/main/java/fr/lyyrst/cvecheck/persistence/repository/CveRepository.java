package fr.lyyrst.cvecheck.persistence.repository;

import fr.lyyrst.cvecheck.persistence.entities.CveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface CveRepository extends JpaRepository<CveEntity, String> {

    @Query("""
        SELECT c FROM CveEntity c 
        WHERE c.technology IN :technologies 
          AND c.publishedAt > :since 
        ORDER BY c.publishedAt ASC
    """)
    List<CveEntity> findNewCvesForTechnologies(
            @Param("technologies") Collection<String> technologies,
            @Param("since") Instant since
    );
}
