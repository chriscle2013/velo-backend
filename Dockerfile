FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/Instahyre-0.0.1-SNAPSHOT.jar app.jar

# Crea un script de entrada en el momento de la construcción
RUN echo '#!/bin/bash\n\
JDBC_URL="jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}?sslmode=require"\n\
java -jar app.jar' > entrypoint.sh && chmod +x entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
