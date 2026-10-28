package fr.lyyrst.cvecheck.persistence.repository;

import fr.lyyrst.cvecheck.persistence.entities.ClientSubscriptionEntity;
import fr.lyyrst.cvecheck.persistence.entities.ClientSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ClientSubscriptionRepository extends JpaRepository<ClientSubscriptionEntity, ClientSubscriptionId> {

    List<ClientSubscriptionEntity> findByIdClientId(String clientId);

    void deleteByIdClientIdAndIdTechnology(String clientId, String technology);

    @Query("SELECT DISTINCT s.id.technology FROM ClientSubscriptionEntity s")
    Set<String> findAllSubscribedTechnologies();

    boolean existsByIdClientIdAndIdTechnology(String clientId, String technology);
}
