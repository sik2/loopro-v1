# infra

배포·운영 관련 코드가 들어갈 자리. **아직 비어 있다.**

배포는 지금 이 디렉터리가 아니라 각 모듈에 붙어 있다 — `back/Dockerfile`, 루트의
`railway.json`, `front-react/public/_redirects`. 배포 대상이 둘뿐이고 모듈마다 하나씩이라,
여기로 모으면 파일이 자기 모듈에서 멀어지기만 한다.

배포 방법과 환경변수는 `docs/deploy.md`에, 로컬 실행은 저장소 루트의 `README.md`에 있다.
