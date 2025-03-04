FROM eclipse-temurin:23-jre-alpine AS builder
WORKDIR /extracted
COPY ./build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract
FROM eclipse-temurin:23-jre-alpine
RUN apk update && apk upgrade && apk add curl jq && apk cache clean && rm -rf /var/cache/apk/*
WORKDIR /application
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
