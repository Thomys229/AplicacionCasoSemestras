FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY gradle/wrapper gradle/wrapper
COPY gradlew .
COPY backend/build.gradle backend/settings.gradle backend/
COPY backend/reservas backend/reservas
RUN mkdir -p backend/catalogo backend/materiales && sh gradlew -p backend :reservas:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/backend/reservas/build/install/reservas/ ./
USER 10001
ENTRYPOINT ["/app/bin/reservas"]
