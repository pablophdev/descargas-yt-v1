# 1. Fase de compilación con Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Al estar dentro de download-service, copiamos directamente el pom.xml y src
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Fase de ejecución en Linux
FROM eclipse-temurin:21-jre-jammy

# Instalar las dependencias del sistema de manera global: python3, ffmpeg y yt-dlp
RUN apt-get update && apt-get install -y \
    python3 \
    ffmpeg \
    curl \
    && curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp \
    && chmod a+rx /usr/local/bin/yt-dlp \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
# Copia el archivo .jar generado en la fase anterior
COPY --from=build /app/target/download-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]