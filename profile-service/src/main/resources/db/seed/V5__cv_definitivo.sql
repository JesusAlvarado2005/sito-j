-- Allinea il profilo al CV definitivo (novembre 2026).
-- (le descrizioni della formazione non hanno il punto finale: lo aggiunge il sito)

UPDATE profile_translation SET bio_md =
 'Sono Jeanfranco, Junior Backend Developer a Milano, con esperienza pratica in Java e Go maturata in due tirocini di sviluppo software. In Sysnet ho sviluppato un microservizio Go/Gin con API REST, autenticazione JWT, RBAC e PostgreSQL, in un team di 8 persone con merge request e code review. Nel progetto di diploma ITS ho curato in Spring Boot la persistenza time-series su InfluxDB 3 di dati di telemetria a 50 Hz. Diploma ITS (104/110) e laurea in Ingegneria Informatica (L-8) prevista nel 2027.'
WHERE profile_id = 1 AND lang = 'it';

UPDATE profile_translation SET bio_md =
 'I''m Jeanfranco, a Junior Backend Developer in Milan with hands-on experience in Java and Go from two software development internships. At Sysnet I built a Go/Gin microservice with REST APIs, JWT authentication, RBAC and PostgreSQL, in a team of 8 working with merge requests and code review. In my ITS diploma project I handled the time-series persistence of 50 Hz telemetry data on InfluxDB 3 with Spring Boot. ITS diploma (104/110); BSc in Computer Engineering (L-8) expected in 2027.'
WHERE profile_id = 1 AND lang = 'en';

UPDATE education_translation t SET description_md = v.descr
FROM education e, (VALUES
 ('Form&Job', 'it', 'Ottobre – novembre 2026: laboratori su Azure con VM e reti, Terraform e Bicep, Docker e ACR, CI/CD con Azure DevOps, AKS. Laboratori pubblicati su GitHub nel repository azure-devops-lab'),
 ('Form&Job', 'en', 'October – November 2026: hands-on labs on Azure with VMs and networking, Terraform and Bicep, Docker and ACR, CI/CD with Azure DevOps, AKS. Labs published on GitHub in the azure-devops-lab repository'),
 ('ITS Tech Talent Factory', 'it', 'Java e OOP avanzata, Spring Boot, JPA/Hibernate, API REST, database relazionali e NoSQL, CI/CD, Agile/Scrum'),
 ('ITS Tech Talent Factory', 'en', 'Advanced Java and OOP, Spring Boot, JPA/Hibernate, REST APIs, relational and NoSQL databases, CI/CD, Agile/Scrum')
) AS v(inst, lang, descr)
WHERE t.education_id = e.id AND e.institution = v.inst AND t.lang = v.lang;

-- Skill: MongoDB non è più nel CV; RBAC sì
DELETE FROM skill WHERE name = 'MongoDB';
INSERT INTO skill (category_id, name, sort_order)
SELECT id, 'RBAC', 12 FROM skill_category WHERE code = 'OTHER';
