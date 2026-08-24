# 12: 삭제 연쇄 정리

**What to build:** `Post`를 지우면 딸린 `Comment`와 두 종류의 추천 기록이 함께 사라진다. `Comment`를 지우면 그 댓글의 추천 기록이 사라진다. 고아 데이터가 남지 않는다.

물리 삭제다. 소프트 삭제를 쓰지 않는다 — 모든 조회에 필터 조건이 붙고 유일 제약과도 충돌하기 때문이다.

**Blocked by:** 06 (`Post` 수정·삭제와 권한), 09 (`Comment`), 11 (`CommentLike`)

**Status:** ready-for-agent

- [ ] `Post`를 삭제하면 딸린 `Comment`가 함께 물리 삭제된다
- [ ] `Post`를 삭제하면 그 `Post`의 `PostLike`가 함께 삭제된다
- [ ] `Post`를 삭제하면 딸린 `Comment`들의 `CommentLike`까지 함께 삭제된다
- [ ] `Comment`를 삭제하면 그 `Comment`의 `CommentLike`가 함께 삭제된다
- [ ] 어디에도 소프트 삭제를 쓰지 않는다
- [ ] HTTP seam 테스트: 댓글과 두 종류 추천이 달린 `Post`를 삭제한 뒤, 후속 요청으로 남은 데이터가 없는지 확인한다. 삭제 API가 200을 냈다는 것만으로 통과시키지 않는다
