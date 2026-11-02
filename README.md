# sito-j

Sito personale a microservizi: Java 25, Spring Boot 4, PostgreSQL, Docker.
Architettura completa: [docs/ARCHITETTURA.md](docs/ARCHITETTURA.md)

## Avvio in locale

```bash
cp .env.example .env          # solo la prima volta
docker compose up -d          # PostgreSQL (crea schemi e utenti al primo avvio)
./mvnw verify                 # compila e lancia i test (Testcontainers, serve Docker)
./mvnw -pl profile-service spring-boot:run
curl localhost:8081/actuator/health
```

| Modulo | Porta locale |
|---|---|
| profile-service | 8081 |
| projects-service | 8082 |
| contact-service | 8083 |
