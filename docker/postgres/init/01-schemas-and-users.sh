#!/bin/sh
# Eseguito da Postgres SOLO al primo avvio (quando il volume dati è vuoto).
# Crea un utente e uno schema per ogni microservizio: ogni servizio può
# leggere e scrivere solo nel PROPRIO schema (isolamento tra microservizi).
set -eu

create_service_schema() {
  user="$1"; password="$2"; schema="$3"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-SQL
    CREATE ROLE ${user} LOGIN PASSWORD '${password}';
    CREATE SCHEMA ${schema} AUTHORIZATION ${user};
    GRANT CONNECT ON DATABASE ${POSTGRES_DB} TO ${user};
    -- l'utente vede di default solo il proprio schema
    ALTER ROLE ${user} SET search_path = ${schema};
SQL
}

# Nessuno (tranne il superuser) può creare oggetti nello schema "public"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  -c "REVOKE ALL ON SCHEMA public FROM PUBLIC;" \
  -c "REVOKE ALL ON DATABASE ${POSTGRES_DB} FROM PUBLIC;"

create_service_schema "$PROFILE_DB_USER"  "$PROFILE_DB_PASSWORD"  profile
create_service_schema "$PROJECTS_DB_USER" "$PROJECTS_DB_PASSWORD" projects
create_service_schema "$CONTACT_DB_USER"  "$CONTACT_DB_PASSWORD"  contact
