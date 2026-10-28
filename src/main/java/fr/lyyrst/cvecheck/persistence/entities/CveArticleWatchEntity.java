package fr.lyyrst.cvecheck.persistence.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cve_article_watches", uniqueConstraints = {
        @UniqueConstraint(name = "uq_client_cve_watch", columnNames = {"client_id", "cve_id"})
})
public class CveArticleWatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", length = 64, nullable = false)
    private String clientId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cve_id", referencedColumnName = "cve_id", nullable = false)
    private CveEntity cveEntity;

    @Column(name = "watch_until", nullable = false)
    private Instant watchUntil;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public CveArticleWatchEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public CveEntity getCveEntity() { return cveEntity; }
    public void setCveEntity(CveEntity cve) { this.cveEntity = cve; }

    public Instant getWatchUntil() { return watchUntil; }
    public void setWatchUntil(Instant watchUntil) { this.watchUntil = watchUntil; }

    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(Instant lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }

    public Instant getCreatedAt() { return createdAt; }
}
