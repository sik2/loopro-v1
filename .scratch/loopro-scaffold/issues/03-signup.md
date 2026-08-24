# 03: 회원가입

**What to build:** 방문자가 회원가입 화면에서 `Username`·비밀번호·`Nickname`을 입력해 `Member`가 된다. 이미 쓰이는 `Username`이나 `Nickname`이면 거부당하고, 형식이 틀리면 어느 입력칸이 왜 틀렸는지 그 자리에 표시된다. 아직 로그인은 없다 — 가입만 된다.

**Blocked by:** 01 (back 부트스트랩), 02 (front-react 부트스트랩)

**Status:** done

- [x] `Member` 엔티티가 있고 공통 상위 클래스를 상속한다
- [x] `Username`과 `Nickname`에 각각 유일 제약이 걸려 있다
- [x] `Role`은 `USER`와 `ADMIN` 두 값만 갖는다. 가입하면 `USER`다
- [x] `Member`에 이메일 필드는 없다
- [x] 비밀번호는 해시해서 저장한다. 평문이나 가역 암호화를 쓰지 않는다
- [x] 가입 API가 `Username`·비밀번호·`Nickname`을 받는다
- [x] `Username`이 중복이면 거부한다
- [x] `Nickname`이 중복이면 거부한다
- [x] 검증 실패는 항목명과 메시지의 쌍을 담은 `ProblemDetail`로 돌아온다
- [x] 회원가입 화면에서 가입이 완료된다
- [x] 화면의 검증 스키마가 back의 검증 규칙과 맞물리고, 서버가 준 항목별 오류가 해당 입력칸에 표시된다
- [x] HTTP seam 테스트: 가입 성공 / `Username` 중복 거부 / `Nickname` 중복 거부 / 검증 실패 응답의 형태
