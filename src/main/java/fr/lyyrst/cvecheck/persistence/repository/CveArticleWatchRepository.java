package fr.lyyrst.cvecheck.persistence.repository;

import fr.lyyrst.cvecheck.persistence.entities.CveArticleWatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CveArticleWatchRepository extends JpaRepository<CveArticleWatchEntity, Long> {

    Optional<CveArticleWatchEntity> findByClientIdAndId(String clientId, String cveId);

    @Query("""
                SELECT DISTINCT w.cveEntity.cveId FROM CveArticleWatchEntity w 
                WHERE w.watchUntil > :now
            """)
    List<String> findDistinctActiveWatchedCveIds(@Param("now") Instant now);

    List<CveArticleWatchEntity> findByClientId(String clientId);
}
