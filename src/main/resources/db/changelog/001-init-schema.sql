--liquibase formatted sql

--changeset lyyrst:1-init-schema
--comment: Initialisation du schema cvecheck

-- 1. Curseur d'ingestion globale de l'API CVE.org
CREATE TABLE ingestion_state
(
    id                  VARCHAR(32) PRIMARY KEY,
    last_poll_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO ingestion_state (id, last_poll_timestamp)
VALUES ('CVE_ORG_GLOBAL', CURRENT_TIMESTAMP - INTERVAL '7 days');


-- 2. Référentiel des CVE publiées
CREATE TABLE cves
(
    cve_id       VARCHAR(32) PRIMARY KEY,
    technology   VARCHAR(64)              NOT NULL,
    description  TEXT,
    published_at TIMESTAMP WITH TIME ZONE,
    cvss_score   REAL,
    raw_payload  JSONB,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cves_technology_published_at ON cves (technology, published_at);
CREATE INDEX idx_cves_published_at ON cves (published_at);


-- 3. Abonnements des clients aux technologies cibles
CREATE TABLE client_subscriptions
(
    client_id  VARCHAR(64) NOT NULL,
    technology VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (client_id, technology)
);

CREATE INDEX idx_client_subscriptions_tech ON client_subscriptions (technology);


-- 4. Curseur temporel de synchronisation par client
CREATE TABLE client_sync_state
(
    client_id            VARCHAR(64) PRIMARY KEY,
    last_cve_poll_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    last_article_poll_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- 5. Tâches de surveillance d'articles sur une CVE
CREATE TABLE cve_article_watches
(
    id              BIGSERIAL PRIMARY KEY,
    client_id       VARCHAR(64)              NOT NULL,
    cve_id          VARCHAR(32)              NOT NULL REFERENCES cves (cve_id) ON DELETE CASCADE,
    watch_until     TIMESTAMP WITH TIME ZONE NOT NULL,
    last_checked_at TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_client_cve_watch UNIQUE (client_id, cve_id)
);

CREATE INDEX idx_cve_article_watches_watch_until ON cve_article_watches (watch_until);


-- 6. Articles collectés pour une CVE
CREATE TABLE cve_articles
(
    id            BIGSERIAL PRIMARY KEY,
    cve_id        VARCHAR(32)              NOT NULL REFERENCES cves (cve_id) ON DELETE CASCADE,
    title         VARCHAR(512)             NOT NULL,
    url           TEXT                     NOT NULL,
    source        VARCHAR(64),
    published_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    discovered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cve_article_url UNIQUE (cve_id, url)
);

CREATE INDEX idx_cve_articles_cve_published ON cve_articles (cve_id, published_at);