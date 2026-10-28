--liquibase formatted sql

--changeset lyyrst:2-insert-dev-data
--comment: Données par défaut de test (surveillance de node pour un client discord)

INSERT INTO client_subscriptions (client_id, technology)
VALUES ('dev', 'node')
    ON CONFLICT DO NOTHING;

INSERT INTO client_sync_state (client_id, last_cve_poll_at, last_article_poll_at)
VALUES ('dev', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days')
    ON CONFLICT (client_id) DO NOTHING;