# sito-j

**Sito online → https://jesusalvarado2005.github.io/sito-j/**

Il mio sito personale (IT/EN) e, insieme, un progetto backend completo: microservizi in **Java 25 + Spring Boot 4**,
PostgreSQL, Docker e Caddy, con CI su GitHub Actions.

*My personal website (IT/EN) and a complete backend project: Java 25 + Spring Boot 4 microservices, PostgreSQL, Docker, Caddy, GitHub Actions CI.*

> Online c'è la **copia statica** del sito, generata dal sito vero ([scripts/export-static.sh](scripts/export-static.sh)) e ospitata gratis su GitHub Pages.
> Lo stack completo, con database e form contatti, gira in Docker con un solo comando ed è pronto per il deploy su una VM ([docs/DEPLOY.md](docs/DEPLOY.md)).

## Architettura

```
Internet ──▶ Caddy ──▶ web-public ──▶ profile-service  ─┐
            (HTTPS,    (Spring MVC +   projects-service ─┼─▶ PostgreSQL
             CSP)       Thymeleaf)     contact-service  ─┘   (uno schema e un utente per servizio)
                                            │
                                            ├─▶ GitHub / GitLab API (statistiche dei repository)
                                            └─▶ Cloudflare Turnstile, SMTP (form contatti)
```

| Modulo | Cosa fa |
|---|---|
| `profile-service` | Profilo, esperienze, formazione, skill. API REST con traduzioni IT/EN e fallback |
| `projects-service` | Progetti e tag; sincronizza le statistiche dei repository da GitHub e GitLab ogni 6 ore |
| `contact-service` | Messaggi del form: anti-bot Turnstile, rate limit, IP salvati solo come HMAC, email con outbox e retry, cancellazione dopo 12 mesi (GDPR) |
| `web-public` | Il sito: pagine server-side, SEO (hreflang, sitemap, JSON-LD), cache con fallback se un servizio è giù, form con rate limit e honeypot |

**Scelte principali**
- Schema del DB solo tramite **Flyway**, Hibernate in `validate`; DTO record, entity mai fuori dal service; errori come **ProblemDetail**.
- **Sicurezza:** solo Caddy è esposto; `/api` e `/actuator` bloccati; CSP senza script inline; IP reale ricavato solo da proxy fidati;
  configurazione sensibile validata all'avvio; timeout su ogni chiamata esterna; container non root.
- **Test:** 75 test (JUnit 5, MockMvc, Testcontainers con PostgreSQL vero).

Il progetto completo è descritto in [docs/ARCHITETTURA.md](docs/ARCHITETTURA.md).

## Stack

Java 25 · Spring Boot 4.1 · Spring MVC · Thymeleaf · Spring Data JPA · Flyway · PostgreSQL 18 ·
JUnit 5 · Testcontainers · Maven · Docker Compose · Caddy · GitHub Actions · GHCR

## Avvio in locale

Servono Docker e JDK 25.

```bash
cp .env.example .env                                 # poi metti password e segreti tuoi
./mvnw verify                                        # build + test
./mvnw -DskipTests package && docker compose up -d --build
# → http://localhost:8088
```

## Struttura

```
profile-service/   projects-service/   contact-service/   web-public/
docker/            Caddyfile e init del database
scripts/           export della copia statica e pubblicazione su GitHub Pages
docs/              ARCHITETTURA.md, DEPLOY.md
.github/workflows/ CI: build, test e immagini Docker su GHCR
```

## Autore

**Jeanfranco Alvarado**, Junior Backend Developer · Milano
[LinkedIn](https://www.linkedin.com/in/jeanfranco-alvarado-800a02344) · [GitHub](https://github.com/JesusAlvarado2005) · [GitLab](https://gitlab.com/JesusAlvarado2005)
