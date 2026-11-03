# sito-j

Il mio sito personale: chi sono, i progetti, il percorso e un form contatti.
È anche un progetto a sé: backend a microservizi in **Java 25 + Spring Boot 4**, pensato per girare **a costo zero**.

*My personal website, built as a Spring Boot microservices project. Bilingual (IT/EN), work in progress.*

## Stato

| Parte | Stato |
|---|---|
| `profile-service` — profilo, esperienze, formazione, skill (IT/EN) | ✅ fatto, con test |
| `projects-service` — progetti, tag, statistiche sincronizzate da GitHub/GitLab | ✅ fatto, con test |
| `contact-service` — form contatti (Turnstile, rate limit, IP in hash, notifica email, retention GDPR) | ✅ fatto, con test |
| Prototipo grafico della home (`design/prototype`) | ✅ fatto |
| `web-public` — sito pubblico in Thymeleaf | 🔜 prossimo |
| `web-admin` — pannello in Vaadin | ⏳ |
| `gateway`, CI/CD, deploy | ⏳ |

## Stack

- Java 25, Spring Boot 4.1, Maven multi-modulo
- PostgreSQL 18 (uno schema e un utente per servizio), Flyway
- Test di integrazione con JUnit 5 e Testcontainers
- Docker Compose; deploy previsto su Oracle Cloud Always Free con Caddy (HTTPS)

## Avvio in locale

Serve Docker e JDK 25.

```bash
cp .env.example .env          # poi cambia password e segreti
docker compose up -d          # PostgreSQL
./mvnw verify                 # build + test
./mvnw -pl profile-service spring-boot:run
```

Porte: profile `8081`, projects `8082`, contact `8083`.
Esempio: `curl "localhost:8082/api/v1/projects?lang=it&featured=true"`

## Struttura

```
profile-service/    API del profilo
projects-service/   API dei progetti + sync GitHub/GitLab
contact-service/    form contatti
design/prototype/   prototipo statico della home (HTML/CSS/GSAP)
docker/             init del database
docs/ARCHITETTURA.md  progetto completo: servizi, tabelle, API, flussi, sicurezza, hosting
```

## Autore

Jeanfranco Alvarado — [LinkedIn](https://www.linkedin.com/in/jeanfranco-alvarado-800a02344) · [GitHub](https://github.com/JesusAlvarado2005) · [GitLab](https://gitlab.com/JesusAlvarado2005)
