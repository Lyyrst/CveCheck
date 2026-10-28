package fr.lyyrst.cvecheck.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "client_sync_state")
public class ClientSyncStateEntity {

    @Id
    @Column(name = "client_id", length = 64, nullable = false)
    private String clientId;

    @Column(name = "last_cve_poll_at", nullable = false)
    private Instant lastCvePollAt;

    @Column(name = "last_article_poll_at", nullable = false)
    private Instant lastArticlePollAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public ClientSyncStateEntity() {}

    public ClientSyncStateEntity(String clientId, Instant initialTimestamp) {
        this.clientId = clientId;
        this.lastCvePollAt = initialTimestamp;
        this.lastArticlePollAt = initialTimestamp;
        this.updatedAt = Instant.now();
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public Instant getLastCvePollAt() { return lastCvePollAt; }
    public void setLastCvePollAt(Instant lastCvePollAt) { this.lastCvePollAt = lastCvePollAt; }

    public Instant getLastArticlePollAt() { return lastArticlePollAt; }
    public void setLastArticlePollAt(Instant lastArticlePollAt) { this.lastArticlePollAt = lastArticlePollAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}