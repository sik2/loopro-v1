# 배포 이미지를 Dockerfile로 고정한다

Railway의 자동 빌더에 맡기지 않고 `back/Dockerfile`로 JDK와 빌드 단계를 직접 못 박는다. 이유는 세 가지이고 전부 확인한 사실이다. 첫째, 구 빌더 Nixpacks는 **Gradle 9를 거부한다**("Unsupported Gradle version: 9") — 이 프로젝트는 9.5.1이다. 둘째, 현 빌더 Railpack의 JDK 기본값은 21이고 25는 `RAILPACK_JDK_VERSION`으로 따로 지정해야 한다. 셋째, Railpack에는 `gradle-wrapper.properties`의 버전을 major로 잘라(`9.5.1` → `9`) mise에 묻는 미해결 버그가 있어(railpack#554) 9.x 마일스톤 프리뷰를 끌어와 빌더 이미지에 캐시한다. Dockerfile 하나면 이 셋이 전부 사라지고, 로컬·CI·배포가 같은 JDK로 같은 빌드를 한다.

## Considered Options

- **Railpack + `RAILPACK_JDK_VERSION=25` + `mise.toml`로 Gradle 고정.** 동작은 하지만 움직이는 부품이 셋이고, 그중 둘이 Railway 쪽 구현에 묶여 있다. 빌더가 바뀌면 다시 깨진다.
- **JDK 21로 낮추기.** 기본 빌더가 확실히 지원하지만 로컬과 버전이 갈린다. 스펙이 JDK 25를 명시했다.

## Consequences

- 빌드 컨텍스트는 저장소 루트다. `front-react`도 같은 저장소에 있으므로 Dockerfile의 경로에 `back/`이 붙고, `.dockerignore`가 나머지를 걸러낸다.
- 이미지 안에서는 테스트를 돌리지 않는다(`bootJar -x test`). 테스트는 로컬과 CI의 몫이다.
- 런타임은 JRE 이미지에서 비특권 사용자로 돈다.
- JDK를 올릴 때 `build.gradle.kts`의 툴체인과 Dockerfile의 두 `FROM`을 함께 고쳐야 한다. 한쪽만 고치면 빌드는 되는데 실행이 깨진다.
