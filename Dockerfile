FROM openjdk:23-jdk AS builder

ARG APP_DIR=/app
WORKDIR ${APP_DIR}

COPY mvnw .                   
COPY mvnw.cmd .               
COPY pom.xml .                
COPY .mvn .mvn                
COPY src src                  

RUN chmod a+x ./mvnw && ./mvnw clean package -Dmaven.test.skip=true

FROM openjdk:23-jdk

ARG DEPLOY_DIR=/app
WORKDIR ${DEPLOY_DIR}

COPY --from=builder /app/target/mini-ssf-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8080         

EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=60s --start-period=120s --retries=3 \
   CMD curl -s -f http://localhost:${SERVER_PORT}/status || exit 1

ENTRYPOINT [ "java", "-jar", "app.jar" ]FROM openjdk:23-jdk AS builder

ARG APP_DIR=/app
WORKDIR ${APP_DIR}

COPY mvnw .                   
COPY mvnw.cmd .               
COPY pom.xml .                
COPY .mvn .mvn                
COPY src src                  

RUN chmod a+x ./mvnw && ./mvnw clean package -Dmaven.test.skip=true

FROM openjdk:23-jdk

ARG DEPLOY_DIR=/app
WORKDIR ${DEPLOY_DIR}

COPY --from=builder /app/target/mini-ssf-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8080         

EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=60s --start-period=120s --retries=3 \
   CMD curl -s -f http://localhost:${SERVER_PORT}/status || exit 1

ENTRYPOINT [ "java", "-jar", "app.jar" ]