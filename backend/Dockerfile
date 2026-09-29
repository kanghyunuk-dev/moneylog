# 1단계: 빌드
FROM gradle:9.5.1-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar -x test --no-daemon

# 2단계: 실행
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
