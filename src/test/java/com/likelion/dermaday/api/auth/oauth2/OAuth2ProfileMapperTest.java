package com.likelion.dermaday.api.auth.oauth2;

import com.likelion.dermaday.api.member.domain.OAuthProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OAuth2ProfileMapperTest {

    private final OAuth2ProfileMapper mapper = new OAuth2ProfileMapper();

    @Test
    void mapsKakaoIdAndNickname() {
        OAuthProfile profile = mapper.map("kakao", Map.of(
                "id", 12345L,
                "kakao_account", Map.of(
                        "profile", Map.of("nickname", "카카오 사용자")
                )
        ));

        assertEquals(OAuthProvider.KAKAO, profile.provider());
        assertEquals("12345", profile.providerUserId());
        assertEquals("카카오 사용자", profile.displayName());
    }

    @Test
    void mapsNaverIdAndName() {
        OAuthProfile profile = mapper.map("naver", Map.of(
                "response", Map.of(
                        "id", "naver-user-id",
                        "name", "네이버 사용자"
                )
        ));

        assertEquals(OAuthProvider.NAVER, profile.provider());
        assertEquals("naver-user-id", profile.providerUserId());
        assertEquals("네이버 사용자", profile.displayName());
    }

    @Test
    void rejectsMissingDisplayName() {
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> mapper.map("naver", Map.of(
                        "response", Map.of("id", "naver-user-id")
                ))
        );

        assertEquals("missing_required_profile", exception.getError().getErrorCode());
    }
}
