# 01: back 부트스트랩

**What to build:** 개발자가 `back`을 실행하면 애플리케이션이 뜬다. 브라우저에서 Swagger UI와 h2-console을 열 수 있고, 개발 모드에서는 데이터가 파일로 남는다. 아직 도메인 기능은 하나도 없다. 이 티켓의 목적은 이후 모든 티켓이 올라설 바닥을 놓는 것이다.

**Blocked by:** None (can start immediately)

**Status:** done

- [x] Spring Boot 4.1.1, JDK 25, Gradle Kotlin DSL로 프로젝트가 구성된다. 언어는 Java
- [x] 루트 패키지는 `com.back`이고, 메인 클래스에서 JPA auditing이 켜져 있다
- [x] 모든 엔티티가 상속할 공통 상위 클래스가 있다: 식별자 + 작성일 + 수정일. 작성일·수정일은 auditing이 채운다
- [x] 공통 상위 클래스의 `equals`/`hashCode`는 식별자 기준이다. Hibernate lazy proxy는 실제 클래스가 다르므로 클래스 비교를 쓰지 않는다
- [x] 공통 설정 파일과 개발·테스트 프로파일 파일이 분리되어 있다
- [x] 개발 프로파일: H2 파일 DB(`back` 디렉터리 기준 `db_dev.mv.db`), 스키마 자동 갱신, h2-console 활성화
- [x] 테스트 프로파일: H2 인메모리 DB, 매번 스키마 생성, h2-console 없음
- [x] OSIV가 꺼져 있다
- [x] Spring Security가 들어 있다. 이 시점에는 모든 요청을 허용하고, CSRF는 끄고, 세션은 stateless다
- [x] CORS 허용 출처가 설정 프로퍼티로 주입된다. 코드에 하드코딩하지 않는다. front 개발 서버 주소가 개발 프로파일에 들어 있다
- [x] 서비스 계층이 던질 전용 예외와, 이를 RFC 9457 `ProblemDetail`로 변환하는 전역 핸들러가 있다
- [x] 입력값 검증 실패를 항목명과 메시지의 쌍으로 변환하는 처리가 전역 핸들러에 있다
- [x] Spring 페이지 객체를 그대로 직렬화하지 않고 감쌀 얇은 페이지 DTO가 있다. 페이지 번호는 1부터 시작한다
- [x] Swagger UI가 개발 프로파일에서 열린다
- [x] h2-console이 개발 프로파일에서 열리고, frame 관련 제약이 예외 처리되어 있다
- [x] DB 파일이 버전 관리에서 제외된다
- [x] 애플리케이션 컨텍스트가 뜨는지 확인하는 스모크 테스트가 통과한다
