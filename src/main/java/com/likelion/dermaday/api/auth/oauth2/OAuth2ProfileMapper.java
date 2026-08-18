package com.likelion.dermaday.api.auth.oauth2;

import com.likelion.dermaday.api.member.domain.OAuthProvider;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuth2ProfileMapper {

    private static final String MISSING_REQUIRED_PROFILE = "missing_required_profile";

    public OAuthProfile map(String registrationId, Map<String, Object> attributes) {
        OAuthProvider provider;
        try {
            provider = OAuthProvider.fromRegistrationId(registrationId);
        } catch (IllegalArgumentException exception) {
            throw oauth2Exception("unsupported_provider", "지원하지 않는 OAuth2 공급자입니다.");
        }

        return switch (provider) {
            case KAKAO -> mapKakao(attributes);
            case NAVER -> mapNaver(attributes);
        };
    }

    private OAuthProfile mapKakao(Map<String, Object> attributes) {
        String providerUserId = requiredString(attributes.get("id"));
        Map<String, Object> account = requiredMap(attributes.get("kakao_account"));
        Map<String, Object> profile = requiredMap(account.get("profile"));
        String displayName = requiredString(profile.get("nickname"));
        return new OAuthProfile(OAuthProvider.KAKAO, providerUserId, displayName);
    }

    private OAuthProfile mapNaver(Map<String, Object> attributes) {
        Map<String, Object> response = requiredMap(attributes.get("response"));
        String providerUserId = requiredString(response.get("id"));
        String displayName = requiredString(response.get("name"));
        return new OAuthProfile(OAuthProvider.NAVER, providerUserId, displayName);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> requiredMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            throw oauth2Exception(MISSING_REQUIRED_PROFILE, "필수 소셜 프로필 정보가 누락되었습니다.");
        }
        return (Map<String, Object>) map;
    }

    private String requiredString(Object value) {
        if (value == null) {
            throw oauth2Exception(MISSING_REQUIRED_PROFILE, "필수 소셜 프로필 정보가 누락되었습니다.");
        }

        String text = String.valueOf(value);
        if (text.isBlank()) {
            throw oauth2Exception(MISSING_REQUIRED_PROFILE, "필수 소셜 프로필 정보가 누락되었습니다.");
        }
        return text;
    }

    private OAuth2AuthenticationException oauth2Exception(String code, String message) {
        return new OAuth2AuthenticationException(new OAuth2Error(code), message);
    }
}
