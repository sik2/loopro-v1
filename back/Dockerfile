# Railway의 기본 빌더에 맡기지 않고 직접 고정한다.
#  - 구 빌더(Nixpacks)는 Gradle 9를 거부한다. 이 프로젝트는 9.5.1이다.
#  - 현 빌더(Railpack)는 JDK 21이 기본이고, wrapper 버전을 major로 잘라
#    9.x 마일스톤 프리뷰를 끌어오는 미해결 버그가 있다(railpack#554).
# 여기서 못 박으면 로컬·CI·배포가 같은 JDK로 같은 빌드를 한다.
#
# 빌드 컨텍스트는 저장소 루트다. front-react도 같은 저장소에 있으므로
# 경로에 back/을 붙인다. .dockerignore가 나머지를 걸러낸다.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# wrapper와 빌드 스크립트를 먼저 복사해 의존성 레이어를 캐시한다.
# src만 바뀐 재배포에서는 의존성을 다시 받지 않는다.
COPY back/gradlew back/settings.gradle.kts back/build.gradle.kts ./
COPY back/gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies --quiet || true

COPY back/src src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# root로 돌리지 않는다.
RUN useradd --system --create-home --uid 10001 loopro
USER loopro

COPY --from=build /workspace/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=prod

# PORT는 Railway가 주입한다. application-prod.yml이 그 값을 읽는다.
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
