package fr.lyyrst.cvecheck.persistence.repository;

import fr.lyyrst.cvecheck.persistence.entities.CveArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CveArticleRepository extends JpaRepository<CveArticleEntity, Long> {

    boolean existsByIdAndUrl(String cveId, String url);

    @Query("""
        SELECT a FROM CveArticleEntity a 
        JOIN CveArticleWatchEntity w ON w.cveEntity.cveId = a.cveEntity.cveId
        WHERE w.clientId = :clientId 
          AND a.discoveredAt > :since 
        ORDER BY a.publishedAt ASC
    """)
    List<CveArticleEntity> findNewArticlesForClient(
            @Param("clientId") String clientId,
            @Param("since") Instant since
    );
}
