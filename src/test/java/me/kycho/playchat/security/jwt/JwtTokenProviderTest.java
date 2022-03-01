package me.kycho.playchat.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtTokenProviderTest {

    @Test
    @DisplayName("JWT 토큰 생성 및 authentication 조회 테스트")
    void createTokenTest_And_getAuthenticationTest() {
        // given
        String email = "kycho@naver.com";
        List<GrantedAuthority> authorities = Stream.of("ROLE_MEMBER", "ROLE_ADMIN")
            .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(email, "", authorities);

        JwtTokenProvider tokenProvider = new JwtTokenProvider(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            86400L);

        // when
        String token = tokenProvider.createToken(authentication);
        Authentication result = tokenProvider.getAuthentication(token);

        // then
        assertThat(result.getName()).isEqualTo(email);
        assertThat(result.getAuthorities()).isEqualTo(authorities);
    }

    @Test
    @DisplayName("토큰 유효성 검사 테스트")
    void validateTokenTest() {
        // given
        String email = "kycho@naver.com";
        List<GrantedAuthority> authorities = Stream.of("ROLE_MEMBER", "ROLE_ADMIN")
            .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(email, "", authorities);

        JwtTokenProvider tokenProvider = new JwtTokenProvider(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            86400L);
        JwtTokenProvider tokenProviderNoTime = new JwtTokenProvider(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            0L);

        // when
        String token = tokenProvider.createToken(authentication);
        String tokenNoTime = tokenProviderNoTime.createToken(authentication);

        // then
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.validateToken(token + "a")).isFalse();
        assertThat(tokenProvider.validateToken(tokenNoTime)).isFalse();
    }
}
