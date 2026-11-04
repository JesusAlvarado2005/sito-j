-- Allinea il profilo al CV di novembre 2026. Le migrazioni precedenti non si modificano: si aggiorna qui.
-- (le descrizioni della formazione non hanno il punto finale: lo aggiunge il sito)

UPDATE profile_translation SET bio_md =
 'Sono Jeanfranco, sviluppatore backend junior a Milano: realizzo API REST e microservizi in Java/Spring Boot e Go, dalla modellazione del database al deploy in container. Mi sono formato con l''ITS, due tirocini e un corso da Cloud Engineer su Microsoft Azure, e sto concludendo la laurea in Ingegneria Informatica. Lavoro bene sia in team (in Sysnet eravamo in 8, con merge request e code review) sia in autonomia.'
WHERE profile_id = 1 AND lang = 'it';

UPDATE profile_translation SET bio_md =
 'I''m Jeanfranco, a junior backend developer in Milan: I build REST APIs and microservices in Java/Spring Boot and Go, from database modelling to container deployment. I trained through an ITS programme, two internships and a Cloud Engineer course on Microsoft Azure, and I''m finishing a degree in Computer Engineering. I work well both in a team (at Sysnet we were 8, with merge requests and code review) and on my own.'
WHERE profile_id = 1 AND lang = 'en';

UPDATE education_translation t SET description_md = v.descr
FROM education e, (VALUES
 ('Form&Job', 'it', 'Ottobre – novembre 2026: laboratori su Azure con VM e reti, Azure CLI, Terraform e Bicep, Docker e ACR, CI/CD con Azure DevOps, Kubernetes su AKS'),
 ('Form&Job', 'en', 'October – November 2026: hands-on labs on Azure with VMs and networking, Azure CLI, Terraform and Bicep, Docker and ACR, CI/CD with Azure DevOps, Kubernetes on AKS'),
 ('ITS Tech Talent Factory', 'it', 'Java e OOP avanzata, Spring Boot, JPA/Hibernate, API REST, database SQL e NoSQL, CI/CD, Agile/Scrum'),
 ('ITS Tech Talent Factory', 'en', 'Advanced Java and OOP, Spring Boot, JPA/Hibernate, REST APIs, SQL and NoSQL databases, CI/CD, Agile/Scrum'),
 ('ITIS Galileo Galilei', 'it', 'Meccanica, Meccatronica ed Energia: disegno tecnico manuale e CAD (AutoCAD, SolidWorks), organi meccanici, tornitura'),
 ('ITIS Galileo Galilei', 'en', 'Mechanics, Mechatronics and Energy: manual and CAD technical drawing (AutoCAD, SolidWorks), machine elements, turning')
) AS v(inst, lang, descr)
WHERE t.education_id = e.id AND e.institution = v.inst AND t.lang = v.lang;

INSERT INTO skill (category_id, name, sort_order)
SELECT id, 'Agile/Scrum', 11 FROM skill_category WHERE code = 'OTHER';
