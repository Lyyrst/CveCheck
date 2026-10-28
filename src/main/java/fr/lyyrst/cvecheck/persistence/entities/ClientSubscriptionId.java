package fr.lyyrst.cvecheck.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClientSubscriptionId implements Serializable {

    @Column(name = "client_id", length = 64, nullable = false)
    private String clientId;

    @Column(name = "technology", length = 64, nullable = false)
    private String technology;

    public ClientSubscriptionId() {}

    public ClientSubscriptionId(String clientId, String technology) {
        this.clientId = clientId;
        this.technology = technology;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getTechnology() { return technology; }
    public void setTechnology(String technology) { this.technology = technology; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientSubscriptionId that)) return false;
        return Objects.equals(clientId, that.clientId) && Objects.equals(technology, that.technology);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, technology);
    }
}