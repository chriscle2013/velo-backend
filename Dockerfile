# Usa una imagen de Maven para construir la aplicación
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app

# Copia los archivos de configuración de Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente y lo compila
COPY src ./src
RUN mvn clean package -DskipTests

# Usa una imagen más pequeña para ejecutar la aplicación
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/Instahyre-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
