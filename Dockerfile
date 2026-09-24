FROM eclipse-temurin:21-jdk-noble AS build

WORKDIR /workspace

COPY . .

RUN chmod +x mvnw && ./mvnw -ntp -Pprod -DskipTests package

FROM eclipse-temurin:21-jre-noble

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
