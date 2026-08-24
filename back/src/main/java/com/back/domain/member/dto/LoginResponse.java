package com.back.domain.member.dto;

/**
 * 로그인 결과. 클라이언트는 accessToken을 저장했다가 요청 헤더에 실어 보낸다.
 */
public record LoginResponse(String accessToken, MemberDto member) {
}
