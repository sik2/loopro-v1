# 배포

| | 어디 | 주소 |
| --- | --- | --- |
| `front-react` | Cloudflare Pages | <https://loopro-v1.pages.dev> |
| `back` | Railway | <https://back-production-42a6.up.railway.app> |
| DB | Railway Postgres | 사설 네트워크로만 접근 |

배포 판단의 근거는 [ADR-0004](adr/0004-배포-환경은-설정을-파일이-아니라-환경변수로-받는다.md)와 [ADR-0005](adr/0005-배포-이미지를-Dockerfile로-고정한다.md)에 있다.

## 운영에서 달라지는 것

`prod` 프로파일은 `dev`와 세 군데가 다르다.

- **DB가 Postgres다.** 개발은 H2 파일 DB지만, Railway의 컨테이너 파일시스템은 재배포마다 초기화되므로 파일 DB를 쓰면 글이 매번 사라진다.
- **Swagger UI와 h2-console이 닫혀 있다.** `/swagger-ui.html`, `/v3/api-docs`, `/h2-console` 전부 404다.
- **샘플 데이터는 `APP_INIT_DATA_PASSWORD`가 있을 때만 만들어진다.** 이 값이 없으면 만들지 않는다. 기본값을 두면 저장소에 적힌 비밀번호가 공개된 서비스에서 되살아난다.

## 환경변수

`back` 서비스에 설정한다. **기본값이 없다** — 빠지면 앱이 뜨지 않는다.

| 변수 | 값 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_JDBC_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `DATABASE_USERNAME` | `${{Postgres.PGUSER}}` |
| `DATABASE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `APP_JWT_SECRET` | 32바이트 이상의 임의 문자열 |
| `APP_CORS_ALLOWED_ORIGINS` | 배포된 front 주소. 쉼표로 여러 개 |
| `APP_INIT_DATA_PASSWORD` | 샘플 계정 비밀번호. 비우면 샘플 데이터 없음 |

`${{Postgres.*}}`는 Railway의 변수 참조다. DB 접속 정보를 복사해 두지 않고 Postgres 서비스에서 그때그때 읽어오므로, 비밀번호가 바뀌어도 따라간다.

front는 빌드 시점에 `VITE_API_BASE_URL`이 번들에 박힌다. `front-react/.env.production`에 있고, 비밀이 아니다 — 브라우저가 어차피 호출하는 공개 주소다.

## 다시 배포하기

back:

```bash
railway up --service back --detach -y
```

front (API 주소가 빌드에 박히므로 반드시 빌드부터):

```bash
cd front-react && npm run build && npx wrangler pages deploy dist --project-name loopro-v1 --branch main
```

## 처음부터 다시 세울 때

순서가 중요하다. back과 front가 서로의 주소를 알아야 하는데 배포 전에는 주소가 없다.

1. `railway init --name loopro-v1` → `railway add --database postgres` → `railway add --service back`
2. `back`에 환경변수를 넣는다. 이때 `APP_CORS_ALLOWED_ORIGINS`는 아직 모르므로 아무 값이나 둔다.
3. `railway up --service back` → `railway domain --service back`으로 back 주소를 얻는다.
4. 그 주소를 `front-react/.env.production`에 적고 빌드해서 Pages에 올린다.
5. Pages 주소를 `APP_CORS_ALLOWED_ORIGINS`에 넣는다. 값을 바꾸면 Railway가 알아서 재배포한다.

## 확인할 것

배포가 끝나면 이 네 가지가 맞아야 한다.

```bash
API=https://back-production-42a6.up.railway.app
WEB=https://loopro-v1.pages.dev

# 1. API가 응답한다
curl -s -o /dev/null -w '%{http_code}\n' $API/api/posts                    # 200

# 2. 운영에서 문서·콘솔이 닫혀 있다
curl -s -o /dev/null -w '%{http_code}\n' $API/swagger-ui.html              # 404
curl -s -o /dev/null -w '%{http_code}\n' $API/h2-console                   # 404

# 3. CORS가 배포된 front만 허용한다
curl -s -o /dev/null -w '%{http_code}\n' -X OPTIONS $API/api/posts \
  -H "Origin: $WEB" -H 'Access-Control-Request-Method: GET'                # 200
curl -s -o /dev/null -w '%{http_code}\n' -X OPTIONS $API/api/posts \
  -H 'Origin: https://evil.example' -H 'Access-Control-Request-Method: GET' # 403

# 4. 클라이언트 라우팅이 새로고침을 견딘다
curl -s -o /dev/null -w '%{http_code}\n' $WEB/p/1                          # 200
```

4번은 `front-react/public/_redirects`가 하는 일이다. 이 파일이 없으면 `/p/1`을 직접 열거나 새로고침할 때 Pages가 그 경로의 파일을 찾다 404를 낸다.

## 알고 있는 제약

- **Railway는 사용량 과금이다.** 서비스 1개 + Postgres 1개가 크레딧을 쓴다.
- **`ddl-auto: update`로 스키마를 맞춘다.** 컬럼을 지우거나 타입을 바꾸는 변경은 반영되지 않는다. 스키마가 흔들리기 시작하면 마이그레이션 도구가 필요하고, 그때 이 문서를 다시 연다.
- **샘플 데이터가 운영 DB에 들어 있다.** 첫 배포에서 만들어졌다. 실제로 쓰기 시작하면 지워야 한다.
- **`Post` 본문은 `@Lob`이 아니라 `LONGVARCHAR`로 매핑한다.** `@Lob`은 PostgreSQL에서 oid(large object) 컬럼이 되어 트랜잭션 밖 읽기가 깨진다. 이 매핑이면 PostgreSQL은 `text`, H2는 `VARCHAR`가 되어 두 DB에서 같은 뜻이다.
