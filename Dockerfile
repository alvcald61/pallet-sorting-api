# Dockerfile para el backend Spring Boot
# Debe estar en la raíz del repo pallet-sorting-api
# Railway lo detecta automáticamente al hacer deploy

# ───── Etapa 1: Build ─────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Instalar la librería local 2D-Bin-Packing desde JAR precompilado.
# El JAR debe estar commiteado en lib/2d-bin-packing-1.0.0.jar
COPY lib/ ./lib/
RUN mvn install:install-file \
      -Dfile=lib/2d-bin-packing-1.0.0.jar \
      -DgroupId=org.packing \
      -DartifactId=2d-bin-packing \
      -Dversion=1.0.0 \
      -Dpackaging=jar \
      --batch-mode -q

# Copiar pom.xml para cachear el resto de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline --batch-mode -q

# Copiar el código fuente y compilar
COPY src/ ./src/
RUN mvn package -DskipTests --batch-mode -q

# ───── Etapa 2: Runtime (imagen mínima) ───────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S tupack && adduser -S tupack -G tupack
USER tupack

# Copiar el JAR desde la etapa de build
COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
