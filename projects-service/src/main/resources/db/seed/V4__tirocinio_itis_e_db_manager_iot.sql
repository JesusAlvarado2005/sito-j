-- Allinea i progetti a CV e LinkedIn (novembre 2026):
-- 1) il tirocinio 2025 era all'ITIS Galileo Galilei, non "Galilux";
-- 2) DB-Manager: si parla solo di dati da sensori IoT, senza il dominio del cliente.

UPDATE project_translation t SET context = v.context
FROM project p, (VALUES
 ('it', 'Tirocinio ITIS Galileo Galilei · da solo · 2025'),
 ('en', 'ITIS Galileo Galilei internship · solo project · 2025')
) AS v(lang, context)
WHERE t.project_id = p.id AND p.slug = 'language-school-webapp' AND t.lang = v.lang;

UPDATE project_translation t SET summary = v.summary
FROM project p, (VALUES
 ('it', 'Microservizio in Go per i dati di sensori IoT ricevuti via MQTT. Permessi per ruolo, login JWT, dati salvati su PostgreSQL e API REST documentate con Swagger.'),
 ('en', 'A Go microservice for IoT sensor data received over MQTT. Role-based permissions, JWT login, data stored in PostgreSQL and REST APIs documented with Swagger.')
) AS v(lang, summary)
WHERE t.project_id = p.id AND p.slug = 'db-manager' AND t.lang = v.lang;
