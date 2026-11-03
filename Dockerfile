# Immagine unica per tutti i servizi Spring Boot: cambia solo il jar (ARG MODULE).
# Il jar si compila PRIMA con Maven (./mvnw package, in locale o in CI): un jar Java gira su qualsiasi CPU,
# quindi la stessa build va bene sia per il PC (amd64) sia per la VM Oracle (arm64).
FROM eclipse-temurin:25-jre-alpine

ARG MODULE
LABEL org.opencontainers.image.source="https://github.com/JesusAlvarado2005/sito-j"

WORKDIR /app
COPY ${MODULE}/target/${MODULE}-*.jar app.jar

# Mai come root: se il processo venisse compromesso, nel container non avrebbe privilegi
USER 10001:10001

# La JVM usa al massimo il 75% della memoria del container; in caso di OutOfMemory esce e Docker la riavvia
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
