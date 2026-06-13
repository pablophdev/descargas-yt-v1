# 1. Fase de compilación (Apuntando a la carpeta download-service)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copiamos el contenido de la carpeta del backend al contenedor
COPY download-service/ .
RUN mvn clean package -DskipTests

# 2. Fase de ejecución
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && apt-get install -y \
    python3 \
    ffmpeg \
    curl \
    && curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp \
    && chmod a+rx /usr/local/bin/yt-dlp \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
# Copiar el .jar generado desde la fase de build
COPY --from=build /app/target/download-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]