# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build
ARG WORKDIR=/opt/app

WORKDIR ${WORKDIR}
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# Stage 2: Extract layered jar
FROM eclipse-temurin:17-jre-alpine AS extract
ARG WORKDIR=/opt/app

WORKDIR ${WORKDIR}
COPY --from=build ${WORKDIR}/target/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Stage 3: Final image
FROM eclipse-temurin:17-jre-alpine
ARG WORKDIR=/opt/app
WORKDIR ${WORKDIR}

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=extract ${WORKDIR}/dependencies/ ./
COPY --from=extract ${WORKDIR}/spring-boot-loader/ ./
COPY --from=extract ${WORKDIR}/snapshot-dependencies/ ./
COPY --from=extract ${WORKDIR}/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
