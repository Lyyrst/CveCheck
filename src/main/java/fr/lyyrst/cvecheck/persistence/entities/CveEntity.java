package fr.lyyrst.cvecheck.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "cves")
public class CveEntity {

    @Id
    @Column(name = "cve_id", length = 32, nullable = false)
    private String cveId;

    @Column(name = "technology", length = 64, nullable = false)
    private String technology;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "cvss_score")
    private Float cvssScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public CveEntity() {
    }

    public String getCveId() {
        return cveId;
    }

    public void setCveId(String cveId) {
        this.cveId = cveId;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Float getCvssScore() {
        return cvssScore;
    }

    public void setCvssScore(Float cvssScore) {
        this.cvssScore = cvssScore;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}