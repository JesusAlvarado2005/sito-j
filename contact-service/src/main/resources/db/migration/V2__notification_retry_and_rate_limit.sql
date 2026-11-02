-- V2 - Retry con backoff per le notifiche + indice per il rate limit

-- Tentativi di invio email: dopo contact.notification.max-attempts si smette (il messaggio resta nell'inbox admin)
ALTER TABLE contact_message
    ADD COLUMN notify_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN next_attempt_at TIMESTAMPTZ;           -- NULL = tentare subito

-- Rate limit: "quanti messaggi da questo IP (hash) nell'ultima ora?"
CREATE INDEX idx_contact_message_ip_recent ON contact_message (ip_hash, created_at);
