-- Dati reali del profilo (fonte: il mio CV). Solo contenuti pubblici: niente telefono.
-- Le modifiche future si fanno dal pannello admin, non qui.

INSERT INTO profile (id, full_name, public_email, location)
VALUES (1, 'Jeanfranco Alvarado', 'jean.alv0705@gmail.com', 'Milano');

INSERT INTO profile_translation (profile_id, lang, headline, bio_md) VALUES
(1, 'it', 'Junior Backend Developer · Milano',
 'Sono Jeanfranco, sviluppatore backend junior a Milano: Java e Spring Boot, e Go. Mi sono formato con l''ITS da Backend Developer, due tirocini e un corso da Cloud Engineer su Azure, e sono all''ultimo anno di Ingegneria Informatica. So lavorare in team (in Sysnet eravamo in 8, con code review) e anche da solo: la web app di Galilux l''ho consegnata in autonomia.'),
(1, 'en', 'Junior Backend Developer · Milan',
 'I''m Jeanfranco, a junior backend developer in Milan: Java and Spring Boot, and Go. I trained through an ITS Backend Developer programme, two internships and a Cloud Engineer course on Azure, and I''m in the final year of a Computer Engineering degree. I work well in a team (at Sysnet we were 8, with code review) and on my own: I delivered the Galilux web app independently.');

INSERT INTO social_link (type, url, label, sort_order) VALUES
('GITHUB',   'https://github.com/JesusAlvarado2005', 'GitHub', 1),
('GITLAB',   'https://gitlab.com/JesusAlvarado2005', 'GitLab', 2),
('LINKEDIN', 'https://www.linkedin.com/in/jeanfranco-alvarado-800a02344', 'LinkedIn', 3);

-- ---------- Esperienze (le date mostrate sono mese + anno) ----------
INSERT INTO experience (company, start_date, end_date, sort_order) VALUES
('Sysnet',   '2026-04-01', '2026-07-31', 1),
('Galilux',  '2025-07-01', '2025-10-31', 2);

INSERT INTO experience_translation (experience_id, lang, role, description_md)
SELECT id, 'it', 'Tirocinante Sviluppatore Software',
 'Il microservizio DB-Manager in Go, poi il deploy: Docker Compose in container LXC su Proxmox VE, con Gitea e Grafana configurati da zero.'
FROM experience WHERE company = 'Sysnet';
INSERT INTO experience_translation (experience_id, lang, role, description_md)
SELECT id, 'en', 'Software Developer Intern',
 'The DB-Manager microservice in Go, then the deployment: Docker Compose in LXC containers on Proxmox VE, with Gitea and Grafana set up from scratch.'
FROM experience WHERE company = 'Sysnet';
INSERT INTO experience_translation (experience_id, lang, role, description_md)
SELECT id, 'it', 'Tirocinante Sviluppatore Java', 'Web app in Servlet/JSP per una scuola di lingue, sviluppata da solo.'
FROM experience WHERE company = 'Galilux';
INSERT INTO experience_translation (experience_id, lang, role, description_md)
SELECT id, 'en', 'Java Developer Intern', 'A Servlet/JSP web app for a language school, built on my own.'
FROM experience WHERE company = 'Galilux';

-- ---------- Formazione (le date mostrate sono solo gli anni) ----------
INSERT INTO education (institution, start_date, end_date, sort_order) VALUES
('Form&Job',                       '2026-10-12', '2026-11-06', 1),
('University of Helsinki (MOOC.fi)', '2026-01-01', '2026-01-01', 2),
('Universitas Mercatorum',         '2025-01-01', NULL,         3),
('ITS Tech Talent Factory',        '2024-10-01', '2026-09-30', 4),
('ITIS Galileo Galilei',           '2019-09-01', '2024-07-01', 5);

INSERT INTO education_translation (education_id, lang, degree, description_md)
SELECT id, v.lang, v.degree, v.descr FROM education e JOIN (VALUES
 ('Form&Job', 'it', 'Programmatore Junior Cloud Engineer (160 ore)',
  'Ottobre – novembre 2026: laboratori su Azure con VM e reti, Terraform e Bicep, Docker, pipeline con Azure DevOps e GitHub Actions, Kubernetes su AKS. I laboratori sono su GitHub'),
 ('Form&Job', 'en', 'Junior Cloud Engineer Programmer (160 hours)',
  'October – November 2026: hands-on labs on Azure with VMs and networking, Terraform and Bicep, Docker, pipelines with Azure DevOps and GitHub Actions, Kubernetes on AKS. The labs are on GitHub'),
 ('University of Helsinki (MOOC.fi)', 'it', 'Certificato Java Programming I & II', NULL),
 ('University of Helsinki (MOOC.fi)', 'en', 'Java Programming I & II certificate', NULL),
 ('Universitas Mercatorum', 'it', 'Laurea Triennale in Ingegneria Informatica (L-8)', 'Ultimo anno, laurea prevista nel 2027'),
 ('Universitas Mercatorum', 'en', 'BSc in Computer Engineering (L-8)', 'Final year, graduation expected in 2027'),
 ('ITS Tech Talent Factory', 'it', 'Diploma Backend Developer (EQF 5), 104/110', 'Due tirocini e la tesina BackendTelemetry'),
 ('ITS Tech Talent Factory', 'en', 'Backend Developer diploma (EQF 5), 104/110', 'Two internships and the BackendTelemetry thesis project'),
 ('ITIS Galileo Galilei', 'it', 'Diploma di Perito Meccanico', 'Disegno tecnico, automazione e programmazione CNC'),
 ('ITIS Galileo Galilei', 'en', 'Mechanical Technician diploma', 'Technical drawing, automation and CNC programming')
) AS v(inst, lang, degree, descr) ON v.inst = e.institution;

-- ---------- Skill ----------
INSERT INTO skill_category (code, sort_order) VALUES ('BACKEND', 1), ('CLOUD', 2), ('OTHER', 3);
INSERT INTO skill_category_translation (category_id, lang, name)
SELECT id, v.lang, v.name FROM skill_category c JOIN (VALUES
 ('BACKEND', 'it', 'Backend e database'), ('BACKEND', 'en', 'Backend and databases'),
 ('CLOUD',   'it', 'Cloud e DevOps'),     ('CLOUD',   'en', 'Cloud and DevOps'),
 ('OTHER',   'it', 'E poi'),              ('OTHER',   'en', 'And also')
) AS v(code, lang, name) ON v.code = c.code;

INSERT INTO skill (category_id, name, sort_order)
SELECT c.id, v.name, v.ord FROM skill_category c JOIN (VALUES
 ('BACKEND', 'Java', 1), ('BACKEND', 'Go', 2), ('BACKEND', 'Spring Boot', 3), ('BACKEND', 'Hibernate', 4),
 ('BACKEND', 'Gin', 5), ('BACKEND', 'PostgreSQL', 6), ('BACKEND', 'MySQL', 7), ('BACKEND', 'MongoDB', 8),
 ('BACKEND', 'InfluxDB', 9), ('BACKEND', 'Vaadin', 10), ('BACKEND', 'JUnit 5', 11), ('BACKEND', 'Maven', 12),
 ('BACKEND', 'Swagger', 13), ('BACKEND', 'JWT', 14), ('BACKEND', 'Python', 15),
 ('CLOUD', 'Azure', 1), ('CLOUD', 'Azure DevOps', 2), ('CLOUD', 'Terraform', 3), ('CLOUD', 'Docker', 4),
 ('CLOUD', 'Kubernetes', 5), ('CLOUD', 'GitHub Actions', 6), ('CLOUD', 'Linux', 7), ('CLOUD', 'Proxmox VE', 8),
 ('CLOUD', 'LXC', 9), ('CLOUD', 'Grafana', 10), ('CLOUD', 'Gitea', 11), ('CLOUD', 'MQTT', 12),
 ('CLOUD', 'Git', 13), ('CLOUD', 'Claude Code', 14),
 ('OTHER', 'API REST', 1), ('OTHER', 'Microservizi', 2), ('OTHER', 'WebSocket', 3), ('OTHER', 'SQL', 4),
 ('OTHER', 'CI/CD', 5), ('OTHER', 'Bicep', 6), ('OTHER', 'AKS', 7), ('OTHER', 'Clean Architecture', 8),
 ('OTHER', 'Lombok', 9), ('OTHER', 'Jackson', 10)
) AS v(code, name, ord) ON v.code = c.code;
