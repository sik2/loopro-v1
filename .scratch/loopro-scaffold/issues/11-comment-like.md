# 11: `CommentLike`

**What to build:** `Member`가 댓글에도 글과 똑같이 추천하고 취소한다. 댓글마다 추천수가 보이고, 내가 추천한 댓글은 버튼에 표시된다.

**Blocked by:** 09 (`Comment`), 10 (`PostLike`)

**Status:** done

- [x] `CommentLike` 엔티티: 추천한 `Member`와 대상 `Comment`. `PostLike`와 별도 엔티티다
- [x] `Comment`와 `Member`의 조합에 유일 제약이 걸려 있다
- [x] 추천 API와 취소 API. 인증을 요구한다
- [x] 이미 추천한 대상을 다시 추천해도 기록이 늘지 않는다
- [x] `Comment` 목록 응답에 추천수가 담긴다
- [x] 요청이 인증된 경우 "내가 이 `Comment`를 추천했는지"가 함께 담긴다
- [x] 상세 화면의 각 댓글에 추천 버튼이 있고 내 상태를 반영한다
- [x] 로그인하지 않은 상태로 추천하려 하면 로그인하라고 안내한다
- [x] HTTP seam 테스트: 추천 후 재조회로 증가와 "내가 추천함" 확인 / 중복 방지 / 취소 후 재조회로 감소 확인
