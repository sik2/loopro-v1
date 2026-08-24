package com.back.global.initdata;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.post.entity.Comment;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.CommentRepository;
import com.back.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 개발용 샘플 데이터. 빈 화면부터 시작하지 않게 하고, 권한 분기·공개 범위·마크다운 렌더링을
 * 띄우자마자 눈으로 확인할 수 있게 한다.
 *
 * <p>테스트 프로파일에서는 아예 빈으로 올라오지 않는다. 테스트는 매번 빈 DB에서 시작해야 한다.
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class BaseInitDataService {

	/** 샘플 계정의 공통 비밀번호. 개발용이며 README에 적혀 있다. */
	private static final String PASSWORD = "password123";

	private final MemberRepository memberRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void run() {
		// 한 명이라도 있으면 이미 채워진 DB다. 재시작할 때마다 데이터가 불어나면 안 된다.
		if (memberRepository.count() > 0) {
			log.info("샘플 데이터를 건너뜁니다. 이미 Member가 있습니다.");
			return;
		}

		Member admin = memberRepository.save(
				Member.joinAsAdmin("admin", passwordEncoder.encode(PASSWORD), "관리자"));
		Member gureum = joinUser("gureum", "구름");
		Member baram = joinUser("baram", "바람");
		Member namu = joinUser("namu", "나무");
		Member byeol = joinUser("byeol", "별");

		List<Post> posts = postRepository.saveAll(List.of(
				Post.write(gureum, "Spring Boot 4로 REST API 만들기", API_교안, true),
				Post.write(baram, "마크다운으로 교안 쓰기", 마크다운_교안, true),
				Post.write(namu, "H2 콘솔로 개발 DB 들여다보기", H2_교안, true),
				Post.write(admin, "공지: 이 플랫폼은 아직 스캐폴딩입니다", 공지, true),
				// 최소 하나는 비발행이다. 목록·상세에서 작성자에게만 보이는지 바로 확인할 수 있다.
				Post.write(gureum, "쓰다 만 교안 (아직 비발행)", 초안, false)
		));

		commentRepository.saveAll(List.of(
				Comment.write(baram, posts.get(0), "JDK 25 툴체인 설정에서 막혔는데 이거 보고 풀었습니다."),
				Comment.write(namu, posts.get(0), "표 렌더링까지 되는 게 좋네요."),
				Comment.write(gureum, posts.get(1), "코드블록 예시가 특히 도움이 됐어요."),
				Comment.write(byeol, posts.get(2), "JDBC URL을 자꾸 틀렸는데 덕분에 찾았습니다."),
				Comment.write(admin, posts.get(3), "질문은 댓글로 남겨주세요.")
		));

		log.info("샘플 데이터를 만들었습니다. Member 5, Post 5, Comment 5. 비밀번호는 모두 {}", PASSWORD);
	}

	private Member joinUser(String username, String nickname) {
		return memberRepository.save(Member.join(username, passwordEncoder.encode(PASSWORD), nickname));
	}

	private static final String API_교안 = """
			# Spring Boot 4로 REST API 만들기

			이 교안은 `back` 모듈을 기준으로 합니다.

			## 준비물

			- JDK 25
			- Gradle Kotlin DSL
			- H2

			## 컨트롤러

			```java
			@RestController
			@RequestMapping("/api/posts")
			class PostController {
			    @GetMapping("/{id}")
			    PostDetailDto detail(@PathVariable long id) { ... }
			}
			```

			## 프로파일

			| 프로파일 | DB | ddl-auto |
			| --- | --- | --- |
			| dev | 파일 | update |
			| test | 메모리 | create |
			""";

	private static final String 마크다운_교안 = """
			# 마크다운으로 교안 쓰기

			## 왜 마크다운인가

			1. 텍스트라서 diff가 읽힙니다
			2. 코드블록이 자연스럽습니다
			3. 어디에 붙여넣어도 깨지지 않습니다

			> 교안도 Post입니다. 별도 엔티티를 만들지 않습니다.

			## 코드블록

			```typescript
			export function formatDate(value: string) {
			  return new Date(value).toLocaleDateString('ko-KR')
			}
			```
			""";

	private static final String H2_교안 = """
			# H2 콘솔로 개발 DB 들여다보기

			## 접속

			- 주소: `http://localhost:8080/h2-console`
			- JDBC URL: `jdbc:h2:./db_dev`
			- 사용자: `sa`, 비밀번호: 없음

			## 자주 쓰는 쿼리

			```sql
			select id, nickname, role from member;
			select id, title, published from post order by create_date desc;
			```

			개발 프로파일은 파일 DB라서 앱을 껐다 켜도 데이터가 남습니다.
			""";

	private static final String 공지 = """
			# 아직 스캐폴딩입니다

			지금 되는 것:

			- 가입, 로그인, 내 정보
			- 글 쓰기·고치기·지우기, 발행 여부
			- 댓글, 글·댓글 추천

			아직 없는 것:

			- 시리즈와 목차
			- 코드블록 raw 공유
			- 검색, 태그, 이미지 업로드
			""";

	private static final String 초안 = """
			# 쓰다 만 교안

			여기까지 썼습니다.

			- [ ] 목차 정리
			- [ ] 예제 코드 채우기

			이 글은 발행하지 않았으므로 작성자인 `구름`에게만 보입니다.
			""";
}
