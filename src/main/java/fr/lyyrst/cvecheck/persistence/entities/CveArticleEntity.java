package fr.lyyrst.cvecheck.persistence.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cve_articles", uniqueConstraints = {
        @UniqueConstraint(name = "uq_cve_article_url", columnNames = {"cve_id", "url"})
})
public class CveArticleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cve_id", referencedColumnName = "cve_id", nullable = false)
    private CveEntity cveEntity;

    @Column(name = "title", length = 512, nullable = false)
    private String title;

    @Column(name = "url", columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(name = "source", length = 64)
    private String source;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "discovered_at", insertable = false, updatable = false)
    private Instant discoveredAt;

    public CveArticleEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CveEntity getCveEntity() { return cveEntity; }
    public void setCveEntity(CveEntity cve) { this.cveEntity = cve; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public Instant getDiscoveredAt() { return discoveredAt; }
}
