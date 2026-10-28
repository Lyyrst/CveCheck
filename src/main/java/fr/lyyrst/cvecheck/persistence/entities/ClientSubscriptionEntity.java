package fr.lyyrst.cvecheck.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "client_subscriptions")
public class ClientSubscriptionEntity {

    @EmbeddedId
    private ClientSubscriptionId id;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public ClientSubscriptionEntity() {}

    public ClientSubscriptionEntity(String clientId, String technology) {
        this.id = new ClientSubscriptionId(clientId, technology);
    }

    public ClientSubscriptionId getId() { return id; }
    public void setId(ClientSubscriptionId id) { this.id = id; }

    public Instant getCreatedAt() { return createdAt; }
}