# Loopro

블로그 기능을 기반으로 **교안**을 작성·공유하는 플랫폼. back과 front가 공유하는 domain language를 정의한다.

## Language

### 계정

**Member**:
로그인할 수 있는 사람. 수강생인지 저자인지 같은 신분이 아니라 **인증 주체**를 뜻한다.
_Avoid_: User, Account, 사용자

**Role**:
`Member`가 가지는 시스템 권한 등급. `USER`와 `ADMIN` 두 가지뿐이다.
_Avoid_: Authority, Grade, 등급

**Username**:
`Member`가 로그인할 때 사용하는 식별자. 유일하며 **화면에 노출하지 않는다**.
_Avoid_: LoginId, 아이디

**Nickname**:
`Member`를 화면에서 식별하는 표시용 이름. 유일하다. `Member`의 공개 식별자는 이것뿐이다.
_Avoid_: DisplayName, 별명

### 글

**Post**:
`Member`가 작성한 글. **교안도 `Post`다** — 교안과 일반 글을 구분하는 별도 개념은 없다. `Member`별 블로그로 나뉘지 않는 하나의 공간에 모인다.
_Avoid_: Article, Lesson, Material, 교안, 게시물

**Published**:
`Post`가 작성자 외의 사람에게 보이는 상태. 발행되지 않은 `Post`는 작성자만 볼 수 있다. `Post`의 유일한 공개 범위 개념이며, 지정 공유나 링크 공유는 없다.
_Avoid_: Public, Visibility, Draft, 발행

**Comment**:
`Post`에 달린 `Member`의 답글. `Comment`에는 `Comment`를 달 수 없다.
_Avoid_: Reply, 대댓글

**PostLike** / **CommentLike**:
`Member`가 `Post` 또는 `Comment`에 표시한 추천. 한 `Member`는 한 대상에 한 번만 추천할 수 있고 취소할 수 있다. 값을 세는 counter가 아니라 **누가 무엇을 추천했는지의 기록**이다.
_Avoid_: Vote, Upvote, Recommend, 좋아요

**ViewCount**:
`Post` 상세 조회 요청이 발생한 횟수. 같은 사람의 반복 조회를 구분하지 않으므로 **고유 독자 수가 아니다**.
_Avoid_: Hits, ReadCount
