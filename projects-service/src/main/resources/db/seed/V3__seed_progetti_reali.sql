-- I miei progetti reali. Gli id non sono scritti a mano: l'identity li genera (niente conflitti con il pannello admin).

INSERT INTO project (slug, status, featured, start_date, end_date, repo_provider, repo_path, sort_order) VALUES
('backend-telemetry',      'PUBLISHED', TRUE,  '2026-07-01', '2026-09-30', 'GITLAB', 'fr.mantegazza/backend-telemetry', 1),
('db-manager',             'PUBLISHED', FALSE, '2026-04-01', '2026-07-31', 'NONE',   NULL, 2),
('sito-j',                 'PUBLISHED', FALSE, NULL,         NULL,         'GITHUB', 'JesusAlvarado2005/sito-j', 3),
('language-school-webapp', 'PUBLISHED', FALSE, '2025-07-01', '2025-10-31', 'NONE',   NULL, 4);

INSERT INTO project_translation (project_id, lang, title, summary, context)
SELECT p.id, v.lang, v.title, v.summary, v.context FROM project p JOIN (VALUES
 ('backend-telemetry', 'it', 'BackendTelemetry',
  'Telemetria live di Le Mans Ultimate: un bridge C++ manda circa 66 valori per campione, 50 volte al secondo, via WebSocket. Io ho curato la persistenza su InfluxDB 3: scritture a blocchi e una coda che non blocca mai la dashboard Vaadin.',
  'Tesina ITS · team di 2 · 2026'),
 ('backend-telemetry', 'en', 'BackendTelemetry',
  'Live telemetry for Le Mans Ultimate: a C++ bridge sends about 66 values per sample, 50 times a second, over WebSocket. I built the InfluxDB 3 persistence: batched writes and a queue that never blocks the Vaadin dashboard.',
  'ITS thesis project · team of 2 · 2026'),
 ('db-manager', 'it', 'DB-Manager',
  'Microservizio in Go per gestire i pali ferroviari, 40 solo nell''area di Milano. Permessi per ruolo, login JWT, dati dei sensori salvati su PostgreSQL e API documentate con Swagger.',
  'Tirocinio Sysnet · team di 8 · 2026'),
 ('db-manager', 'en', 'DB-Manager',
  'A Go microservice to manage railway poles, 40 in the Milan area alone. Role-based permissions, JWT login, sensor data stored in PostgreSQL and APIs documented with Swagger.',
  'Sysnet internship · team of 8 · 2026'),
 ('sito-j', 'it', 'sito-j',
  'Il sito che stai guardando. Microservizi Spring Boot, PostgreSQL e Docker, in italiano e inglese, pensato per girare a costo zero. Il codice è pubblico.',
  'Progetto personale · 2026'),
 ('sito-j', 'en', 'sito-j',
  'The site you are looking at. Spring Boot microservices, PostgreSQL and Docker, in Italian and English, designed to run at zero cost. The code is public.',
  'Personal project · 2026'),
 ('language-school-webapp', 'it', 'Web app scuola di lingue',
  'Commissionata da un docente, l''ho sviluppata da solo, dal database all''interfaccia. Tre ruoli (admin, docente, studente), password protette con bcrypt, e ogni docente gestisce i propri corsi ed esercizi.',
  'Tirocinio Galilux · da solo · 2025'),
 ('language-school-webapp', 'en', 'Language school web app',
  'Commissioned by a teacher and built on my own, from database to interface. Three roles (admin, teacher, student), passwords protected with bcrypt, and each teacher manages their own courses and exercises.',
  'Galilux internship · solo project · 2025')
) AS v(slug, lang, title, summary, context) ON v.slug = p.slug;

INSERT INTO tag (name, slug) VALUES
('Java 25', 'java-25'), ('Spring Boot 4.1', 'spring-boot-4-1'), ('WebSocket', 'websocket'), ('InfluxDB 3', 'influxdb-3'),
('Vaadin', 'vaadin'), ('Docker Compose', 'docker-compose'), ('Go', 'go'), ('Gin', 'gin'), ('PostgreSQL', 'postgresql'),
('GORM', 'gorm'), ('Swagger', 'swagger'), ('Spring Boot 4', 'spring-boot-4'), ('Docker', 'docker'),
('Java', 'java'), ('Servlet', 'servlet'), ('JSP', 'jsp'), ('MySQL', 'mysql'), ('Tomcat', 'tomcat');

INSERT INTO project_tag (project_id, tag_id)
SELECT p.id, t.id FROM project p JOIN (VALUES
 ('backend-telemetry', 'java-25'), ('backend-telemetry', 'spring-boot-4-1'), ('backend-telemetry', 'websocket'),
 ('backend-telemetry', 'influxdb-3'), ('backend-telemetry', 'vaadin'), ('backend-telemetry', 'docker-compose'),
 ('db-manager', 'go'), ('db-manager', 'gin'), ('db-manager', 'postgresql'), ('db-manager', 'gorm'),
 ('db-manager', 'swagger'), ('db-manager', 'docker-compose'),
 ('sito-j', 'java-25'), ('sito-j', 'spring-boot-4'), ('sito-j', 'postgresql'), ('sito-j', 'docker'),
 ('language-school-webapp', 'java'), ('language-school-webapp', 'servlet'), ('language-school-webapp', 'jsp'),
 ('language-school-webapp', 'mysql'), ('language-school-webapp', 'tomcat')
) AS v(slug, tag) ON v.slug = p.slug JOIN tag t ON t.slug = v.tag;
