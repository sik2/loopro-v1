# Loopro

블로그 기능을 기반으로 **콘텐츠**를 작성·공유하는 플랫폼.

| | 주소 |
| --- | --- |
| 웹 | <https://www.loopro.kr> |
| API | <https://api.loopro.kr> |

배포 방법과 환경변수는 [docs/deploy.md](docs/deploy.md)에 있다.

도메인 용어는 [CONTEXT.md](CONTEXT.md)에, 되돌리기 어려운 결정은 [docs/adr/](docs/adr/)에 있다.

## 구성

| 디렉터리                     | 내용                                    |
| ---------------------------- | --------------------------------------- |
| [`back`](back)               | Spring Boot REST API (Java 25, Gradle)  |
| [`front-react`](front-react) | Vite + React + TypeScript 웹            |
| [`infra`](infra)             | 비어 있음                               |
| [`front-kmp`](front-kmp)     | 비어 있음                               |

`back`은 REST API만 낸다. 화면은 전부 `front-react`가 그린다.

## 로컬 실행

### back

```bash
cd back && ./gradlew bootRun
```

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- h2-console: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:./db_dev`, 사용자 `sa`, 비밀번호 없음)

개발 프로파일은 `back/db_dev.mv.db` 파일에 데이터를 남긴다. 껐다 켜도 데이터가 유지된다.

빈 DB로 처음 띄우면 샘플 데이터(`Member` 5, `Post` 5, `Comment` 5)가 들어간다.
이미 `Member`가 있으면 건너뛰므로 재시작해도 불어나지 않는다.
처음부터 다시 보려면 `back/db_dev.mv.db`를 지우고 띄운다.

| Username | Nickname | Role    |
| -------- | -------- | ------- |
| `admin`  | 관리자   | `ADMIN` |
| `gureum` | 구름     | `USER`  |
| `baram`  | 바람     | `USER`  |
| `namu`   | 나무     | `USER`  |
| `byeol`  | 별       | `USER`  |

**로컬 개발 환경에서만** 비밀번호가 `password123`이다. 배포 환경은 `APP_INIT_DATA_PASSWORD`
환경변수로 받으며, 그 값이 없으면 샘플 데이터를 아예 만들지 않는다.

`gureum`에게는 비발행 글이 하나 있어서, 로그인 여부에 따라 목록이 달라지는 것을 바로 확인할 수 있다.

### front-react

```bash
cd front-react && npm install && npm run dev
```

- 웹: <http://localhost:5173>
- API 주소는 `VITE_API_BASE_URL` 환경변수로 주입한다. `front-react/.env.example` 참고.

## 검사

```bash
cd back && ./gradlew test
```

```bash
cd front-react && npm test && npx tsc -b && npm run lint
```

front 테스트는 화면을 세우고 사용자처럼 만진다. back을 띄우지 않아도 돌아간다 —
네트워크는 MSW가 가로챈다. 왜 그렇게 했는지는
[ADR-0006](docs/adr/0006-front의-seam은-화면-하나다.md)에 있다.
