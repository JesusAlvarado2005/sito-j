-- V1 - Schema "contact" (vedi docs/ARCHITETTURA.md, sezione 3.3)

CREATE TABLE contact_message (
    id           UUID         PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    subject      VARCHAR(150) NOT NULL,
    body         TEXT         NOT NULL CHECK (length(body) <= 5000),
    lang         CHAR(2)      NOT NULL CHECK (lang IN ('it', 'en')),
    ip_hash      VARCHAR(64)  NOT NULL,                 -- SHA-256(sale + IP): mai l'IP in chiaro
    status       VARCHAR(20)  NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'READ', 'ARCHIVED', 'SPAM')),
    notified_at  TIMESTAMPTZ,                           -- NULL = email di notifica ancora da inviare
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Indice parziale: contiene solo i messaggi da notificare (pochi), la query del job resta veloce
CREATE INDEX idx_contact_message_pending ON contact_message (created_at) WHERE notified_at IS NULL;
CREATE INDEX idx_contact_message_status ON contact_message (status, created_at DESC);
