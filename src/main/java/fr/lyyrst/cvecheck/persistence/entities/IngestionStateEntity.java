package fr.lyyrst.cvecheck.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ingestion_state")
public class IngestionStateEntity {
    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @Column(name = "last_poll_timestamp", nullable = false)
    private Instant lastPollTimestamp;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public IngestionStateEntity() {
    }

    public IngestionStateEntity(String id, Instant lastPollTimestamp) {
        this.id = id;
        this.lastPollTimestamp = lastPollTimestamp;
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getLastPollTimestamp() {
        return lastPollTimestamp;
    }

    public void setLastPollTimestamp(Instant lastPollTimestamp) {
        this.lastPollTimestamp = lastPollTimestamp;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}